package com.versionone.git;

import com.versionone.git.configuration.GitConnection;
import com.versionone.git.configuration.ChangeSet;
import com.versionone.git.storage.IDbStorage;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitConnector implements IGitConnector {
    private FileRepository local;
    private RemoteConfig remoteConfig;

    private final String remoteBranchName = "refs/heads/master";
    private final String remoteName = "origin";

    private final int timeout = 100;
    private GitConnection gitConnection;
    private ChangeSet changeSetConfig;

    private final String localDirectory;
    private final IDbStorage storage;
    private final String repositoryId;

    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public GitConnector(GitConnection gitConnection, String repositoryId, String localDirectory, IDbStorage storage, ChangeSet changeSetConfig) {
        this.gitConnection = gitConnection;
        this.repositoryId = repositoryId;
        this.localDirectory = localDirectory;
        this.storage = storage;
        this.changeSetConfig = changeSetConfig;

        SshSessionFactory.installWithCredentials(gitConnection.getPassword(), gitConnection.getPassphrase());
    }

    public void initRepository() throws GitException {
    	LOG.debug("Initalizing repository...");

        try {
            cloneRepository();
            doFetch();
        } catch (IOException ex) {
            LOG.fatal("Local repository creation failed: "+ ex.getMessage());
            throw new GitException(ex);
        } catch (URISyntaxException ex) {
            LOG.fatal("Local repository creation failed: "+ ex.getMessage());
            throw new GitException(ex);
        }
    }

    public List<ChangeSetInfo> getChangeSets() throws GitException {
        try {
            doFetch();

            ChangeSetListBuilder builder = new ChangeSetListBuilder(Pattern.compile(changeSetConfig.getReferenceExpression())) {
                public boolean shouldAdd(ChangeSetInfo changeSet) {
                    if(changeSetConfig.isAlwaysCreate()){
                        return true;
                    }

                    if(gitConnection.getUseBranchName()) {
                        return changeSet.getReferences().size() > 0;
                    } else {
                        return matchByPattern(changeSet.getMessage());
                    }
                }
            };

            traverseChanges(builder);

            return builder.build();
        } catch(NotSupportedException ex) {
            LOG.fatal(ex);
            throw new GitException(ex);
        } catch(TransportException ex) {
            LOG.fatal(ex);
            throw new GitException(ex);
        }
    }

    private void traverseChanges(ChangeSetListBuilder builder) throws GitException {

        Iterable<RevCommit> commits = getCommits();

        for (RevCommit commit : commits) {

            // jGit returns data in seconds
            long millisecond = commit.getCommitTime() *  1000l;
            ChangeSetInfo info = new ChangeSetInfo(
                    gitConnection,
                    commit.getAuthorIdent().getName(),
                    commit.getFullMessage().trim(),
                    commit.getId().getName(),
                    new Date(millisecond));

            if(gitConnection.getUseBranchName()) {
                List<String> branches = getBranchNames(commit);
                for(String branch : branches) {
                    fillReferences(branch, info.getReferences());
                }
            } else {
                fillReferences(info.getMessage(), info.getReferences());
            }

            builder.add(info);
        }
    }

    private Iterable<RevCommit> getCommits() throws GitException {
        ArrayList<RevCommit> commits = new ArrayList<RevCommit>();

        try {
            Git git = new Git(local);
            Map<String, Ref> refs;

            // Either filter by just the watched branch if one is specified, or get all branch refs
            if (gitConnection.getWatchedBranch() != null && !gitConnection.getWatchedBranch().trim().isEmpty()) {
                String branchName = Constants.R_REMOTES + "/" + Constants.DEFAULT_REMOTE_NAME +  "/" + gitConnection.getWatchedBranch();
                refs = new HashMap();
                refs.put(branchName, local.getRef(branchName));
            }
            else
                refs = local.getAllRefs();

            // Iterate through each branch checking for any new commits since the last one processed
            for (String ref : refs.keySet()) {

                try {
                    // Skip anything other than branches (e.g. tags) since they're not commit objects and
                    // will throw an IncorrectObjectTypeException when setting the log command range
                    if (!ref.contains("refs/remotes/origin"))
                        continue;

                    // For each branch traversal use a new log object, since they're intended to be called only once
                    LogCommand logCommand = git.log();

                    AnyObjectId headId;
                    RevWalk walk = new RevWalk(local);
                    walk.sort(RevSort.COMMIT_TIME_DESC);
                    walk.sort(RevSort.TOPO);

                    headId = local.resolve(refs.get(ref).getName());
                    walk.markStart(walk.parseCommit(headId));

                    String headHash = headId.getName();
                    String persistedHash = storage.getLastCommit(repositoryId, ref);

                    if (persistedHash != null) {
                        AnyObjectId persistedHeadId = local.resolve(persistedHash);
                        LOG.debug("Checking branch " + ref + " for new commits since the last one processed (" + persistedHash + ")...");
                        //here we get lock for directory
                        logCommand.addRange(persistedHeadId, headId);
                    } else {
                        logCommand.add(headId);
                        LOG.debug("Last commit processed on branch " + ref + " was not found so processing commits from the beginning.");
                    }

                    if(!headHash.equals(persistedHash)) {
                        for (RevCommit commit : logCommand.call()) {
                            if (!commits.contains(commit))
                                commits.add(commit);
                        }
                        storage.persistLastCommit(headHash, repositoryId, ref);
                    } else {
                        LOG.debug("No new commits were found on branch " + ref);
                    }
                } catch (IOException ex) {
                    LOG.error(ref + " couldn't be processed:", ex);
                } catch (NoHeadException ex) {
                    LOG.error("Couldn't find starting revision for " + ref, ex);
                }
            }
        } catch (Exception ex) {
            LOG.fatal("An exception occurred in the Git connector while getting commits:", ex);
            throw new GitException(ex);
        }

        // Sort commits by commit time which is needed when they've been taken
        // from multiple branches since they won't be listed chronologically
        Comparator comparator = new GitCommitComparator();
        Collections.sort(commits, comparator);

        return commits;
    }

    private void fillReferences(String message, List<String> references) {
        Matcher matcher = Pattern.compile(changeSetConfig.getReferenceExpression()).matcher(message);

        while(matcher.find()) {
            if (!references.contains(matcher.group()))
                references.add(matcher.group());
        }
    }

    private List<String> getBranchNames(RevCommit commit) {
        List<String> branchNames = new LinkedList<String>();
        Map<String, Ref> refs = local.getAllRefs();

        for (String key : refs.keySet()) {
            AnyObjectId headId;
            RevWalk walk = new RevWalk(local);
            walk.sort(RevSort.COMMIT_TIME_DESC);
            walk.sort(RevSort.TOPO);

            try {
                headId = local.resolve(refs.get(key).getName());
                walk.markStart(walk.parseCommit(headId));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (RevCommit commitFromBranch : walk) {
                if (commit.equals(commitFromBranch)) {
                    branchNames.add(refs.get(key).getName());
                    break;
                }
            }
        }

        return branchNames;
    }

    private void cloneRepository() throws IOException, URISyntaxException {
    	LOG.debug("Cloning repository...");
        local = new FileRepository(localDirectory);
        local.create();

        URIish uri = new URIish(gitConnection.getRepositoryPath());

		remoteConfig = new RemoteConfig(local.getConfig(), remoteName);
		remoteConfig.addURI(uri);

		final String dst = Constants.R_REMOTES + remoteConfig.getName();
		RefSpec wcrs = new RefSpec();
		wcrs = wcrs.setForceUpdate(true);
		wcrs = wcrs.setSourceDestination(Constants.R_HEADS + "*", dst + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
        remoteConfig.addFetchRefSpec(wcrs);

		local.getConfig().setBoolean("core", null, "bare", true); //$NON-NLS-1$ //$NON-NLS-2$

		remoteConfig.update(local.getConfig());

		String branchName = remoteBranchName;

		// setup the default remote branch for branchName
		local.getConfig().setString("branch", branchName, "remote", remoteName); //$NON-NLS-1$ //$NON-NLS-2$
		local.getConfig().setString("branch", branchName, "merge", remoteBranchName); //$NON-NLS-1$ //$NON-NLS-2$

		local.getConfig().save();

        local.close();
    }

	private void doFetch() throws NotSupportedException, TransportException {
		LOG.debug("Fetching repository...");
		final Transport tn = Transport.open(local, remoteConfig);
		tn.setTimeout(this.timeout);

        try {
        	tn.fetch(new ProgressMonitor() {
				@Override public void beginTask(String taskName, int totalWork) {LOG.debug(taskName + ", total subtasks: " + totalWork);}
				@Override public void start(int totalTasks) { LOG.debug("Starting task, total tasks: " + totalTasks); }
				@Override public void update(int completed) {}
				@Override public void endTask() {}
				@Override public boolean isCancelled() {return false;}}
        	, null);
		} finally {
			tn.close();
		}
	}

    /** Compares two commits and sorts them by commit time in ascending order */
    private class GitCommitComparator implements Comparator {
        public int compare(Object object1, Object object2) {
            RevCommit commit1 = (RevCommit)object1;
            RevCommit commit2 = (RevCommit)object2;

            return commit1.getCommitTime() - commit2.getCommitTime();
        }
    }
}
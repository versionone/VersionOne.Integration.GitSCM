package com.versionone.git;

import org.apache.log4j.Logger;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitConnector implements IGitConnector {

    private FileRepository local;
    private RemoteConfig remoteConfig;

    private final String remoteBranchName = "refs/heads/master";
    private final String remoteName = "origin";

    private final int timeout = 100;

    private final String url;

    private final String localDirectory;
    private final String watchedBranch;

    private final Pattern regexPattern;

    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public GitConnector(String password, String passphrase, String url, String watchedBranch,
                        String localDirectory, String regexPattern) {
        this.url = url;
        this.watchedBranch = watchedBranch;
        this.localDirectory = localDirectory;
        this.regexPattern = Pattern.compile(regexPattern);

        SshSessionFactory.installWithCredentials(password, passphrase);

    }

    public void cleanupLocalDirectory() {
        deleteDirectory(new File(localDirectory));
    }

    public void initRepository() throws ConnectorException {
        try {
            cloneRepository();
            doFetch();
        } catch (IOException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        } catch (URISyntaxException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        }
    }

    public List<ChangeSetInfo> getBranchCommits() throws ConnectorException {
        try {
            doFetch();

            ChangeSetListBuilder builder = new ChangeSetListBuilder(regexPattern) {
                public boolean shouldAdd(ChangeSetInfo changeSet) {
                    return matchByPattern(changeSet.getMessage());
                }
            };

            traverseChanges(builder, true);
            return builder.build();
        } catch(NotSupportedException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        } catch(TransportException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        }
    }

    public List<ChangeSetInfo> getMergedBranches() throws ConnectorException {
        try {
            doFetch();

            ChangeSetListBuilder builder = new ChangeSetListBuilder(regexPattern) {
                public boolean shouldAdd(ChangeSetInfo changeSet) {
                    return changeSet.getReferences().size() > 0;
                }
            };

            traverseChanges(builder, false);
            return builder.build();
        } catch(NotSupportedException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        } catch(TransportException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        }
    }

    private List<ChangeSetInfo> traverseChanges(ChangeSetListBuilder builder, boolean useCommitMessages)
            throws ConnectorException {
        Map<String, Ref> refs = local.getAllRefs();
        for (String key : refs.keySet()) {
            System.out.println(key + " - " + refs.get(key).getName());
        }

        RevWalk walk = new RevWalk(local);
        walk.sort(RevSort.COMMIT_TIME_DESC);
        walk.sort(RevSort.TOPO);
        try {
            AnyObjectId headId = local.resolve(Constants.R_REMOTES + "/" + Constants.DEFAULT_REMOTE_NAME +  "/" + watchedBranch);
            walk.markStart(walk.parseCommit(headId));//
        } catch (IOException ex) {
            LOG.fatal(ex);
            throw new ConnectorException(ex);
        }

        for (RevCommit commit : walk) {
            ChangeSetInfo info = new ChangeSetInfo(
                    commit.getAuthorIdent().getName(),
                    commit.getFullMessage().trim(),
                    commit.getId().getName(),
                    new Date(commit.getCommitTime()));

            if(useCommitMessages) {
                fillReferences(info.getMessage(), info.getReferences());
            } else {
                List<String> branches = getBranchNames(commit);
                for(String branch : branches) {
                    fillReferences(branch, info.getReferences());
                }
            }

            builder.add(info);
        }

        return builder.build();
    }

    private void fillReferences(String message, List<String> references) {
        Matcher matcher = regexPattern.matcher(message);

        while(matcher.find()) {
            references.add(matcher.group());
        }
    }

    private List<String> getBranchNames(RevCommit commit) {
        List<String> branchNames = new LinkedList<String>();
        Map<String, Ref> refs = local.getAllRefs();

        for (String key : refs.keySet()) {
            AnyObjectId headId = null;
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
        local = new FileRepository(localDirectory);
        local.create();

        URIish uri = new URIish(url);

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
    }

	private void doFetch() throws NotSupportedException, TransportException {
		final Transport tn = Transport.open(local, remoteConfig);
		tn.setTimeout(this.timeout);

        try {
			FetchResult fetchResult = tn.fetch(NullProgressMonitor.INSTANCE, null);
		} finally {
			tn.close();
		}
	}

	private static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			for (String child : dir.list()) {
				boolean success = deleteDirectory(new File(dir, child));

                if (!success) {
					return false;
				}
			}
		}

        return dir.delete();
	}
}
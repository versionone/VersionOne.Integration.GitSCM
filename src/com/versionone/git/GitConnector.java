package com.versionone.git;

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
import java.util.regex.Pattern;

public class GitConnector implements IGitConnector {

    private FileRepository local;
    private RemoteConfig remoteConfig;

    private final String remoteBranchName = "refs/heads/master";
    private final String remoteName = "origin";

    private final int timeout = 100;

    private final String url;

    private final String localDirectory;
    private final String regexPattern;
    private final String watchedBranch;

    private final IDbStorage storage;

    public GitConnector(String password, String passphrase, String url, String watchedBranch,
                        String localDirectory, String regexPattern, IDbStorage storage) {
        this.storage = storage;

        this.url = url;
        this.watchedBranch = watchedBranch;
        this.localDirectory = localDirectory;
        this.regexPattern = regexPattern;

        SshSessionFactory.installWithCredentials(password, passphrase);

    }

    public void cleanupLocalDirectory() {
        deleteDirectory(new File(localDirectory));
    }

    public void initRepository() throws ConnectorException {
        try {
            cloneRepository();
            doFetch();
        } catch (IOException e) {
            throw new ConnectorException(e);
        } catch (URISyntaxException e) {
            throw new ConnectorException(e);
        }
    }

    public List<ChangeSetInfo> getBranchCommits() throws ConnectorException {
        try {
            doFetch();

            // TODO extract class and move interface off connector
            IChangeSetListBuilder builder = new IChangeSetListBuilder() {
                private final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
                private final Pattern regex = Pattern.compile(regexPattern);

                public IChangeSetListBuilder add(ChangeSetInfo info) {
                    if(regex.matcher(info.getMessage()).find()) {
                        changes.add(info);
                    }
                    
                    return this;
                }

                public List<ChangeSetInfo> build() {
                    return changes;
                }
            };
            traverseChanges(builder);
            return builder.build();
        } catch(NotSupportedException e) {
            throw new ConnectorException(e);
        } catch(TransportException e) {
            throw new ConnectorException(e);
        }
    }

    public List<ChangeSetInfo> getMergedBranches() throws ConnectorException {
        // TODO
        return null;
    }

    private List<ChangeSetInfo> traverseChanges(IChangeSetListBuilder builder) throws ConnectorException {
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
        } catch (IOException e) {
            throw new ConnectorException(e);
        }

        for (RevCommit commit : walk) {
            ChangeSetInfo info = new ChangeSetInfo(
                    commit.getAuthorIdent().getName(),
                    commit.getFullMessage().trim(),
                    commit.getId().getName(),
                    new Date(commit.getCommitTime()));
            builder.add(info);

            List<String> branchNames = getBranchNames(commit);
        }

        return builder.build();
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

    private interface IChangeSetListBuilder {
        IChangeSetListBuilder add(ChangeSetInfo changeSet);
        List<ChangeSetInfo> build();
    }
}
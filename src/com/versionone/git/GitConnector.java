package com.versionone.git;

import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GitConnector implements IGitConnector {

    private FileRepository local;
    private RemoteConfig remoteConfig;
    private FetchResult fetchResult;

    private final String remoteBranchName = "refs/heads/master";
    private final String remoteName = "origin";

    private final int timeout = 100;

    private final String url;
    private final String username;
    private final String password;
    private final String passphrase;
    private final String localDirectory;

    private final IDbStorage storage;

    public GitConnector(String username, String password, String passphrase, String url, String localDirectory, IDbStorage storage) {
        this.storage = storage;

        this.username = username;
        this.password = password;
        this.passphrase = passphrase;
        this.url = url;
        this.localDirectory = localDirectory;

        SshSessionFactory.installWithCredentials(password, passphrase);
        Authenticator.install(username, password);

        // TODO uncomment when configuration is ready
        // initRepository();
    }

    private void initRepository() {
        try {
            //checkoutBranch(branchName);
            cloneRepository();
            doFetch();
            //doCheckout();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        Map<String, Ref> refs = local.getAllRefs();
        for (String key : refs.keySet()) {
            System.out.println(key + " - " + refs.get(key).getName());
        }

        RevWalk walk = new RevWalk(local);
        walk.sort(RevSort.COMMIT_TIME_DESC);
        walk.sort(RevSort.TOPO);
        try {
            AnyObjectId headId = local.resolve(Constants.R_REMOTES + "/" + Constants.DEFAULT_REMOTE_NAME +  "/master");
            walk.markStart(walk.parseCommit(headId));//
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(" -  - - - -  -  --  - - - - -  --  - -");

        for (RevCommit commit : walk) {
            System.out.println("Id = " + commit.getId().getName());
            System.out.println("Message =  " + commit.getFullMessage().trim());
            System.out.println("Date =  " + new Date(commit.getCommitTime()));
            System.out.println("Author =  " + commit.getAuthorIdent().getName());
            List<String> branchNames = getBranchNames(commit);
            System.out.println("BranchNames:");
            for (String name : branchNames) {
                System.out.println(name);
            }


            System.out.println("Files:");

            TreeWalk treeWalk = new TreeWalk(local);

            RevTree tree = commit.getTree();

            System.out.println(" -----------  ");
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
		wcrs = wcrs.setSourceDestination(Constants.R_HEADS
				+ "*", dst + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
        remoteConfig.addFetchRefSpec(wcrs);

		local.getConfig().setBoolean(
				"core", null, "bare", true); //$NON-NLS-1$ //$NON-NLS-2$

		remoteConfig.update(local.getConfig());

		// branch is like 'Constants.R_HEADS + branchName', we need only
		// the 'branchName' part
		String branchName = remoteBranchName;//.substring(Constants.R_HEADS.length());

		// setup the default remote branch for branchName
		local.getConfig().setString("branch", branchName, "remote", remoteName); //$NON-NLS-1$ //$NON-NLS-2$
		local.getConfig().setString("branch", branchName, "merge", remoteBranchName); //$NON-NLS-1$ //$NON-NLS-2$

		local.getConfig().save();
    }

	private void doFetch()
			throws NotSupportedException, TransportException {
		final Transport tn = Transport.open(local, remoteConfig);
		tn.setTimeout(this.timeout);
		try {
			fetchResult = tn.fetch(NullProgressMonitor.INSTANCE, null);
		} finally {
			tn.close();
		}
	}

	private static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
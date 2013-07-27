package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import com.versionone.git.configuration.GitConnection;
import com.versionone.git.storage.DbStorage;
import com.versionone.git.storage.IDbStorage;
import org.apache.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class GitPollTask extends TimerTask {
    private final IChangeSetWriter changeSetWriter;
    private static final Logger LOG = Logger.getLogger("GitIntegration");
    private final Configuration configuration;
    private Map<GitConnection, GitService> gitServices = new HashMap<GitConnection, GitService>();
    private final static String REPOSITORY_DIRECTORY_PATTERN = "%s/%sRepo";

    GitPollTask(Configuration configuration) throws VersionOneException, NoSuchAlgorithmException {
        this.configuration = configuration;

        IVersionOneConnector v1Connector = new VersionOneConnector();
        v1Connector.connect(configuration.getVersionOneConnection());

        changeSetWriter = new ChangeSetWriter(configuration.getChangeSet(), v1Connector);
        cleanupLocalDirectory();
        serviceInitialize();
    }

    public void serviceInitialize() throws NoSuchAlgorithmException {
        int amountOfServices = configuration.getGitConnections().size();
        LOG.info(String.format("Creating %s Git service(s)...", amountOfServices));

        for (int gitRepositoryIndex = 0; gitRepositoryIndex < amountOfServices; gitRepositoryIndex ++) {
            GitConnection gitConnection = configuration.getGitConnections().get(gitRepositoryIndex);
            String repositoryId = Utilities.getRepositoryId(gitConnection);
            GitService service = getGitService(gitConnection, repositoryId);

            if (service != null) {
                gitServices.put(gitConnection, service);
            }
        }

        LOG.info("Git services created successfully");
    }

    @Override
    public void run() {
        LOG.info("Checking for new changes...");

        for (GitConnection gitConnection : gitServices.keySet()) {
            LOG.info("Checking " + gitConnection.getRepositoryPath());
            processRepository(gitServices.get(gitConnection));
        }
        LOG.info("Check for new changes complete");
    }

    private void processRepository(GitService service) {
        try {
            service.onInterval();
        } catch(GitException ex) {
            LOG.fatal("Git service failed: " + ex.getInnerException().getMessage());
        } catch (VersionOneException ex) {
            LOG.fatal("VersionOne service failed: " + ex.getInnerException().getMessage());
        }
    }

    private GitService getGitService(GitConnection gitConnection, String repositoryId) {
        IDbStorage storage = new DbStorage();

        IGitConnector gitConnector = new GitConnector(
                gitConnection,
                repositoryId,
                String.format(REPOSITORY_DIRECTORY_PATTERN, configuration.getLocalDirectory(), repositoryId),
                storage,
                configuration.getChangeSet()
        );

        GitService service = new GitService(storage, gitConnector, changeSetWriter, repositoryId);

        LOG.info(String.format("Initializing Git Service for %s...", gitConnection.getRepositoryPath()));
        return initializeGitService(service) ? service : null;
    }

    private boolean initializeGitService(GitService service) {
        try {
            service.initialize();
        } catch (GitException ex) {
            System.out.println("Fail: " + ex.getInnerException().getMessage());
            LOG.fatal("Git service initialize failed: " + ex.getInnerException().getMessage());
            return false;
        }

        return true;
    }

    private void cleanupLocalDirectory() {
        LOG.debug(String.format("Resetting local directory %s...", configuration.getLocalDirectory()));

        if (!Utilities.deleteDirectory(new File(configuration.getLocalDirectory()))) {
            LOG.warn(configuration.getLocalDirectory() + " couldn't be reset, possibly due to this being the first time the service has been run");
        }

        boolean result = new File(configuration.getLocalDirectory()).mkdir();

        if (!result) {
            LOG.fatal(configuration.getLocalDirectory() + " couldn't be created");
            System.exit(-1);
        }
    }
}

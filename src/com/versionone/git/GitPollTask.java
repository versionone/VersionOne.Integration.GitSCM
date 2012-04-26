package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import com.versionone.git.configuration.GitSettings;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class GitPollTask extends TimerTask {
    private final IChangeSetWriter changeSetWriter;
    private static final Logger LOG = Logger.getLogger("GitIntegration");
    private final Configuration configuration;
    private Map<GitSettings, GitService> gitServices = new HashMap<GitSettings, GitService>();

    GitPollTask(Configuration configuration) throws VersionOneException {
        LOG.info("Creating services...");
        this.configuration = configuration;

        IVersionOneConnector v1Connector = new VersionOneConnector();
        v1Connector.connect(configuration.getVersionOneSettings());

        changeSetWriter = new ChangeSetWriter(configuration, v1Connector);
        cleanupLocalDirectory();
        gitServiceInitialize();

        LOG.info("Services created.");
    }

    public void gitServiceInitialize() {
        int amountOfServices = configuration.getGitSettings().size();
        LOG.info("Creating services (" + amountOfServices + ")...");

        for (int gitRepositoryIndex = 0; gitRepositoryIndex < amountOfServices; gitRepositoryIndex ++) {
            GitSettings gitSettings = configuration.getGitSettings().get(gitRepositoryIndex);
            GitService service = getGitService(gitRepositoryIndex);
            if (service != null) {
                gitServices.put(gitSettings, service);
            }
        }
    }


    @Override
    public void run() {
        LOG.info("Processing new changes...");

        //for (int gitRepository = 0; gitRepository < configuration.getGitSettings().size(); gitRepository ++) {
        for (GitService service : gitServices.values()) {
            //cleanupLocalDirectory();
            //LOG.info("Processing " + (gitRepository + 1) + " repository.");

            processRepository(service);
        }

        LOG.info("Completed.");
    }

    private void processRepository(GitService service) {
        try {
            service.onInterval();
        } catch(GitException ex) {
            System.out.println("Fail: " + ex.getInnerException().getMessage());
            LOG.fatal("Git service failed: " + ex.getInnerException().getMessage());
        } catch (VersionOneException ex) {
            System.out.println("Fail: " + ex.getInnerException().getMessage());
            LOG.fatal("VersionOne service failed: " + ex.getInnerException().getMessage());
        }
    }

    private GitService getGitService(int gitRepository) {
        IDbStorage storage = new DbStorage();

        GitSettings gitSettings = configuration.getGitSettings().get(gitRepository);

        IGitConnector gitConnector = new GitConnector(
                gitSettings.getPassword(),
                gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(),
                gitSettings.getWatchedBranch(),
                String.format("%s/%sRepo", configuration.getLocalDirectory(), gitRepository),
                configuration.getReferenceExpression(),
                gitSettings.getUseBranchName(),
                configuration.isAlwaysCreate(),
                storage);
        GitService service = new GitService(storage, gitConnector, changeSetWriter);

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
        LOG.debug("cleanupLocalDirectory");

        if (!Utilities.deleteDirectory(new File(configuration.getLocalDirectory()))) {
            LOG.error(configuration.getLocalDirectory() + " can't be cleaned up");
        }

        boolean result = new File(configuration.getLocalDirectory()).mkdir();

        if (!result) {
            LOG.error(configuration.getLocalDirectory() + " can't be created");
        }
    }
}

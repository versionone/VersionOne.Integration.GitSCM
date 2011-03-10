package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import com.versionone.git.configuration.GitSettings;
import org.apache.log4j.Logger;

import java.util.TimerTask;

public class GitPollTask extends TimerTask {
    private final GitService service;
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    GitPollTask(Configuration configuration) throws GitException, VersionOneException {
        LOG.info("Creating service...");

        IDbStorage storage = new DbStorage();

        GitSettings gitSettings = configuration.getGitSettings();

        IGitConnector gitConnector = new GitConnector(
                gitSettings.getPassword(),
                gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(),
                gitSettings.getWatchedBranch(),
                gitSettings.getLocalDirectory(),
                configuration.getReferenceExpression(),
                configuration.getUseBranchName()
        );

        IVersionOneConnector v1Connector = new VersionOneConnector();
        v1Connector.connect(configuration.getVersionOneSettings());

        IChangeSetWriter changeSetWriter = new ChangeSetWriter(configuration, v1Connector);

        service = new GitService(storage, gitConnector, changeSetWriter);
        service.initialize();

        LOG.info("Service created.");
    }

    @Override
    public void run() {
        LOG.info("Processing new changes...");

        try {
            service.onInterval();
        } catch(GitException ex) {
            System.out.println("Fail: " + ex.getInnerException().getMessage());
            LOG.fatal("Git service failed: " + ex.getInnerException().getMessage());
        } catch (VersionOneException ex) {
            System.out.println("Fail: " + ex.getInnerException().getMessage());
            LOG.fatal("VersionOne service failed: " + ex.getInnerException().getMessage());
        }

        LOG.info("Completed.");
    }
}

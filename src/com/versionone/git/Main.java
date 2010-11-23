package com.versionone.git;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private final static Timer timer = new Timer();
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public static void main(String[] arg) {
        LOG.info("Loading config..");
        Configuration configuration = Configuration.getInstance();
        LOG.info("Configuration loaded..");

        try {
            timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getTimeoutMillis());
        } catch(GitException e) {
            System.exit(-1);
        }

        while(true) { /* do nothing, the job is done in background thread */ }
    }

    private static class GitPollTask extends TimerTask {

        private final GitService service;

        GitPollTask(Configuration configuration) throws GitException {
            LOG.info("Creating service...");
            IDbStorage storage = new DbStorage();
            Configuration.GitSettings gitSettings = configuration.getGitSettings();
            IGitConnector connector = new GitConnector(gitSettings.getPassword(), gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(), gitSettings.getWatchedBranch(), gitSettings.getLocalDirectory(),
                configuration.getReferenceExpression());
            service = new GitService(configuration, storage, connector);
            service.initialize();
            LOG.info("Service created.");
        }

        @Override
        public void run() {
            LOG.info("Processing new changes...");

            try {
                service.onInterval();
            } catch(GitException e) {
                System.out.println("Fail: " + e.getInnerException().getMessage());
            }

            LOG.info("Completed.");
        }
    }
}

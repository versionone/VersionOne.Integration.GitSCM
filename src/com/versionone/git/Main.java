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
        } catch(GitException ex) {
            fail();
        } catch (VersionOneException ex) {
            fail();
        }

        while(true) {
            /* do nothing, the job is done in background thread */
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                fail();
            }
        }
    }

    public static void fail(){
        System.exit(-1);
    }

    private static class GitPollTask extends TimerTask {

        private final GitService service;

        GitPollTask(Configuration configuration) throws GitException, VersionOneException {
            LOG.info("Creating service...");
            IDbStorage storage = new DbStorage();
            Configuration.GitSettings gitSettings = configuration.getGitSettings();
            IGitConnector gitConnector = new GitConnector(gitSettings.getPassword(), gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(), gitSettings.getWatchedBranch(), gitSettings.getLocalDirectory(),
                configuration.getReferenceExpression());
            IChangeSetWriter changeSetWriter = new ChangeSetWriter(configuration);
            service = new GitService(configuration, storage, gitConnector, changeSetWriter);
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
            } catch (VersionOneException ex) {
                System.out.println("Fail: " + ex.getInnerException().getMessage());
            }

            LOG.info("Completed.");
        }
    }
}

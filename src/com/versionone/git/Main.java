package com.versionone.git;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private final static Timer timer = new Timer();
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public static void main(String[] arg) {
        // TODO parse configuration
        LOG.info("Loading config..");
        Configuration configuration = new Configuration();
        LOG.info("Configuration loaded..");

        try {
            timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getTimeoutMillis());
        } catch(ConnectorException e) {
            System.exit(-1);
        }

        while(true) { /* do nothing, the job is done in background thread */ }
    }

    private static class GitPollTask extends TimerTask {

        private final GitService service;

        GitPollTask(Configuration configuration) throws ConnectorException {
            LOG.info("Creating service...");
            service = new GitService(configuration);
            LOG.info("Service created.");
        }

        @Override
        public void run() {
            LOG.info("Running processing..");
            try {
                service.onInterval();
            } catch(ConnectorException e) {
                System.out.println("Fail: " + e.getInnerException().getMessage());
            }
            LOG.info("Processing completed.");
        }
    }
}

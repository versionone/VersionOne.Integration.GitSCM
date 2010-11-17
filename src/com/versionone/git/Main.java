package com.versionone.git;

import java.util.Timer;
import java.util.TimerTask;

public class Main {

    private final static Timer timer = new Timer();

    public static void main(String[] arg) {
        // TODO parse configuration
        Configuration configuration = new Configuration();

        timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getTimeoutMillis());

        while(true) { /* do nothing, the job is done in background thread */ }
    }

    private static class GitPollTask extends TimerTask {

        private final GitService service;

        GitPollTask(Configuration configuration) {
            service = new GitService(configuration);
        }

        @Override
        public void run() {
            service.onInterval();
        }
    }
}

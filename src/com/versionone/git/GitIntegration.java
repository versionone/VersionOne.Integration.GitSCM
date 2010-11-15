package com.versionone.git;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Main Class for integration with Git
 */
public class GitIntegration {

    private final static Timer timer = new Timer();

    public static void main(String[] arg) {
        // Timer runs all tasks in a single background thread, so its behaviour is similar to .NET events
        timer.scheduleAtFixedRate(new GitPollTask(), 0, 3000);

        while(true) { /* do nothing, the job is done in background thread */ }
    }

    private static class GitPollTask extends TimerTask {

        @Override
        public void run() {
            try {
                Thread.sleep(4000);
                System.out.println("hit next interval");
            } catch(InterruptedException ex) {
                // do nothing
            }
        }
    }
}

package com.versionone.git;

import org.apache.log4j.Logger;

public class Main {
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public static void main(String[] arg) {
        ServiceHandler.start(arg);

        while(true) {
            /* do nothing, the job is done in background thread */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                LOG.fatal("Closing application due to internal error.");
                System.exit(-1);
            }
        }
    }
}

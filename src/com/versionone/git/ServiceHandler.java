package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import org.apache.log4j.Logger;

import java.util.Timer;

public class ServiceHandler {
    private static final Timer timer = new Timer();
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public static void start(String[] arg) {
        LOG.info("Git integration service is starting.");
        LOG.info("Loading configuration...");
        Configuration configuration = Configuration.getInstance();
        LOG.info("Configuration loaded.");

        try {
            timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getTimeoutMillis());
        } catch(GitException ex) {
            fail();
        } catch (VersionOneException ex) {
            fail();
        }
    }

    public static void fail(){
        LOG.fatal("Closing application due to internal error.");
        System.exit(-1);
    }

    public static void stop(String[] arg){
        timer.cancel();
    }
}

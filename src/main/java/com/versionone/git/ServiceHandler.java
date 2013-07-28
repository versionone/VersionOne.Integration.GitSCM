package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import org.apache.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.util.Timer;

public class ServiceHandler {
    private static final Timer timer = new Timer();
    private static final Logger LOG = Logger.getLogger("GitIntegration");

    public static void start(String[] arg) {
        LOG.info("Git integration service is starting...");

        Configuration configuration = Configuration.getInstance();

        try {
            timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getPollIntervalInSeconds() * 1000);
        } catch (VersionOneException ex) {
            if (ex.getInnerException() != null)
                LOG.fatal(ex.getInnerException().getMessage() + ex.getInnerException().getStackTrace());
            fail(ex);
        } catch (NoSuchAlgorithmException ex) {
            fail(ex);
        }
    }

    public static void fail(Exception ex){
        LOG.fatal("Closing application due to internal error:");
        LOG.fatal(ex.getMessage() + ex.getStackTrace());
        System.exit(-1);
    }

    public static void stop(String[] arg){
        timer.cancel();
    }

    public static void main(String[] arg) {
        LOG.info("Git integration service is starting...");

        Configuration configuration = Configuration.getInstance();

        try {
            timer.scheduleAtFixedRate(new GitPollTask(configuration), 0, configuration.getPollIntervalInSeconds() * 1000);
        } catch (VersionOneException ex) {
            if (ex.getInnerException() != null)
                LOG.fatal(ex.getInnerException().getMessage() + ex.getInnerException().getStackTrace());
            fail(ex);
        } catch (NoSuchAlgorithmException ex) {
            fail(ex);
        }
    }

}

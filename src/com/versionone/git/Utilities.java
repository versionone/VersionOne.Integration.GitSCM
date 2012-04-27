package com.versionone.git;

import com.versionone.git.configuration.GitSettings;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utilities {
    static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (String child : dir.list()) {
                boolean success = deleteDirectory(new File(dir, child));

                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static String getRepositoryId(GitSettings gitSettings) throws NoSuchAlgorithmException {
        StringBuffer sb = new StringBuffer();
        sb.append(gitSettings.getRepositoryPath()).
                append(gitSettings.getPassphrase()).
                append(gitSettings.getPassword()).
                append(gitSettings.getWatchedBranch()).
                append(gitSettings.getUseBranchName());

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sb.toString().getBytes());
        BigInteger hash = new BigInteger(1, md.digest());
        return hash.toString(16);
    }
}

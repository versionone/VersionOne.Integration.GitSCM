package com.versionone.git;

import com.versionone.git.configuration.GitConnection;

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

    public static String getRepositoryId(GitConnection gitConnection) throws NoSuchAlgorithmException {
        StringBuffer sb = new StringBuffer();
        sb.append(gitConnection.getRepositoryPath()).
                append(gitConnection.getPassphrase()).
                append(gitConnection.getPassword()).
                append(gitConnection.getWatchedBranch()).
                append(gitConnection.getUseBranchName());

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(sb.toString().getBytes());
        BigInteger hash = new BigInteger(1, md.digest());
        return hash.toString(16);
    }
}

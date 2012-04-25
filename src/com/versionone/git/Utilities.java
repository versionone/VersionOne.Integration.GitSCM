package com.versionone.git;

import java.io.File;


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
}

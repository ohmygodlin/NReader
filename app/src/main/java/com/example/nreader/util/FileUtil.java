package com.example.nreader.util;

import java.io.File;

public class FileUtil {

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles())
                deleteDir(f);
        }
        dir.delete();
    }

}

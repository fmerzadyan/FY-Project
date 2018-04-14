package com.merzadyan;

import java.io.File;

public class FileOp {
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
    
    public static boolean isFile(String path) {
        return new File(path).isFile();
    }
    
    public static boolean isEmptyFile(String path) {
        return isFile(path) && new File(path).length() <= 0;
    }
}
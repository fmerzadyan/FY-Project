package com.merzadyan;

import java.io.File;

public class Common {
    public static boolean isNullOrEmptyString(String string) {
        return string == null || string.isEmpty();
    }
    
    public static boolean isFile(String filePath) {
        return new File(filePath).isFile();
    }
    
    public static boolean isEmptyFile(String filePath) {
        return isFile(filePath) && new File(filePath).length() <= 0;
    }
}
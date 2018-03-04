package com.merzadyan;

import java.io.File;

public class CommonOp {
    public static boolean isNotNullAndNotEmpty(String string) {
        return string != null && !string.isEmpty();
    }
    
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return !file.isDirectory() && file.exists();
    }
}
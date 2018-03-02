package com.merzadyan;

import java.io.File;

public class FileOp {
    public static boolean isNotNullAndNotEmpty(String path) {
        return path != null && !path.isEmpty();
    }
    
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return !file.isDirectory() && file.exists();
    }
}
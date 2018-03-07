package com.lyk.jsbridge.photopick;

import java.io.Serializable;

/**
 * Created by tian on 15-9-9.
 */
public class ImageInfo implements Serializable{

    public String path;
    public long photoId;
    public int width;
    public int height;
    private static final String prefix = "file://";

    public ImageInfo(String path) {
        this.path = pathAddPreFix(path);
    }

    public static String pathAddPreFix(String path) {
        if (!path.startsWith(prefix)) {
            path = prefix + path;
        }
        return path;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "path='" + path + '\'' +
                ", photoId=" + photoId +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}

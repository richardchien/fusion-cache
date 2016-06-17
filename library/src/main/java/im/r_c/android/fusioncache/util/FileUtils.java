/*
 * Copyright (c) 2016 Richard Chien
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package im.r_c.android.fusioncache.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * FusionCache
 * Created by richard on 6/15/16.
 *
 * @author Richard Chien
 */

public class FileUtils {
    /**
     * Recursively delete a file or a directory.
     *
     * @param file File or directory to delete.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFile(File file) {
        if (!file.isDirectory()) {
            file.delete();
            return;
        }

        // The file is a directory
        for (File f : file.listFiles()) {
            deleteFile(f);
        }
    }

    /**
     * Get the system's default cache directory.
     *
     * Note: Only when the SD card is mounted and it's not removable
     * will this method returns the external cache dir in /sdcard/Android/data,
     * otherwise it returns the inner cache dir in /data/data.
     *
     * @param cacheDirName Name of subdirectory in the root cache dir.
     * @return The file object refers to the needed cache dir.
     */
    public static File getDiskCacheDir(Context context, String cacheDirName) {
        File rootCacheDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.isExternalStorageRemovable()) {
            rootCacheDir = context.getExternalCacheDir();
        } else {
            rootCacheDir = context.getCacheDir();
        }
        return new File(rootCacheDir, cacheDirName);
    }
}

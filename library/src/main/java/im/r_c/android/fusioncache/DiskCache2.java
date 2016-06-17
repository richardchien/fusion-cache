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

package im.r_c.android.fusioncache;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import im.r_c.android.fusioncache.util.BitmapUtils;
import im.r_c.android.fusioncache.util.FileUtils;

/**
 * FusionCache
 * Created by richard on 6/17/16.
 * <p>
 * This is a modified version of {@link DiskCache},
 * using {@code DiskLruCache} instead of manipulating cache files
 * manually.
 * <p>
 * A thread-safe class that provides disk cache functions.
 * <p>
 * Methods of this class may block while doing IO things.
 *
 * @author Richard Chien
 */
public class DiskCache2 extends AbstractCache {

    /**
     * The directory that stores cache files.
     * <p>
     * Typically a sub directory inside the app's cache dir.
     */
    private File mCacheDir;

    private DiskLruCache mDiskLruCache;

    public DiskCache2(File cacheDir, long maxCacheSize) {
        if (cacheDir.exists() && cacheDir.isFile()) {
            throw new IllegalArgumentException("cacheDir is not a directory.");
        }

        try {
            mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, maxCacheSize);
        } catch (IOException e) {
            throw new RuntimeException("Open DiskLruCache failed.");
        }

        mCacheDir = cacheDir;
    }

    @Override
    public void put(String key, String value) {
        put(key, value.getBytes());
    }

    @Override
    public void put(String key, JSONObject value) {
        put(key, value.toString());
    }

    @Override
    public void put(String key, JSONArray value) {
        put(key, value.toString());
    }

    /**
     * The ultimate {@code put} method.
     * <p>
     * Any other {@code put} methods will finally call this one
     * to actually store byte array into the disk.
     */
    @Override
    public synchronized void put(String key, byte[] value) {
        int valueSize = value.length;
        if (valueSize > maxSize()) {
            // Value size is bigger than max cache size
            return;
        }

        // Get the hash value of the key
        // Never use the parameter "key" below
        String hashKey = hashKeyForDisk(key);

        DiskLruCache.Editor editor = null;
        OutputStream out = null;
        try {
            editor = mDiskLruCache.edit(hashKey);
            out = editor.newOutputStream(0);
            out.write(value);
            out.flush();
            editor.commit();
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException ignored) {
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void put(String key, Bitmap value) {
        put(key, BitmapUtils.bitmapToBytes(value));
    }

    @Override
    public void put(String key, Drawable value) {
        put(key, BitmapUtils.drawableToBitmap(value));
    }

    @Override
    public void put(String key, Serializable value) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            byte[] byteArray = baos.toByteArray();
            put(key, byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public String getString(String key) {
        byte[] byteArray = getBytes(key);
        if (byteArray == null) {
            return null;
        }
        return new String(byteArray);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        String jsonString = getString(key);
        if (jsonString == null) {
            return null;
        }
        JSONObject result = null;
        try {
            result = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public JSONArray getJSONArray(String key) {
        String jsonString = getString(key);
        if (jsonString == null) {
            return null;
        }
        JSONArray result = null;
        try {
            result = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * The ultimate {@code get} method.
     * <p>
     * Any other {@code get} methods will first call this one
     * to get the primitive byte array and then convert to the type needed.
     */
    @Override
    public synchronized byte[] getBytes(String key) {
        // Get the hash value of the key
        // Never use the parameter "key" below
        String hashKey = hashKeyForDisk(key);

        DiskLruCache.Snapshot snapshot = null;
        InputStream in = null;
        byte[] byteArray = null;
        try {
            snapshot = mDiskLruCache.get(hashKey);
            if (snapshot != null) {
                in = snapshot.getInputStream(0);
                byteArray = new byte[(int) FileUtils.getFileSize((FileInputStream) in)];
                if (in.read(byteArray) < 0) {
                    // Didn't read anything actually
                    byteArray = null;
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return byteArray;
    }

    @Override
    public Bitmap getBitmap(String key) {
        return BitmapUtils.bytesToBitmap(getBytes(key));
    }

    /**
     * @deprecated Use {@code getDrawable(String, Resources)} instead.
     */
    @Deprecated
    @Override
    public Drawable getDrawable(String key) {
        return BitmapUtils.bitmapToDrawable(getBitmap(key), null);
    }

    public Drawable getDrawable(String key, Resources res) {
        return BitmapUtils.bitmapToDrawable(getBitmap(key), res);
    }

    @Override
    public Serializable getSerializable(String key) {
        byte[] byteArray = getBytes(key);
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        Object result = null;
        try {
            bais = new ByteArrayInputStream(byteArray);
            ois = new ObjectInputStream(bais);
            result = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException ignored) {
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (result == null || !(result instanceof Serializable)) {
            return null;
        } else {
            return (Serializable) result;
        }
    }

    /**
     * Always returns null, because for disk cache,
     * any action that gets a value must specify the type of the value.
     */
    @Override
    public synchronized Object remove(String key) {
        // Get the hash value of the key
        // Never use the parameter "key" below
        String hashKey = hashKeyForDisk(key);

        try {
            mDiskLruCache.remove(hashKey);
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void clear() {
        FileUtils.deleteFile(mCacheDir);
    }

    @Override
    public synchronized long size() {
        return mDiskLruCache.size();
    }

    @Override
    public synchronized long maxSize() {
        return mDiskLruCache.maxSize();
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash
     * suitable for using as a disk filename.
     */
    private static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * Convert bytes to hex string,
     * working for {@link #hashKeyForDisk(String)}
     */
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}

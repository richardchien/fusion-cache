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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.r_c.android.fusioncache.util.BitmapUtils;
import im.r_c.android.fusioncache.util.FileUtils;

/**
 * FusionCache
 * Created by richard on 6/12/16.
 * <p>
 * A thread-safe class that provides disk cache functions.
 * <p>
 * Methods of this class may block while doing IO things.
 *
 * @author Richard Chien
 */
public class DiskCache extends AbstractCache {

    /**
     * Pretend to store the key-value entry in a LRU cache.
     * The actual objects are in the disk in fact.
     * <p>
     * Keys in this cache wrapper are all hashed keys,
     * same as the cache file name.
     */
    private LruCacheWrapper<String, ValueWrapper> mCacheWrapper;

    /**
     * The directory that stores cache files.
     * <p>
     * Typically a sub directory inside the app's cache dir.
     */
    private File mCacheDir;

    public DiskCache(File cacheDir, int maxCacheSize) {
        if (cacheDir.exists() && cacheDir.isFile()) {
            throw new IllegalArgumentException("cacheDir is not a directory.");
        } else if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                // Failed to make dirs
                throw new RuntimeException("Cannot create cache directory.");
            }
        }

        mCacheDir = cacheDir;
        mCacheWrapper = new LruCacheWrapper<>(maxCacheSize, new LruCacheDelegate(mCacheDir));

        // Try to restore journal, aka the state of mCacheWrapper when last used
        List<LruCacheWrapper.Entry<String, ValueWrapper>> entryList = restoreJournal();
        if (entryList != null && entryList.size() > 0) {
            for (LruCacheWrapper.Entry<String, ValueWrapper> entry : entryList) {
                // Sizes of entries in the restore list are all fit current max cache size
                // so just put it in
                mCacheWrapper.put(entry.key, entry.value);
            }
        }
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

        File file = new File(mCacheDir, hashKey);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(value);
            fos.flush();
            mCacheWrapper.put(hashKey, new ValueWrapper(valueSize));

            // Save journal file
            saveJournal();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
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

        File file = new File(mCacheDir, hashKey);
        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        byte[] byteArray = new byte[(int) file.length()];
        try {
            fis = new FileInputStream(file);
            if (fis.read(byteArray) < 0) {
                // Didn't read anything actually
                byteArray = null;
            }
            mCacheWrapper.get(hashKey);

            // Save journal file
            saveJournal();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {
                }
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

        File file = new File(mCacheDir, hashKey);
        //noinspection ResultOfMethodCallIgnored
        file.delete();

        mCacheWrapper.remove(hashKey);

        // Save journal file
        saveJournal();

        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void clear() {
        FileUtils.deleteFile(mCacheDir);
    }

    @Override
    public synchronized int size() {
        return mCacheWrapper.size();
    }

    @Override
    public synchronized int maxSize() {
        return mCacheWrapper.maxSize();
    }

    synchronized Map<String, ValueWrapper> snapshot() {
        return mCacheWrapper.snapshot();
    }

    /**
     * Save snapshot to journal file.
     */
    synchronized void saveJournal() {
        final Map<String, ValueWrapper> snapshot = snapshot();
        File file = getJournalFile();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
            for (String hashKey : snapshot.keySet()) {
                pw.println(hashKey);
            }
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * Restore from journal file if exists.
     * <p>
     * Typically called in constructor.
     * <p>
     * Note: Only restore caches that fit current max cache size,
     * which means the ones whose sizes are bigger than current max size will be skipped,
     * and the corresponding file will be deleted.
     *
     * @return List of entries.
     */
    synchronized List<LruCacheWrapper.Entry<String, ValueWrapper>> restoreJournal() {
        File journalFile = getJournalFile();
        if (!journalFile.exists()) {
            return null;
        }

        List<LruCacheWrapper.Entry<String, ValueWrapper>> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(journalFile));
            String hashKey;
            while ((hashKey = br.readLine()) != null) {
                File cacheFile = new File(mCacheDir, hashKey);
                if (!cacheFile.exists()) {
                    continue;
                } else if (cacheFile.length() > maxSize()) {
                    // The cache file does not fit in the current cache
                    // so delete it
                    //noinspection ResultOfMethodCallIgnored
                    cacheFile.delete();
                    continue;
                }
                list.add(new LruCacheWrapper.Entry<>(hashKey, new ValueWrapper((int) cacheFile.length())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }

        return list;
    }

    /**
     * Returns a {@code File} object refers to the journal file.
     */
    private File getJournalFile() {
        //noinspection SpellCheckingInspection
        return new File(mCacheDir, ".fusioncache.journal");
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
     * working for {@link #hashKeyForDisk(java.lang.String)}
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

    /**
     * A value wrapper for disk cache items,
     * used to keep sizes of cache files.
     */
    static class ValueWrapper {
        int size;

        public ValueWrapper(int size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return "ValueWrapper{" +
                    "size=" + size +
                    '}';
        }
    }

    /**
     * Implements some delegate methods of {@code LruCache}.
     */
    private static class LruCacheDelegate implements LruCacheWrapper.Delegate<String, ValueWrapper> {
        private File mCacheDir;

        public LruCacheDelegate(File cacheDir) {
            mCacheDir = cacheDir;
        }

        @Override
        public int sizeOf(String key, ValueWrapper valueWrapper) {
            return valueWrapper.size;
        }

        @Override
        public void entryRemoved(boolean evicted, String hashKey, ValueWrapper oldValue, ValueWrapper newValue) {
            if (evicted) {
                File file = new File(mCacheDir, hashKey);
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }
}

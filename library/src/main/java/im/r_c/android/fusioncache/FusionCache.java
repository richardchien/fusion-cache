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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.r_c.android.fusioncache.util.MemoryUtils;

/**
 * FusionCache
 * Created by richard on 6/12/16.
 * <p/>
 * A cache class that mixes memory cache and disk cache,
 * and intelligently caches things into memory or disk,
 * and even moves cache items from one to the other automatically.
 * <p/>
 * It also can be used as separate memory and disk caches.
 * <p/>
 * This class is thread-safe.
 *
 * @author Richard Chien
 */
public class FusionCache extends AbstractCache {
    private static final String LOG_TAG = "FusionCache";
    private static final boolean DEBUG = false;

    private static final String DEFAULT_DISK_CACHE_DIR_NAME = "FusionCache";

    private Context mAppContext;
    private MemCache mMemCache;
    private DiskCache mDiskCache;
    private boolean mFusionModeEnabled;

    public FusionCache(Context context, int maxMemCacheSize, int maxDiskCacheSize) {
        this(context, maxMemCacheSize, maxDiskCacheSize, DEFAULT_DISK_CACHE_DIR_NAME, true);
    }

    public FusionCache(Context context, int maxMemCacheSize, int maxDiskCacheSize, boolean enableFusionMode) {
        this(context, maxMemCacheSize, maxDiskCacheSize, DEFAULT_DISK_CACHE_DIR_NAME, enableFusionMode);
    }

    public FusionCache(Context context, int maxMemCacheSize, int maxDiskCacheSize, String diskCacheSizeName) {
        this(context, maxMemCacheSize, maxDiskCacheSize, diskCacheSizeName, true);
    }

    public FusionCache(Context context, int maxMemCacheSize, int maxDiskCacheSize, String diskCacheDirName, boolean enableFusionMode) {
        if (maxMemCacheSize < 0 || maxDiskCacheSize < 0) {
            throw new IllegalArgumentException("Max cache size should be non-negative.");
        }

        mAppContext = context.getApplicationContext();
        mFusionModeEnabled = enableFusionMode;

        if (maxMemCacheSize > 0) {
            mMemCache = new MemCache(maxMemCacheSize);
        }
        if (maxDiskCacheSize > 0) {
            mDiskCache = new DiskCache(new File(mAppContext.getCacheDir(), diskCacheDirName), maxDiskCacheSize);
        }
    }

    /**
     * Returns the {@link #mMemCache},
     * or null if max memory cache size <= 0.
     */
    public MemCache getMemCache() {
        return mMemCache;
    }

    /**
     * Returns the {@link #mDiskCache},
     * or null if max disk cache size <= 0.
     */
    public DiskCache getDiskCache() {
        return mDiskCache;
    }

    @Override
    public void put(String key, String value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, JSONObject value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, JSONArray value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, byte[] value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, Bitmap value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, Drawable value) {
        putInternal(key, value);
    }

    @Override
    public void put(String key, Serializable value) {
        putInternal(key, value);
    }

    @Override
    public String getString(String key) {
        return getInternal(key, String.class);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return getInternal(key, JSONObject.class);
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return getInternal(key, JSONArray.class);
    }

    @Override
    public byte[] getBytes(String key) {
        return getInternal(key, byte[].class);
    }

    @Override
    public Bitmap getBitmap(String key) {
        return getInternal(key, Bitmap.class);
    }

    @Override
    public Drawable getDrawable(String key) {
        return getInternal(key, Drawable.class);
    }

    @Override
    public Serializable getSerializable(String key) {
        return getInternal(key, Serializable.class);
    }

    @Override
    public synchronized Object remove(String key) {
        Object result = null;
        if (mMemCache != null) {
            result = mMemCache.remove(key);
        }
        if (mDiskCache != null) {
            mDiskCache.remove(key);
        }
        return result;
    }

    @Override
    public synchronized void clear() {
        if (mMemCache != null) {
            mMemCache.clear();
        }
        if (mDiskCache != null) {
            mDiskCache.clear();
        }
    }

    @Override
    public synchronized int size() {
        return memCacheSize() + diskCacheSize();
    }

    @Override
    public synchronized int maxSize() {
        return maxMemCacheSize() + maxDiskCacheSize();
    }

    /**
     * Save all caches in {@link #mMemCache} into {@link #mDiskCache}.
     * <p/>
     * Won't change anything in {@link #mMemCache}.
     */
    public synchronized void saveMemCacheToDisk() {
        if (mMemCache != null && mDiskCache != null) {
            // We got both mMemCache and mDiskCache here
            Map<String, MemCache.ValueWrapper> memCacheSnapshot = mMemCache.snapshot();
            for (Map.Entry<String, MemCache.ValueWrapper> entry : memCacheSnapshot.entrySet()) {
                putInDiskLocked(entry.getKey(), entry.getValue().obj);
            }
        }
    }

    /**
     * Returns the current used size of the {@link #mMemCache},
     * or 0 if {@link #mMemCache} is null.
     */
    public synchronized int memCacheSize() {
        if (mMemCache != null) {
            return mMemCache.size();
        }
        return 0;
    }

    /**
     * Returns the max size of memory cache,
     * or 0 if {@link #mMemCache} is null.
     */
    public synchronized int maxMemCacheSize() {
        if (mMemCache != null) {
            return mMemCache.maxSize();
        }
        return 0;
    }

    /**
     * Returns the current used size of the {@link #mDiskCache},
     * or 0 if {@link #mDiskCache} is null.
     */
    public synchronized int diskCacheSize() {
        if (mDiskCache != null) {
            return mDiskCache.size();
        }
        return 0;
    }

    /**
     * Returns the max size of disk cache,
     * or 0 if {@link #mDiskCache} is null.
     */
    public synchronized int maxDiskCacheSize() {
        if (mDiskCache != null) {
            return mDiskCache.maxSize();
        }
        return 0;
    }

    /**
     * The internal {@code put} method,
     * called by every public {@code put} method.
     */
    private void putInternal(String key, Object value) {
        checkFusionMode();

        synchronized (this) {
            if (mMemCache != null && MemoryUtils.sizeOf(value) <= maxMemCacheSize()) {
                List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = putInMemLocked(key, value);
                if (mDiskCache != null) {
                    for (LruCacheWrapper.Entry<String, MemCache.ValueWrapper> entry : evictedList) {
                        putInDiskLocked(entry.key, entry.value.obj);
                    }
                }
            } else if (mDiskCache != null) {
                putInDiskLocked(key, value);
            }
        }
    }

    /**
     * The internal {@code get} method,
     * called by every public {@code get} method.
     */
    private <T> T getInternal(String key, Class<T> clz) {
        checkFusionMode();

        T result;

        synchronized (this) {
            if (mMemCache != null) {
                result = getFromMemLocked(key, clz);
                if (result != null) {
                    // Got in memory cache
                    return result;
                }
            }

            if (mDiskCache != null) {
                result = getFromDiskLocked(key, clz);
                if (result != null) {
                    // Got in disk cache
                    if (mMemCache != null) {
                        putInMemLocked(key, result);
                    }
                    return result;
                }
            }
        }

        // Got nothing
        return null;
    }

    /**
     * Put value into memory cache.
     * <p/>
     * Only called when {@link #mMemCache} is not null.
     */
    private List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> putInMemLocked(String key, Object value) {
        // Already know mMemCache != null here
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        if (DEBUG) {
            Log.d(LOG_TAG, "putInMemLocked: {" + key + ": " + value + "}, " + "evictedList: " + evictedList);
        }
        return evictedList;
    }

    /**
     * Put value into disk cache.
     * <p/>
     * Only called when {@link #mDiskCache} is not null.
     */
    private void putInDiskLocked(String key, Object value) {
        // Already know mDiskCache != null here
        if (value instanceof String) {
            mDiskCache.put(key, (String) value);
        } else if (value instanceof JSONObject) {
            mDiskCache.put(key, (JSONObject) value);
        } else if (value instanceof JSONArray) {
            mDiskCache.put(key, (JSONArray) value);
        } else if (value instanceof byte[]) {
            mDiskCache.put(key, (byte[]) value);
        } else if (value instanceof Bitmap) {
            mDiskCache.put(key, (Bitmap) value);
        } else if (value instanceof Drawable) {
            mDiskCache.put(key, (Drawable) value);
        } else if (value instanceof Serializable) {
            mDiskCache.put(key, (Serializable) value);
        }
    }

    /**
     * Get value from memory cache.
     * <p/>
     * Only called when {@link #mMemCache} is not null.
     */
    private <T> T getFromMemLocked(String key, Class<T> clz) {
        // Already know mMemCache != null here
        return mMemCache.get(key, clz);
    }

    /**
     * Get value from disk cache.
     * <p/>
     * Only called when {@link #mDiskCache} is not null.
     */
    private <T> T getFromDiskLocked(String key, Class<T> clz) {
        // Already know mDiskCache != null here
        if (clz == String.class) {
            return clz.cast(mDiskCache.getString(key));
        } else if (clz == JSONObject.class) {
            return clz.cast(mDiskCache.getJSONObject(key));
        } else if (clz == JSONArray.class) {
            return clz.cast(mDiskCache.getJSONArray(key));
        } else if (clz == byte[].class) {
            return clz.cast(mDiskCache.getBytes(key));
        } else if (clz == Bitmap.class) {
            return clz.cast(mDiskCache.getBitmap(key));
        } else if (clz == Drawable.class) {
            return clz.cast(mDiskCache.getDrawable(key, mAppContext.getResources()));
        } else if (clz == Serializable.class) {
            return clz.cast(mDiskCache.getSerializable(key));
        }
        return null;
    }

    /**
     * Check if fusion mode is enabled
     */
    private void checkFusionMode() {
        if (!mFusionModeEnabled) {
            // Fusion mode not enabled, so throw exception
            throw new IllegalStateException("Fusion mode is not enabled.");
        }
    }
}

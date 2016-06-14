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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * FusionCache
 * Created by richard on 6/12/16.
 * <p/>
 * A cache class that mixes memory cache and disk cache,
 * and intelligently cache things into memory or disk.
 */
public class FusionCache extends AbstractCache {
    private static final String LOG_TAG = "FusionCache";

    private static final String DEFAULT_DISK_CACHE_DIR_NAME = "FusionCache";

    private Context mAppContext;
    private MemCache mMemCache;
    private DiskCache mDiskCache;
    private int mMaxMemCacheSize;
    private int mMaxDiskCacheSize;
    private String mDiskCacheDirName;
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
        mMaxMemCacheSize = maxMemCacheSize;
        mMaxDiskCacheSize = maxDiskCacheSize;
        mDiskCacheDirName = diskCacheDirName;
        mFusionModeEnabled = enableFusionMode;

        if (mMaxMemCacheSize > 0) {
            mMemCache = new MemCache(mMaxMemCacheSize);
        }
    }

    public MemCache getMemCache() {
        return mMemCache;
    }

    public DiskCache getDiskCache() {
        return mDiskCache;
    }

    @Override
    public void put(String key, String value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, JSONObject value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, JSONArray value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, byte[] value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, Bitmap value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, Drawable value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public void put(String key, Serializable value) {
        checkFusionMode();
        List<LruCacheWrapper.Entry<String, MemCache.ValueWrapper>> evictedList = new ArrayList<>();
        mMemCache.put(key, value, evictedList);
        Log.d(LOG_TAG, "evictedList: " + evictedList);
    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return null;
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return null;
    }

    @Override
    public byte[] getBytes(String key) {
        return new byte[0];
    }

    @Override
    public Bitmap getBitmap(String key) {
        return null;
    }

    @Override
    public Drawable getDrawable(String key) {
        return null;
    }

    @Override
    public Serializable getSerializable(String key) {
        return null;
    }

    @Override
    public Object remove(String key) {
        return mMemCache.remove(key);
    }

    @Override
    public int size() {
        return memCacheSize() + diskCacheSize();
    }

    @Override
    public int maxSize() {
        return maxMemCacheSize() + maxDiskCacheSize();
    }

    public int memCacheSize() {
        return mMemCache.size();
    }

    public int maxMemCacheSize() {
        return mMaxMemCacheSize;
    }

    public int diskCacheSize() {
        return mDiskCache.size();
    }

    public int maxDiskCacheSize() {
        return mDiskCache.maxSize();
    }

    private void checkFusionMode() {
        if (!mFusionModeEnabled) {
            // Fusion mode not enabled, so throw exception
            throw new IllegalStateException("Fusion mode is not enabled.");
        }
    }
}

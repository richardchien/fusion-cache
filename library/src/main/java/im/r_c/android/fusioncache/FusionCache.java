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

/**
 * FusionCache
 * Created by richard on 6/12/16.
 */
public class FusionCache implements KeyValueCache {
    private boolean mFusionModeEnabled;
    private MemCache mMemCache;
    private DiskCache mDiskCache;

    public FusionCache() {
        this(true);
    }

    public FusionCache(boolean enableFusionMode) {
        mFusionModeEnabled = enableFusionMode;
    }

    @Override
    public void put(String key, Object object) {
        if (!mFusionModeEnabled) {
            // Fusion mode not enabled, so throw exception
            throw new RuntimeException("Fusion mode is not enabled, so put() method can't be used.");
        }
    }

    @Override
    public Object get(String key) {
        if (!mFusionModeEnabled) {
            // Fusion mode not enabled, so throw exception
            throw new RuntimeException("Fusion mode is not enabled, so get() method can't be used.");
        }
        return null;
    }

    void putInMem(String key, Object object) {
        mMemCache.put(key, object);
    }

    Object getFromMem(String key) {
        return mMemCache.get(key);
    }

    void putInDisk(String key, Object object) {
        mDiskCache.put(key, object);
    }

    Object getFromDisk(String key) {
        return mDiskCache.get(key);
    }
}

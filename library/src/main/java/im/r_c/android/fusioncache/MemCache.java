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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * FusionCache
 * Created by richard on 6/12/16.
 */
public class MemCache extends AbstractCache {
    private LruCacheWrapper<String, Object> mCacheWrapper;

    public MemCache(int maxCacheSize) {
        mCacheWrapper = new LruCacheWrapper<String, Object>(maxCacheSize) {
            @Override
            protected int sizeOf(String key, Object value) {
                return MemoryUtils.sizeOf(value);
            }
        };
    }

    @Override
    public void put(String key, String value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, JSONObject value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, JSONArray value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, byte[] value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, Bitmap value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, Drawable value) {
        putObject(key, value);
    }

    @Override
    public void put(String key, Serializable value) {
        putObject(key, value);
    }

    void putObject(String key, Object value) {
        if (MemoryUtils.sizeOf(value) <= maxSize()) {
            mCacheWrapper.put(key, value);
        }
    }

    @Override
    public Object get(String key) {
        return mCacheWrapper.get(key);
    }

    @Override
    public Object remove(String key) {
        return mCacheWrapper.remove(key);
    }

    public int size() {
        return mCacheWrapper.size();
    }

    public int maxSize() {
        return mCacheWrapper.maxSize();
    }

    public Map<String, Object> snapshot() {
        return mCacheWrapper.snapshot();
    }

    Object put(String key, Object value, List<LruCacheWrapper.Entry<String, Object>> evictedEntryList) {
        return mCacheWrapper.put(key, value, evictedEntryList);
    }
}

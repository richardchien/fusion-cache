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

import im.r_c.android.fusioncache.util.MemoryUtils;

/**
 * FusionCache
 * Created by richard on 6/12/16.
 * <p>
 * A thread-safe class that provides memory cache functions.
 *
 * @author Richard Chien
 */
public class MemCache extends AbstractCache {

    /**
     * A {@code LruCache} wrapper.
     * <p>
     * Keeps strong references to objects so that
     * the objects can be cached in memory.
     */
    private LruCacheWrapper<String, ValueWrapper> mCacheWrapper;

    public MemCache(int maxCacheSize) {
        mCacheWrapper = new LruCacheWrapper<>(maxCacheSize, new LruCacheDelegate());
    }

    @Override
    public void put(String key, String value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, JSONObject value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, JSONArray value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, byte[] value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, Bitmap value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, Drawable value) {
        put(key, value, null);
    }

    @Override
    public void put(String key, Serializable value) {
        put(key, value, null);
    }

    public Object get(String key) {
        return get(key, Object.class);
    }

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return get(key, JSONObject.class);
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return get(key, JSONArray.class);
    }

    @Override
    public byte[] getBytes(String key) {
        return get(key, byte[].class);
    }

    @Override
    public Bitmap getBitmap(String key) {
        return get(key, Bitmap.class);
    }

    @Override
    public Drawable getDrawable(String key) {
        return get(key, Drawable.class);
    }

    @Override
    public Serializable getSerializable(String key) {
        return get(key, Serializable.class);
    }

    @Override
    public synchronized Object remove(String key) {
        return mCacheWrapper.remove(key);
    }

    @Override
    public synchronized void clear() {
        mCacheWrapper.evictAll();
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
     * Special put method, marking evicted entries.
     * It's used by FusionCache to decide which cache items
     * should be moved to disk cache.
     * <p>
     * Only used in this package.
     *
     * @param evictedEntryList A list used to store evicted entries.
     * @return The previous value mapped by {@code key}.
     */
    synchronized Object put(String key, Object value, List<LruCacheWrapper.Entry<String, ValueWrapper>> evictedEntryList) {
        int size = MemoryUtils.sizeOf(value);
        if (size <= maxSize()) {
            return mCacheWrapper.put(key, new ValueWrapper(value, size), evictedEntryList);
        }
        return null;
    }

    /**
     * Special get method.
     * Get value by class passed in.
     * <p>
     * Only used in this package.
     */
    synchronized <T> T get(String key, Class<T> clz) {
        ValueWrapper wrapper = mCacheWrapper.get(key);
        if (wrapper == null || !clz.isInstance(wrapper.obj)) {
            return null;
        }
        return clz.cast(wrapper.obj);
    }

    /**
     * A value wrapper for memory cache items,
     * used to keep strong references to objects
     * and sizes of objects.
     */
    static class ValueWrapper {
        Object obj;
        int size;

        public ValueWrapper(Object obj, int size) {
            this.obj = obj;
            this.size = size;
        }

        @Override
        public String toString() {
            return "ValueWrapper{" +
                    "obj=" + obj +
                    ", size=" + size +
                    '}';
        }
    }

    /**
     * Implements some delegate methods of {@code LruCache}.
     */
    private static class LruCacheDelegate implements LruCacheWrapper.Delegate<String, ValueWrapper> {

        @Override
        public int sizeOf(String key, ValueWrapper valueWrapper) {
            return valueWrapper.size;
        }

        @Override
        public void entryRemoved(boolean evicted, String key, ValueWrapper oldValue, ValueWrapper newValue) {
        }
    }
}

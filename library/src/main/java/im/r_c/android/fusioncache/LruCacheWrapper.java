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

import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FusionCache
 * Created by richard on 6/13/16.
 * <p/>
 * A wrapper class of {@link ExtendedLruCache} which is a subclass
 * of {@link LruCache}, providing a little more features than {@link LruCache}.
 * <p/>
 * This class <b>is not</b> thread-safe.
 *
 * @author Richard Chien
 */
public class LruCacheWrapper<K, V> {

    /**
     * Extended LRU cache which handles the main cache actions.
     */
    private ExtendedLruCache mLruCache;

    /**
     * @param maxSize The maximum sum of the sizes of the entries in this cache.
     */
    public LruCacheWrapper(int maxSize) {
        mLruCache = new ExtendedLruCache(maxSize);
    }

    /**
     * Caches {@code value} for {@code key}.
     * The value is moved to the head of the queue.
     *
     * @return The previous value mapped by {@code key}.
     */
    public final V put(K key, V value) {
        if (sizeOf(key, value) > maxSize()) {
            throw new IllegalArgumentException("Object is bigger than max cache size.");
        }
        mLruCache.mMarkRecentlyEvicted = false;
        return mLruCache.put(key, value);
    }

    /**
     * Caches {@code value} for {@code key}.
     * The value is moved to the head of the queue.
     *
     * @param evictedEntryList A list used to store evicted entries.
     * @return The previous value mapped by {@code key}.
     */
    public final V put(K key, V value, List<Entry<K, V>> evictedEntryList) {
        if (sizeOf(key, value) > maxSize()) {
            throw new IllegalArgumentException("Object is bigger than max cache size.");
        }
        mLruCache.mRecentlyEvictedEntryList.clear();
        mLruCache.mMarkRecentlyEvicted = true;
        V result = mLruCache.put(key, value);
        if (evictedEntryList != null) {
            evictedEntryList.addAll(mLruCache.mRecentlyEvictedEntryList);
        }
        mLruCache.mRecentlyEvictedEntryList.clear();
        return result;
    }

    /**
     * Returns the value for {@code key} if it exists in the cache or can be
     * created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     */
    public final V get(K key) {
        return mLruCache.get(key);
    }

    /**
     * Removes the entry for {@code key} if it exists.
     *
     * @return The previous value mapped by {@code key}.
     */
    public final V remove(K key) {
        return mLruCache.remove(key);
    }

    /**
     * @return The sum of the sizes of the entries in this cache.
     */
    public final int size() {
        return mLruCache.size();
    }

    /**
     * @return The maximum sum of the sizes of the entries in this cache.
     */
    public final int maxSize() {
        return mLruCache.maxSize();
    }

    /**
     * @return A copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    public final Map<K, V> snapshot() {
        return mLruCache.snapshot();
    }

    @Override
    public String toString() {
        return "LruCacheWrapper{" +
                "mLruCache=" + mLruCache +
                '}';
    }

    /**
     * Returns the size of the entry for {@code key} and {@code value} in user-defined units.
     * The default implementation returns 1 so that size
     * is the number of entries and max size is the maximum number of entries.
     * <p/>
     * An entry's size must not change while it is in the cache.
     */
    protected int sizeOf(K key, V value) {
        return 1;
    }

    /**
     * Called for entries that have been evicted or removed. This method is
     * invoked when a value is evicted to make space, removed by a call to
     * {@link #remove}, or replaced by a call to {@link #put}. The default
     * implementation does nothing.
     * <p/>
     * The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     *
     * @param evicted  true if the entry is being removed to make space, false
     *                 if the removal was caused by a {@link #put} or {@link #remove}.
     * @param newValue the new value for {@code key}, if it exists. If non-null,
     *                 this removal was caused by a {@link #put}. Otherwise it was caused by
     *                 an eviction or a {@link #remove}.
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }

    /**
     * Extended LRU cache that marks the recently evicted entries.
     */
    private class ExtendedLruCache extends LruCache<K, V> {
        /**
         * List used to mark recently evicted entries.
         */
        List<Entry<K, V>> mRecentlyEvictedEntryList = new ArrayList<>();

        /**
         * Indicate whether to mark recently evicted entries.
         */
        boolean mMarkRecentlyEvicted = false;

        public ExtendedLruCache(int maxSize) {
            super(maxSize);
        }

        /**
         * Override to mark recently evicted entries,
         * and call the wrapper method which may be override by subclass.
         */
        @Override
        protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (evicted && mMarkRecentlyEvicted) {
                mRecentlyEvictedEntryList.add(new Entry<>(key, oldValue));
            }
            LruCacheWrapper.this.entryRemoved(evicted, key, oldValue, newValue);
        }

        /**
         * Call the wrapper method which may be override by subclass.
         */
        @Override
        protected int sizeOf(K key, V value) {
            return LruCacheWrapper.this.sizeOf(key, value);
        }

        /**
         * Change the super class's description of exception.
         */
        @Override
        public void trimToSize(int maxSize) {
            try {
                super.trimToSize(maxSize);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Cache object was modified without calling put() again.");
            }
        }
    }

    /**
     * A wrapper of key and value.
     */
    public static final class Entry<K, V> {
        K key;
        V value;

        public Entry() {
        }

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
}

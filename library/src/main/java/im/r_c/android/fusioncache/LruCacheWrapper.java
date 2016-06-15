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
class LruCacheWrapper<K, V> {

    /**
     * Extended LRU cache which handles the main cache actions.
     */
    private ExtendedLruCache<K, V> mLruCache;

    /**
     * Delegates some LruCache methods.
     * <p/>
     * Never be null.
     */
    private Delegate<K, V> mDelegate;

    /**
     * @param maxSize The maximum sum of the sizes of the entries in this cache.
     */
    public LruCacheWrapper(int maxSize, Delegate<K, V> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate can't be null.");
        }
        mLruCache = new ExtendedLruCache<>(maxSize, delegate);
        mDelegate = delegate;
    }

    /**
     * Caches {@code value} for {@code key}.
     * The value is moved to the head of the queue.
     *
     * @return The previous value mapped by {@code key}.
     */
    public final V put(K key, V value) {
        if (mDelegate.sizeOf(key, value) > maxSize()) {
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
        if (mDelegate.sizeOf(key, value) > maxSize()) {
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
     * Clear the cache, calling {@link ExtendedLruCache#evictAll}.
     */
    public final void evictAll() {
        mLruCache.evictAll();
    }

    /**
     * Returns the sum of the sizes of the entries in this cache.
     */
    public final int size() {
        return mLruCache.size();
    }

    /**
     * Returns the maximum sum of the sizes of the entries in this cache.
     */
    public final int maxSize() {
        return mLruCache.maxSize();
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
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
     * Extended LRU cache that marks the recently evicted entries.
     */
    private static class ExtendedLruCache<K, V> extends LruCache<K, V> {
        /**
         * Delegates some {@code LruCache} methods.
         * <p/>
         * Never be null.
         */
        Delegate<K, V> mDelegate;

        /**
         * List used to mark recently evicted entries.
         */
        List<Entry<K, V>> mRecentlyEvictedEntryList = new ArrayList<>();

        /**
         * Indicate whether to mark recently evicted entries.
         */
        boolean mMarkRecentlyEvicted = false;

        public ExtendedLruCache(int maxSize, Delegate<K, V> delegate) {
            super(maxSize);
            mDelegate = delegate;
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
            mDelegate.entryRemoved(evicted, key, oldValue, newValue);
        }

        /**
         * Call the wrapper method which may be override by subclass.
         */
        @Override
        protected int sizeOf(K key, V value) {
            return mDelegate.sizeOf(key, value);
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
     * Different cache class does different work at these methods,
     * so move them out.
     * <p/>
     * And avoid potential memory leak which may occur
     * if making {@link ExtendedLruCache} an inner class.
     */
    public interface Delegate<K, V> {
        int sizeOf(K key, V value);

        void entryRemoved(boolean evicted, K key, V oldValue, V newValue);
    }

    /**
     * A wrapper of key and value.
     */
    public static final class Entry<K, V> {
        K key;
        V value;

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

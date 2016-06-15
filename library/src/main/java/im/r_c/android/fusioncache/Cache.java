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

/**
 * FusionCache
 * Created by richard on 6/12/16.
 * <p/>
 * An interface that defines basic functions a cache should provide.
 *
 * @author Richard Chien
 */
interface Cache {
    /**
     * Put a {@code String} value into the cache.
     */
    void put(String key, String value);

    /**
     * Put a {@code JSONObject} value into the cache.
     */
    void put(String key, JSONObject value);

    /**
     * Put a {@code JSONArray} value into the cache.
     */
    void put(String key, JSONArray value);

    /**
     * Put a {@code byte[]} value into the cache.
     */
    void put(String key, byte[] value);

    /**
     * Put a {@code Bitmap} value into the cache.
     */
    void put(String key, Bitmap value);

    /**
     * Put a {@code Drawable} value into the cache.
     */
    void put(String key, Drawable value);

    /**
     * Put a {@code Serializable} value into the cache.
     */
    void put(String key, Serializable value);

    /**
     * Get a {@code String} value from the cache.
     */
    String getString(String key);

    /**
     * Get a {@code JSONObject} value from the cache.
     */
    JSONObject getJSONObject(String key);

    /**
     * Get a {@code JSONArray} value from the cache.
     */
    JSONArray getJSONArray(String key);

    /**
     * Get a {@code byte[]} value from the cache.
     */
    byte[] getBytes(String key);

    /**
     * Get a {@code Bitmap} value from the cache.
     */
    Bitmap getBitmap(String key);

    /**
     * Get a {@code Drawable} value from the cache.
     */
    Drawable getDrawable(String key);

    /**
     * Get a {@code Serializable} value from the cache.
     */
    Serializable getSerializable(String key);

    /**
     * Remove a value from the cache.
     */
    Object remove(String key);

    /**
     * Clear the cache, removing all values.
     */
    void clear();

    /**
     * Returns the used size of the cache.
     */
    int size();

    /**
     * Returns the max size of the cache.
     */
    int maxSize();
}

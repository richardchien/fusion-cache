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
 */
interface CacheInterface {
    void put(String key, String value);

    void put(String key, JSONObject value);

    void put(String key, JSONArray value);

    void put(String key, byte[] value);

    void put(String key, Bitmap value);

    void put(String key, Drawable value);

    void put(String key, Serializable value);

    Object get(String key);

    String getString(String key);

    JSONObject getJSONObject(String key);

    JSONArray getJSONArray(String key);

    byte[] getBytes(String key);

    Bitmap getBitmap(String key);

    Drawable getDrawable(String key);

    Serializable getSerializable(String key);

    Object remove(String key);
}

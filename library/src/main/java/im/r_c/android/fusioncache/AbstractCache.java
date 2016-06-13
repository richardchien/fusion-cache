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
 * Created by richard on 6/13/16.
 */
abstract class AbstractCache implements CacheInterface {
    @Override
    public String getString(String key) {
        return Helper.getCache(this, key, String.class);
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return Helper.getCache(this, key, JSONObject.class);
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return Helper.getCache(this, key, JSONArray.class);
    }

    @Override
    public byte[] getBytes(String key) {
        return Helper.getCache(this, key, byte[].class);
    }

    @Override
    public Bitmap getBitmap(String key) {
        return Helper.getCache(this, key, Bitmap.class);
    }

    @Override
    public Drawable getDrawable(String key) {
        return Helper.getCache(this, key, Drawable.class);
    }

    @Override
    public Serializable getSerializable(String key) {
        return Helper.getCache(this, key, Serializable.class);
    }
}

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

package im.r_c.android.fusioncache.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.Serializable;

import im.r_c.android.fusioncache.FusionCache;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        MemCache memCache = new MemCache(4 * 1024 * 1024);
//        memCache.put("a", "sfjkadgfh");
//        memCache.put("b", BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
//        Log.d(TAG, "" + memCache.get("b"));
//        Log.d(TAG, "" + memCache.getString("a"));
//        Log.d(TAG, "size: " + memCache.size());
//        memCache.clear();
//        memCache.put("a", "sdjafkgsakfgjkhkg");
//        Log.d(TAG, "size: " + memCache.size());
//
//        File cacheDir = new File(getCacheDir(), "FusionCache");
//        DiskCache diskCache = new DiskCache(cacheDir, 4 * 1024 * 1024);
//        diskCache.put("abc", "abc");
//        diskCache.put("abcd", "abcd");
//        diskCache.put("abcde", "abcde");
//        diskCache.put("abcde", "abcdeajfgjgsfg");
//        Log.d(TAG, "abcde: " + diskCache.getString("abcde"));
//        Log.d(TAG, "abcd: " + Arrays.toString(diskCache.getBytes("abcd")));
//
//        try {
//            diskCache.put("jsonObject", new JSONObject("{a: b, c: d}"));
//            diskCache.put("jsonArray", new JSONArray("[{a: b, c: d}, {e: f, g: h}]"));
//            Log.d(TAG, "jsonArray: " + diskCache.getJSONArray("jsonArray"));
//            Log.d(TAG, "jsonObject: " + diskCache.getJSONObject("jsonObject"));
//        } catch (JSONException ignored) {
//        }
//
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        diskCache.put("bitmap", bitmap);
//        ImageView iv = (ImageView) findViewById(R.id.iv_image);
//        assert iv != null;
//        iv.setImageDrawable(diskCache.getDrawable("bitmap", getResources()));
//
//        Bean bean = new Bean(3);
//        diskCache.put("bean", bean);
//        Log.d(TAG, "bean: " + diskCache.getSerializable("bean"));
//
//        diskCache.clear();

        FusionCache cache = new FusionCache(this, 30, 4 * 1024 * 1024);
        cache.put("a", "abcd");
        cache.put("b", "abcde");
        Log.d(TAG, "a: " + cache.getString("a"));
        cache.put("c", "abcdefgh");
//        Log.d(TAG, "c in mem: " + cache.getMemCache().getString("c"));
//        Log.d(TAG, "c in disk: " + cache.getDiskCache().getString("c"));
        Log.d(TAG, "c: " + cache.getString("c"));
        cache.put("d", "safhkhsf");
//        Log.d(TAG, "c in mem: " + cache.getMemCache().getString("c"));
//        Log.d(TAG, "c in disk: " + cache.getDiskCache().getString("c"));
        Log.d(TAG, "c: " + cache.getString("c"));

        cache.put("bean", new Bean(5));
        Log.d(TAG, "bean: " + cache.getSerializable("bean"));

        cache.put("string", "string");
        Log.d(TAG, "string: " + cache.getString("string"));
        cache.saveMemCacheToDisk();

        cache.put("bitmap", bitmap);
        Log.d(TAG, "bitmap: " + cache.getBitmap("bitmap"));

        cache.remove("bitmap");
        Log.d(TAG, "bitmap: " + cache.getBitmap("bitmap"));
    }
}

class Bean implements Serializable {
    int mInt = 2;

    public Bean(int anInt) {
        mInt = anInt;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "mInt=" + mInt +
                '}';
    }
}

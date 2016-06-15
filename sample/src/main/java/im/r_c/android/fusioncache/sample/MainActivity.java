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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import im.r_c.android.commonrecyclerviewadapter.CommonRecyclerViewAdapter;
import im.r_c.android.commonrecyclerviewadapter.ViewHolder;
import im.r_c.android.fusioncache.sample.util.HttpUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static im.r_c.android.fusioncache.sample.util.HttpUtils.DEFAULT_CONNECTION_TIME_OUT;
import static im.r_c.android.fusioncache.sample.util.HttpUtils.DEFAULT_READ_TIME_OUT;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private Adapter mRecyclerViewAdapter;
    private List<MovieEntity> mMovieEntities;
    private String mUrl = "https://api.douban.com/v2/movie/top250";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        loadFromCache();
        loadFromInternet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getCache().saveMemCacheToDisk();
    }

    private void init() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_swipe_refresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        assert mSwipeRefreshLayout != null;
        assert mRecyclerView != null;

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mMovieEntities = new ArrayList<>();
        mRecyclerViewAdapter = new Adapter(this, mMovieEntities, R.layout.list_item);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mRecyclerView.setEnabled(mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
    }

    private void loadFromCache() {
        Observable<JSONArray> observable = Observable.just(mUrl)
                .map(new Func1<String, JSONArray>() {
                    @Override
                    public JSONArray call(String url) {
                        JSONArray array = null;
                        JSONObject object = App.getCache().getJSONObject(url);
                        if (object != null) {
                            try {
                                array = object.getJSONArray("subjects");
                            } catch (JSONException ignored) {
                            }
                        }
                        return array;
                    }
                });
        subscribeJSONArray(observable);
    }

    private void loadFromInternet() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        Observable<JSONArray> observable = Observable.just(mUrl)
                .map(new Func1<String, JSONArray>() {
                    @Override
                    public JSONArray call(String url) {
                        // Get json array
                        JSONArray jsonArray = null;
                        String jsonString = HttpUtils.getSync(url);
                        if (!"".equals(jsonString)) {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonString);
                                App.getCache().getDiskCache().put(mUrl, jsonObject);
                                jsonArray = jsonObject.getJSONArray("subjects");
                            } catch (JSONException ignored) {
                            }
                        }
                        Log.d(TAG, "Got json array: " + jsonArray);
                        return jsonArray;
                    }
                });
        subscribeJSONArray(observable);
    }

    private void subscribeJSONArray(Observable<JSONArray> observable) {
        observable.flatMap(new Func1<JSONArray, Observable<JSONObject>>() {
            @Override
            public Observable<JSONObject> call(final JSONArray jsonArray) {
                // Flatten the "subjects" array
                return Observable.create(new Observable.OnSubscribe<JSONObject>() {
                    @Override
                    public void call(Subscriber<? super JSONObject> subscriber) {
                        subscriber.onStart();
                        if (jsonArray != null) {
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    subscriber.onNext(jsonArray.getJSONObject(i));
                                    Log.d(TAG, "Got entity json object: " + jsonArray.getJSONObject(i));
                                }
                            } catch (JSONException ignored) {
                            }
                        }
                        subscriber.onCompleted();
                        Log.d(TAG, "flatMap completed");
                    }
                });
            }
        })
                .map(new Func1<JSONObject, MovieEntity>() {
                    @Override
                    public MovieEntity call(JSONObject jsonObject) {
                        // Make movie entities
                        MovieEntity entity = null;
                        try {
                            entity = new MovieEntity();
                            entity.setTitle(jsonObject.getString("title"));
                            entity.setOriginalTitle(jsonObject.getString("original_title"));
                            entity.setYear(jsonObject.getString("year"));
                            entity.setImageUrl(jsonObject.getJSONObject("images").getString("medium"));
                        } catch (JSONException ignored) {
                        }
                        Log.d(TAG, "Got movie entity: " + entity);
                        return entity;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MovieEntity>() {
                    private List<MovieEntity> list;

                    @Override
                    public void onStart() {
                        super.onStart();
                        list = new ArrayList<>();
                    }

                    @Override
                    public void onNext(MovieEntity movieEntity) {
                        list.add(movieEntity);
                    }

                    @Override
                    public void onCompleted() {
                        mMovieEntities.clear();
                        mMovieEntities.addAll(list);
                        mRecyclerViewAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void onRefresh() {
        loadFromInternet();
    }

    private static class Adapter extends CommonRecyclerViewAdapter<MovieEntity> {

        public Adapter(Context context, List<MovieEntity> dataList, int layoutId) {
            super(context, dataList, layoutId);
        }

        @Override
        public void onPostBindViewHolder(final ViewHolder viewHolder, MovieEntity movieEntity) {
            viewHolder.setViewText(R.id.tv_title, movieEntity.getTitle())
                    .setViewText(R.id.tv_original_title, movieEntity.getOriginalTitle())
                    .setViewText(R.id.tv_year, movieEntity.getYear());

            Observable.just(movieEntity.getImageUrl())
                    .map(new Func1<String, Bitmap>() {
                        @Override
                        public Bitmap call(String urlString) {
                            Bitmap bitmap = App.getCache().getBitmap(urlString);
                            if (bitmap == null) {
                                HttpURLConnection connection = null;
                                try {
                                    URL url = new URL(urlString);
                                    connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.setConnectTimeout(DEFAULT_CONNECTION_TIME_OUT);
                                    connection.setReadTimeout(DEFAULT_READ_TIME_OUT);
                                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (connection != null) {
                                        connection.disconnect();
                                    }
                                }

                                if (bitmap != null) {
                                    App.getCache().put(urlString, bitmap);
                                }
                            }
                            return bitmap;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Bitmap>() {
                        @Override
                        public void call(Bitmap bitmap) {
                            if (bitmap != null) {
                                viewHolder.setViewImageBitmap(R.id.iv_movie_logo, bitmap);
                            }
                        }
                    });
        }
    }
}

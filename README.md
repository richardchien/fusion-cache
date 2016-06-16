# FusionCache for Android

[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://github.com/richardchien/fusion-cache-android/blob/master/LICENSE)
[![Release](https://jitpack.io/v/richardchien/fusion-cache-android.svg)](https://jitpack.io/#richardchien/fusion-cache-android)

[中文](#zh) [English](#en)

<a name="zh">

这是一个内存和磁盘混合的缓存框架，采用 LRU 算法，内部动态地将缓存的内容在内存缓存和磁盘缓存之间移动，从而保证最近访问的项目在内存中，而较老的缓存在磁盘或被删除（磁盘缓存满了的情况下）。

动态混合缓存由 `FusionCache` 类实现，你也可以不用它，单独使用 `MemCache` 或 `DiskCache` 类来分别做内存和磁盘缓存。

API 文档：[http://richardchien.github.io/fusion-cache-android/](http://richardchien.github.io/fusion-cache-android/)

## 动态机制／原理

### FusionCache

以下内容是开启了混合模式（可在构造方法中通过 `enableFusionMode` 参数指定）的情况。

从外部看来，`FusionCache` 和 `MemCache`、`DiskCache` 一样体现出 LRU 的特点，最近访问的对象将保持在内存缓存的最前面，而最老的对象将会先从内存缓存移动到磁盘缓存，然后被删除。`FusionCache` 内部通过动态地调用 `MemCache` 和 `DiskCache` 对象来实现混合缓存功能。

插入对象时：

- 优先放进内存缓存，如果对象放进去时，为了腾出空间而删除了较老的缓存，则把这些删除掉的缓存放进磁盘缓存；
- 如果对象大小超过了内存缓存的最大容量（无法放进内存缓存），则放进磁盘缓存；
- 如果对象大小超过了磁盘缓存的最大容量，则不缓存。

取出对象时：

- 如果对象在内存缓存，则直接取出返回；
- 如果对象在磁盘缓存，则取出后放进内存缓存（原磁盘缓存中的缓存文件不删除），并返回结果；
- 如果对象不存在，则返回 `null`。

删除对象时，内存和磁盘缓存中所有对应于要删除的键的缓存都将被删除。

清空缓存时，所有内存和磁盘缓存，以及磁盘缓存目录都会被删除。

### MemCache 和 DiskCache

这两个类的内部都采用了经过一定修改的 `LruCache` 来实现 LRU 缓存。`MemCache` 直接由 `LruCache` 来维护对象的强引用，而 `DiskCache` 使用 `LruCache` 来维护缓存文件的文件名（键）和文件大小，具体文件存储在缓存目录下。

## 用法

添加 Gradle 依赖：

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    compile 'com.github.richardchien:fusion-cache-android:v1.0.0-beta2'
}
```

### 使用 FusionCache

```java
FusionCache cache = new FusionCache(
        getApplicationContext(),
        4 * 1024 * 1024, // 缓存容量的单位是字节
        50 * 1024 * 1024,
        true // 开启混合缓存模式，默认为 true
);

cache.put("string", "This is a string.");
cache.put("jsonObject", new JSONObject("{}"));
cache.put("jsonArray", new JSONArray("[]"));
cache.put("bytes", new byte[10]);
cache.put("bitmap", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
cache.put("drawable", getDrawable(R.mipmap.ic_launcher));

String string = cache.getString("string");
JSONObject jsonObject = cache.getJSONObject("jsonObject");
JSONArray jsonArray = cache.getJSONArray("jsonArray");
byte[] bytes = cache.getBytes("bytes");
Bitmap bitmap = cache.getBitmap("bitmap");
Drawable drawable = cache.getDrawable("drawable");

cache.saveMemCacheToDisk(); // 将内存缓存中的内容全部保存到磁盘缓存, 一般在应用退出时调用

cache.remove("bitmap");
cache.clear();
```

**重要**：千万不要试图使用不相对应的 `put` 和 `get` 方法存取同一个键对应的的值，比如存入一个字符串却试图取出字节数组，这会导致不确定的结果或抛出运行时异常。并且，存入一个对象后，在其它地方改变了这个对象所占大小，却没有重新 `put` 进缓存的话，后续缓存操作可能会抛出异常，因此一旦一个缓存了的对象发生改变，应该重新调用 `put` 方法存入缓存。

### 使用 MemCache 和 DiskCache

这两个类的 API 基本和 `FusionCache` 相似。另外，对于 `FusionCache` 可以通过下面两个方法来分别获得其内部的 `MemCache` 和 `DiskCache`：

```java
cache.getMemCache();
cache.getDiskCache();
```

如果 `FusionCache` 没有开启混合缓存模式，则必须通过这两个方法获取 `MemCache` 或 `DiskCache` 来使用，比如 `cache.getMemCache().put("a", "abc")`，否则会抛出异常。

---------

<a name="en">

This is a fusion cache library with mixes memory and disk cache together, using LRU algorithm. It moves cache values dynamically from memory to disk or from disk to memory, so that it can ensure that the most recently accessed items are in memory, and the elder ones are in disk or removed (if the disk cache is full).

The `FusionCache` class gives you a dynamic mixed cache. Also, you can just use `MemCache` or `DiskCache` separately, instead of `FusionCache`.

API docs: [http://richardchien.github.io/fusion-cache-android/](http://richardchien.github.io/fusion-cache-android/)

## Mechanism / Principle

### FusionCache

The following is on the premise that the "fusion mode" is enabled (which can be set through constructor).

From outside, `FusionCache` is just like `MemCache` and `DiskCache`, acting like a LRU cache -- the most recently accessed items are on the top of memory cache and the eldest items will be moved from memory to disk and then deleted. `FusionCache` implements it's functions by manipulating `MemCache` and `DiskCache` inside itself.

Inserting an item:

- Try to put it into memory cache. If memory cache is capable to store it, and some elder items are deleted from memory, then put these deleted ones into disk cache;
- If the size of the item is bigger than the max size of memory cache (which means memory cache is not capable to store it), then put it into disk cache;
- If the size of the item is bigger than the max size of disk cache, then don't cache it.

Getting an item:

- If the item is in memory cache, then get it directly;
- If the item is in disk cache, then put it into memory cache extraly (without deleting the disk cache file);
- If the item does not exist, then return `null`.

Deleting an item: Any items that match the key will be deleted wherever they are stored (memory or disk).

Clearing cache: All items in both memory and disk cache will be deleted, even the disk cache directory.

### MemCache and DiskCache

These two classes both use modified `LruCache` to achieve LRU cache. `MemCache` uses `LruCache` to maintain strong references of objects, and `DiskCache` uses `LruCache` to maintain names (key) and sizes of the cache files while the actual files are stored in the cache directory.

## Usage

Add dependency in `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    compile 'com.github.richardchien:fusion-cache-android:v1.0.0-beta2'
}
```

### Use FusionCache

```java
FusionCache cache = new FusionCache(
        getApplicationContext(),
        4 * 1024 * 1024, // The unit of cache size is byte
        50 * 1024 * 1024,
        true // Enable fusion mode, default is true
);

cache.put("string", "This is a string.");
cache.put("jsonObject", new JSONObject("{}"));
cache.put("jsonArray", new JSONArray("[]"));
cache.put("bytes", new byte[10]);
cache.put("bitmap", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
cache.put("drawable", getDrawable(R.mipmap.ic_launcher));

String string = cache.getString("string");
JSONObject jsonObject = cache.getJSONObject("jsonObject");
JSONArray jsonArray = cache.getJSONArray("jsonArray");
byte[] bytes = cache.getBytes("bytes");
Bitmap bitmap = cache.getBitmap("bitmap");
Drawable drawable = cache.getDrawable("drawable");

cache.saveMemCacheToDisk(); // Save all memory caches into disk cache, typically called while exiting the app

cache.remove("bitmap");
cache.clear();
```

**Important**: NEVER use `put` and `get` methods that doesn't matches each other to save and fetch values, for example, put a String into cache but attempt to get it through `getBytes()`. This will result in an undetermine condition or throw a runtime exception. In addition, if an object is modified after put into cache, the `put` methods should be called again in order to handle potential size change of the object, otherwise, an exception may be thrown in later operations.

### Use MemCache and DiskCache

The APIs of these two classes are almost the same as `FusionCache`. Whatsmore, you can get the `MemCache` and `DiskCache` inside the `FusionCache` through the following two methods:

```java
cache.getMemCache();
cache.getDiskCache();
```

If a `FusionCache`'s fusion mode is not enabled, you can't use it by directly calling it's methods like `put` and `get`, or an exception will be thrown. Instead, you must get it's inner memory or disk cache first, like `cache.getMemCache().put("a", "abc")`.

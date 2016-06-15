# FusionCache for Android

[![License](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)]()
[![Release](https://jitpack.io/v/richardchien/fusion-cache-android.svg)](https://jitpack.io/#richardchien/fusion-cache-android)

[中文](#zh) [English](#en)

<a name="zh">

这是一个内存和磁盘混合的缓存框架，采用 LRU 算法，内部动态地将缓存的内容在内存缓存和磁盘缓存之间移动，从而保证最近访问的项目在内存中，而较老的缓存在磁盘或被删除（磁盘缓存满了的情况下）。

动态混合缓存由 `FusionCache` 类实现，你也可以不用它，单独使用 `MemCache` 或 `DiskCache` 类来分别做内存和磁盘缓存。

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

### 使用 FusionCache

```java
FusionCache cache = new FusionCache(
        getApplicationContext(),
        4 * 1024 * 1024, // 缓存容量的单位是字节
        50 * 1024 * 1024, // 无论内存还是磁盘缓存, 容量都必须小于 Integer.MAX_VALUE
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

### 使用 MemCache 和 DiskCache

这两个类的 API 基本和 `FusionCache` 相似。另外，对于 `FusionCache` 可以通过下面两个方法来分别获得其内部的 `MemCache` 和 `DiskCache`：

```java
cache.getMemCache();
cache.getDiskCache();
```

如果 `FusionCache` 没有开启混合缓存模式，则必须通过这两个方法获取 `MemCache` 或 `DiskCache` 来使用，否则将会抛出异常。

<a name="en">

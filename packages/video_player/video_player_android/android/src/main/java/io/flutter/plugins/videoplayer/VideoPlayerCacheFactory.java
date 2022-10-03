package io.flutter.plugins.videoplayer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheWriter;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayerCacheFactory {

    private static CacheDataSource.Factory factory;
    private static SimpleCache simpleCache;

    public static CacheDataSource.Factory getCacheDataSourceFactory(Context context, DefaultHttpDataSource.Factory defaultFactory) {
        if (simpleCache == null) {
            createSimpleCache(context).release();
            simpleCache = createSimpleCache(context);
            factory = new CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(defaultFactory)
                    .setCacheWriteDataSinkFactory(new CacheDataSink.Factory().setCache(simpleCache).setFragmentSize(2 * 1024 * 1024))
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    .setEventListener(new CacheDataSource.EventListener() {
                        @Override
                        public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {
                            Log.d("VideoCache", "Bytes read from cache: size: " + cacheSizeBytes + ", read: " + cachedBytesRead);
                        }

                        @Override
                        public void onCacheIgnored(int reason) {
                            Log.d("VideoCache", "Cache ignored, reason: " + reason);
                        }
                    });
//            new Thread(VideoPlayerCacheFactory::cacheVideo).start();
        }
        return factory;
    }

    @NonNull
    private static SimpleCache createSimpleCache(Context context) {
        return new SimpleCache(new File(String.valueOf(context.getCacheDir()), "videos"),
                new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
                null, null,
                false, false);
    }

    public static void cacheVideo() {
        CacheDataSource dataSource = factory.createDataSource();

        List<String> urls = new ArrayList<>();
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F3G8gQPFq8sup9uRXeMvVpymWJ.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F3G8gQPFq8sup9uRXeMvVpymWJ_correct.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F3G8gQPFq8sup9uRXeMvVpymWJ_incorrect.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DVZQMfOSmy05WhFe6zjla86Hg2.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DVZQMfOSmy05WhFe6zjla86Hg2_correct.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DVZQMfOSmy05WhFe6zjla86Hg2_incorrect.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F5bENtNBvVhW32IqHILjgxZJZL.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F5bENtNBvVhW32IqHILjgxZJZL_correct.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2F5bENtNBvVhW32IqHILjgxZJZL_incorrect.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJUG9zGV0KbbQMgbhJeAM7sD0G.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJUG9zGV0KbbQMgbhJeAM7sD0G_correct.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJUG9zGV0KbbQMgbhJeAM7sD0G_incorrect.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJEEo9tQnUM3dtf4nFyeObQlht.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJEEo9tQnUM3dtf4nFyeObQlht_correct.mp4");
        urls.add("https://cf-templates-xmbl23h666sf-us-east-1.s3.amazonaws.com/videos/2DJEEo9tQnUM3dtf4nFyeObQlht_incorrect.mp4");

        for (String url : urls) {
            try {
                Uri videoUri = Uri.parse(url);
                DataSpec dataSpec = new DataSpec(videoUri);

                CacheWriter cacheWriter = new CacheWriter(
                        dataSource,
                        dataSpec,
                        null, null
                );
                cacheWriter.cache();
            } catch (Exception e) {
                Log.e("VideoCache", "Error catching url " + url, e);
            }
        }

    }

    public static void preload(List<String> uris) {
        CacheDataSource dataSource = factory.createDataSource();
        new Thread(() -> {
            for (String url : uris) {
                try {
                    Uri videoUri = Uri.parse(url);
                    DataSpec dataSpec = new DataSpec(videoUri);

                    CacheWriter cacheWriter = new CacheWriter(
                            dataSource,
                            dataSpec,
                            null, null
                    );
                    cacheWriter.cache();
                } catch (Exception e) {
                    Log.e("VideoCache", "Error catching url " + url, e);
                }
            }
        }).start();
    }
}


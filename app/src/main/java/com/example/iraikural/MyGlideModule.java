package com.example.iraikural;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Set memory cache size (optional)
        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20MB
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
    }
}

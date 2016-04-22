/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.modules.appmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import hugo.weaving.DebugLog;
import timber.log.Timber;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
import uk.co.senab.bitmapcache.CacheableImageView;

public class AppIconImageView extends CacheableImageView {
    private BitmapLruCache mCache;

    private ImageLoadTask mCurrentTask;

    public AppIconImageView(Context context) {
        this(context, null);
    }

    public AppIconImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppIconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            mCache = null;
            return;
        }
        mCache = App.get(context).getBitmapLruCache();
    }

    @DebugLog public boolean loadImage(AppItem appItem) {
        // First check whether there's already a task running, if so cancel it
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }

        final String pkgName = appItem.getPackageName();

        // Check to see if the memory cache already has the bitmap. We can
        // safely do this on the main thread.
        BitmapDrawable wrapper = ((mCache != null) ? mCache.getFromMemoryCache(pkgName) : null);
        if (wrapper != null) {
            // The cache has it, so just display it
            setImageDrawable(wrapper);
            return true;
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            setImageDrawable(null);

            mCurrentTask = new ImageLoadTask(this, appItem, mCache);
            try {
                mCurrentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (RejectedExecutionException e) {
                Timber.e(e, "rejected task execution");
            }

            return false;
        }
    }

    private static class ImageLoadTask extends AsyncTask<String, Void, CacheableBitmapDrawable> {
        private final BitmapLruCache bitmapLruCache;
        private final AppItem appItem;

        private final WeakReference<CacheableImageView> weakReference;

        ImageLoadTask(CacheableImageView imageView, AppItem appItem, BitmapLruCache cache) {
            this.appItem = appItem;
            bitmapLruCache = cache;
            weakReference = new WeakReference<>(imageView);
        }

        @Override protected CacheableBitmapDrawable doInBackground(String... params) {
            // Return early if the ImageView has disappeared.
            if (weakReference.get() == null) {
                Timber.d("ImageView has disappeared!");
                return null;
            }
            if (bitmapLruCache == null) {
                Timber.d("BitmapLruCache does not exist!");
                return null;
            }

            return getBitmap();
        }

        @Override protected void onPostExecute(CacheableBitmapDrawable result) {
            final CacheableImageView iv = weakReference.get();
            if (iv != null) {
                iv.setImageDrawable(result);
            }
            weakReference.clear();
        }

        @DebugLog private CacheableBitmapDrawable getBitmap() {
            final String pkgName = appItem.getPackageName();

            // Now we're not on the main thread we can check all caches
            final CacheableBitmapDrawable result = bitmapLruCache.get(pkgName, null);
            if (result == null) {
                Timber.d("Loading -> %s", pkgName);

                final Context context = weakReference.get().getContext();
                final Drawable drawable = appItem.getApplicationInfo().loadIcon(context.getPackageManager());
                final Bitmap bitmap = DrawableHelper.drawableToBitmap(drawable);
                final InputStream is = DrawableHelper.bitmapToInputStream(bitmap);

                // Add to cache
                return bitmapLruCache.put(pkgName, is, null);
            } else {
                Timber.d("Got from Cache -> %s", pkgName);
            }
            return result;
        }
    }
}

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:28
 */

public class RecyclingImageView extends ImageView {

    public RecyclingImageView(Context context) {
        super(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            // The drawable is a CountingBitmapDrawable, so notify it
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }

    /**
     * @see android.widget.ImageView#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        // This has been detached from Window, so clear the drawable
        setImageDrawable(null);

        super.onDetachedFromWindow();
    }

    /**
     * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
     */
    @Override
    public void setImageDrawable(Drawable drawable) {
        // Keep hold of previous Drawable
        final Drawable previousDrawable = getDrawable();

        // Call super to set new Drawable
        super.setImageDrawable(drawable);

        // Notify new Drawable that it is being displayed
        notifyDrawable(drawable, true);

        // Notify old Drawable so it is no longer being displayed
        notifyDrawable(previousDrawable, false);
    }

    public class RecyclingBitmapDrawable extends BitmapDrawable {

        static final String TAG = "CountingBitmapDrawable";

        private int mCacheRefCount = 0;
        private int mDisplayRefCount = 0;

        private boolean mHasBeenDisplayed;

        public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        /**
         * Notify the drawable that the displayed state has changed. Internally a
         * count is kept so that the drawable knows when it is no longer being
         * displayed.
         *
         * @param isDisplayed - Whether the drawable is being displayed or not
         */
        public void setIsDisplayed(boolean isDisplayed) {
            //BEGIN_INCLUDE(set_is_displayed)
            synchronized (this) {
                if (isDisplayed) {
                    mDisplayRefCount++;
                    mHasBeenDisplayed = true;
                } else {
                    mDisplayRefCount--;
                }
            }

            // Check to see if recycle() can be called
            checkState();
            //END_INCLUDE(set_is_displayed)
        }

        /**
         * Notify the drawable that the cache state has changed. Internally a count
         * is kept so that the drawable knows when it is no longer being cached.
         *
         * @param isCached - Whether the drawable is being cached or not
         */
        public void setIsCached(boolean isCached) {
            //BEGIN_INCLUDE(set_is_cached)
            synchronized (this) {
                if (isCached) {
                    mCacheRefCount++;
                } else {
                    mCacheRefCount--;
                }
            }

            // Check to see if recycle() can be called
            checkState();
            //END_INCLUDE(set_is_cached)
        }

        private synchronized void checkState() {
            //BEGIN_INCLUDE(check_state)
            // If the drawable cache and display ref counts = 0, and this drawable
            // has been displayed, then recycle
            if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                    && hasValidBitmap()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "No longer being used or cached so recycling. "
                            + toString());
                }

                getBitmap().recycle();
            }
            //END_INCLUDE(check_state)
        }

        private synchronized boolean hasValidBitmap() {
            Bitmap bitmap = getBitmap();
            return bitmap != null && !bitmap.isRecycled();
        }

    }
}
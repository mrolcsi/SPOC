package hu.mrolcsi.android.spoc.gallery;

import android.app.Application;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:07
 */

public class SPOCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //initialize UIL library
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                //.cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(2)
                .diskCacheExtraOptions(480, 320, null)
                .build();
        ImageLoader.getInstance().init(config);

        //TODO: scan MediaStore & white-listed folders for Images
        //  update library if needed
    }
}

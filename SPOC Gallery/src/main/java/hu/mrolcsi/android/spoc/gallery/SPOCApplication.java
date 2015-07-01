package hu.mrolcsi.android.spoc.gallery;

import android.app.Application;
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
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }
}

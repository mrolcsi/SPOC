package hu.mrolcsi.android.spoc.gallery.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.14.
 * Time: 13:35
 */

/**
 * Solution for pinch-zooming crash.
 * <p/>
 * Source:
 * http://www.arthurwang.net/android/arrayindexoutofboundsexception-with-photoview-library-and-drawerlayout
 */
public class DrawerLayout extends android.support.v4.widget.DrawerLayout {
    private boolean IntercepterDisallowed = false;

    //Required default constructors
    public DrawerLayout(Context context) {
        super(context);
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        /*
         * We will store the decision of the inner view, whether the intercepter is disallowed or not.
         */
        IntercepterDisallowed = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        /*
         * When multi touch happens, handle it.
         */
        if (ev.getPointerCount() > 1 && IntercepterDisallowed) {
            requestDisallowInterceptTouchEvent(false);
            boolean handled = super.dispatchTouchEvent(ev);
            requestDisallowInterceptTouchEvent(true);
            return handled;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }
}
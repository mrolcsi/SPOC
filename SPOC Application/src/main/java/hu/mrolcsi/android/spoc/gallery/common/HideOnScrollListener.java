package hu.mrolcsi.android.spoc.gallery.common;

import android.support.v7.widget.RecyclerView;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.08.03.
 * Time: 12:39
 */

public abstract class HideOnScrollListener extends TwoWayView.OnScrollListener {

    private static final int MINIMUM = 0;
    private int scrollDist = 0;
    private boolean isVisible = true;

    public HideOnScrollListener() {
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (isVisible && scrollDist > MINIMUM) {
            hide();
            scrollDist = 0;
            isVisible = false;
        } else if (!isVisible && scrollDist < -MINIMUM) {
            show();
            scrollDist = 0;
            isVisible = true;
        }
        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }
    }

    public abstract void hide();

    public abstract void show();
}

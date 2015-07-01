package hu.mrolcsi.android.spoc.gallery.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hu.mrolcsi.android.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.SPOCFragment;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:12
 */

public class HomeFragment extends SPOCFragment {

    private View mRootView;
    private TwoWayView twList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        twList = (TwoWayView) view.findViewById(R.id.list);

        twList.setAdapter(new HomeScreenAdapter());
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_home);
    }
}

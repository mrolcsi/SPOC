package hu.mrolcsi.android.spoc.gallery.common;

import android.support.v4.app.Fragment;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:23
 */

public abstract class SPOCFragment extends Fragment implements ISPOCFragment {
    @Override
    public String getTagString() {
        return getClass().getName();
    }
}

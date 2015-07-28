package hu.mrolcsi.android.spoc.common.fragment;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:24
 */

public interface ISPOCFragment {

    int getNavigationItemId();

    String getTitle();

    String getTagString();

    boolean onBackPressed();
}

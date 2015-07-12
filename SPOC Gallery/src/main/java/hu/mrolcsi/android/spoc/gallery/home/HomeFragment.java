package hu.mrolcsi.android.spoc.gallery.home;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import hu.mrolcsi.android.spoc.common.MediaStoreLoader;
import hu.mrolcsi.android.spoc.common.SPOCFragment;
import hu.mrolcsi.android.spoc.gallery.R;
import org.lucasr.twowayview.widget.TwoWayView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:12
 */

public class HomeFragment extends SPOCFragment implements CursorLoader.OnLoadCompleteListener<Cursor> {

    private View mRootView;
    private TwoWayView twList;
    private HomeScreenAdapter adapter;

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
        twList.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            //TODO: process params
        }

        getLoaderManager().initLoader(MediaStoreLoader.ID, null, new MediaStoreLoader(getActivity(), this));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (adapter != null && adapter.getCursor() != null)
            adapter.getCursor().close();
    }

    @Override
    public String getTitle() {
        return getString(R.string.title_home);
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        // TODO
        Toast.makeText(getActivity(), "Load complete.", Toast.LENGTH_SHORT).show();
        Log.d(getClass().getName(), "Cursor.getCount= " + data.getCount());

        adapter = new HomeScreenAdapter(getActivity(), data);
        twList.setAdapter(adapter);
    }
}

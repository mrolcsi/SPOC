package hu.mrolcsi.android.spoc.gallery.main.categories;

import android.support.v4.content.CursorLoader;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.10.01.
 * Time: 16:25
 */

public class ContactsFragment extends CategoriesFragment {

    public static final int PEOPLE_LOADER_ID = 22;

    @Override
    protected CategoryHeaderLoader setupCategoryLoader() {
        return new CategoryHeaderLoader() {
            @Override
            public void loadIcon(String s, final ImageView view) {
                //TODO: how to get contactId?
//                new ContactPhotoLoader(getActivity(), contactId) {
//                    @Override
//                    protected void onPreExecute() {
//                        view.setImageResource(R.drawable.user);
//                    }
//
//                    @Override
//                    protected void onPostExecute(Drawable drawable) {
//                        view.setImageDrawable(drawable);
//                    }
//                }.execute();
            }

            @Override
            public void loadText(String s, TextView view) {
                view.setText(s);
            }
        };
    }

    @Override
    protected CursorLoader setupLoader() {
        // TODO
        return null;
    }

    @Override
    public int getLoaderId() {
        return PEOPLE_LOADER_ID;
    }
}

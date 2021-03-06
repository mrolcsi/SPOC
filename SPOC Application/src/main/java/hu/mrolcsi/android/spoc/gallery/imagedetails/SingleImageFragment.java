package hu.mrolcsi.android.spoc.gallery.imagedetails;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import hu.mrolcsi.android.spoc.common.fragment.SPOCFragment;
import hu.mrolcsi.android.spoc.common.helper.FaceDetectorTask;
import hu.mrolcsi.android.spoc.common.loader.ContactsTableLoader;
import hu.mrolcsi.android.spoc.common.loader.ImagesTableLoader;
import hu.mrolcsi.android.spoc.common.utils.FileUtils;
import hu.mrolcsi.android.spoc.common.utils.GeneralUtils;
import hu.mrolcsi.android.spoc.database.model.Contact;
import hu.mrolcsi.android.spoc.database.model.Label;
import hu.mrolcsi.android.spoc.database.model.LabelType;
import hu.mrolcsi.android.spoc.database.model.binder.Contact2Image;
import hu.mrolcsi.android.spoc.database.provider.SPOCContentProvider;
import hu.mrolcsi.android.spoc.gallery.BuildConfig;
import hu.mrolcsi.android.spoc.gallery.R;
import hu.mrolcsi.android.spoc.gallery.common.ContactPhotoLoader;
import hu.mrolcsi.android.spoc.gallery.common.utils.DialogUtils;
import hu.mrolcsi.android.spoc.gallery.common.utils.SystemUiHider;
import hu.mrolcsi.android.spoc.gallery.imagedetails.editor.DateTimePickerDialog;
import hu.mrolcsi.android.spoc.gallery.imagedetails.editor.EditLabelsDialog;
import hu.mrolcsi.android.spoc.gallery.imagedetails.editor.LocationInputDialog;
import hu.mrolcsi.android.spoc.gallery.search.SuggestionAdapter;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.13.
 * Time: 20:19
 */

public class SingleImageFragment extends SPOCFragment implements ImagesTableLoader.LoaderCallbacks {

    public static final int SUGGESTIONS_LOADER_ID = 51;

    public static final String ARG_IMAGE_ID = "SPOC.Gallery.Details.ImageId";
    public static final String ARG_IMAGE_PATH = "SPOC.Gallery.Details.ImagePath";
    public static final String ARG_IMAGE_LOCATION = "SPOC.Gallery.Details.Location";
    public static final String ARG_IMAGE_DATE_TAKEN = "SPOC.Gallery.Details.DateTaken";
    private static final int PLACE_PICKER_REQUEST = 65;
    private static final int VISIBLE_ALPHA = 128;

    private PhotoView photoView;
    private View mFaceTagStatic;
    private View mFaceTagEditable;
    private FaceTagViewHolder mFaceTagViewHolder;

    private int mDesiredWidth;
    private int mDesiredHeight;

    private String mImagePath;
    private int mImageId;
    private CursorLoader mFacesLoader;

    private LayerDrawable mOverlayDrawable;
    private BitmapDrawable mPhotoDrawable;
    private FaceDetectorTask mDetector;
    private List<Contact2Image> mFacePositions;
    private BitmapImageViewTarget mBitmapTarget;
    private BitmapDrawable[] mLayers = new BitmapDrawable[0];

    private Bundle mSuggestionArgs = new Bundle();
    private SuggestionAdapter mSuggestionsAdapter;
    private CursorLoader mSuggestionsLoader;
    private int mSelectedFaceIndex = -1;

    public static SingleImageFragment newInstance(int imageId, String imagePath, String location, long dateTaken) {
        final SingleImageFragment f = new SingleImageFragment();

        final Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_ID, imageId);
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putString(ARG_IMAGE_LOCATION, location);
        args.putLong(ARG_IMAGE_DATE_TAKEN, dateTaken);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSuggestionArgs.putStringArray(ContactsTableLoader.ARG_PROJECTION, new String[]{"_id", Contact.COLUMN_NAME, Contact.COLUMN_TYPE});
        mSuggestionArgs.putString(ContactsTableLoader.ARG_SELECTION, Label.COLUMN_TYPE + "='" + LabelType.CONTACT.name() + "' AND (" + "upper(" + Label.COLUMN_NAME + ") LIKE ?" + " OR " + "lower(" + Label.COLUMN_NAME + ") LIKE ?)");
        mSuggestionArgs.putStringArray(ContactsTableLoader.ARG_SELECTION_ARGS, new String[]{"%"});
        mSuggestionArgs.putString(ContactsTableLoader.ARG_SORT_ORDER, Label.COLUMN_NAME + " ASC");
    }

    @TargetApi(13)
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            android.graphics.Point size = new android.graphics.Point();
            display.getSize(size);
            mDesiredWidth = size.x;
            mDesiredHeight = size.y;
        } else {
            //noinspection deprecation
            mDesiredWidth = display.getWidth();
            //noinspection deprecation
            mDesiredHeight = display.getHeight();
        }

        final SystemUiHider systemUiHider = ((ImagePagerActivity) getActivity()).getSystemUiHider();
        if (systemUiHider != null) {
            systemUiHider.addOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                @Override
                public void onVisibilityChange(boolean visible) {
                    if (mOverlayDrawable != null) {
                        if (!visible) {
                            mOverlayDrawable.setAlpha(0);
                            final float scale = photoView.getScale();
                            photoView.invalidate();
                            photoView.setScale(scale);
                        } else {
                            mOverlayDrawable.setAlpha(VISIBLE_ALPHA);
                            final float scale = photoView.getScale();
                            photoView.invalidate();
                            photoView.setScale(scale);
                        }
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_singleimage, container, false);

            /*
            problem:
                java.lang.ArrayIndexOutOfBoundsException: length=1; index=1
                at android.support.v4.widget.ViewDragHelper.shouldInterceptTouchEvent(ViewDragHelper.java:1014)
                at android.support.v4.widget.DrawerLayout.onInterceptTouchEvent(DrawerLayout.java:1140)...
                when pinch-zooming

            solution:
                http://www.arthurwang.net/android/arrayindexoutofboundsexception-with-photoview-library-and-drawerlayout
             */
            photoView = (PhotoView) mRootView.findViewById(R.id.image);
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    // x and y are values between 0 and 1,
                    // [0,0] being the top left corner of the photo,
                    // [1,1] being the bottom right corner.

                    mSelectedFaceIndex = 0;
                    if (mFacePositions != null && !mFacePositions.isEmpty()) {
                        float photoX = x * mPhotoDrawable.getBitmap().getWidth();
                        float photoY = y * mPhotoDrawable.getBitmap().getHeight();

                        while (mSelectedFaceIndex < mFacePositions.size() && !mFacePositions.get(mSelectedFaceIndex).contains(photoX, photoY)) {
                            mSelectedFaceIndex++;
                        }

                        if (mSelectedFaceIndex < mFacePositions.size()) {
                            showFaceTag(mFacePositions.get(mSelectedFaceIndex));
                            for (int i = 0; i < mOverlayDrawable.getNumberOfLayers(); i++) {
                                if (i == mSelectedFaceIndex) {
                                    mOverlayDrawable.getDrawable(i).setAlpha(VISIBLE_ALPHA);
                                } else {
                                    mOverlayDrawable.getDrawable(i).setAlpha(0);
                                }
                            }
                        } else {
                            if (mFaceTagStatic.getVisibility() == View.VISIBLE || mFaceTagEditable.getVisibility() == View.VISIBLE) {
                                hideFaceTags();
                            } else {
                                ((ImagePagerActivity) getActivity()).getSystemUiHider().toggle();
                            }
                        }
                    } else {
                        if (mFaceTagStatic.getVisibility() == View.VISIBLE || mFaceTagEditable.getVisibility() == View.VISIBLE) {
                            hideFaceTags();
                        } else {
                            ((ImagePagerActivity) getActivity()).getSystemUiHider().toggle();
                        }
                    }
                }
            });

            mFaceTagStatic = mRootView.findViewById(R.id.faceTagStatic);
            mFaceTagEditable = mRootView.findViewById(R.id.faceTagEditable);
        }

        mFaceTagStatic.setVisibility(View.GONE);
        mFaceTagEditable.setVisibility(View.GONE);

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mImagePath = getArguments().getString(ARG_IMAGE_PATH);
        mImageId = getArguments().getInt(ARG_IMAGE_ID);
        //GlideHelper.loadBigImage(this, mImagePath, mDesiredWidth, mDesiredHeight, photoView);

        mDetector = new FaceDetectorTask(getActivity(), mImageId) {
            @Override
            protected void onPostExecute(List<Contact2Image> contact2ImageList) {
                mDetector = null;
                if (!contact2ImageList.isEmpty()) {
                    mFacesLoader.reset();
                    mFacesLoader.startLoading();
                }
            }
        };

        mSuggestionsLoader = (CursorLoader) getLoaderManager().initLoader(SUGGESTIONS_LOADER_ID, mSuggestionArgs, new ContactsTableLoader(getActivity(), this));

        loadImage();
    }

    private void loadImage() {

        if (mBitmapTarget != null) {
            Glide.clear(mBitmapTarget);
        }

        mBitmapTarget = new BitmapImageViewTarget(photoView) {

            @Override
            public void onResourceReady(android.graphics.Bitmap resource, GlideAnimation<? super android.graphics.Bitmap> glideAnimation) {

                Bundle args = new Bundle();
                args.putString(ImagesTableLoader.ARG_URI_STRING, SPOCContentProvider.IMAGES_URI.buildUpon().appendPath(Contact.TABLE_NAME).build().toString());
                args.putString(ImagesTableLoader.ARG_SELECTION, Contact2Image.COLUMN_IMAGE_ID + "=?");
                args.putStringArray(ImagesTableLoader.ARG_SELECTION_ARGS, new String[]{String.valueOf(mImageId)});
                mFacesLoader = (CursorLoader) getLoaderManager().initLoader(mImageId + 100, args, new ImagesTableLoader(getActivity(), SingleImageFragment.this));

                super.onResourceReady(resource, glideAnimation);
            }

            @Override
            protected void setResource(Bitmap resource) {
                mPhotoDrawable = new BitmapDrawable(getResources(), resource);
                mOverlayDrawable = new LayerDrawable(mLayers);

                if (((ImagePagerActivity) getActivity()).getSystemUiHider().isVisible()) {
                    mOverlayDrawable.setAlpha(VISIBLE_ALPHA);
                } else {
                    mOverlayDrawable.setAlpha(0);
                }

                LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{mPhotoDrawable, mOverlayDrawable});
                photoView.setImageDrawable(layerDrawable);
            }
        };

        Glide.with(this)
                .fromString()
                .asBitmap()
                .fitCenter()
                .override(mDesiredWidth, mDesiredHeight)
                .error(hu.mrolcsi.android.spoc.common.R.drawable.error)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .signature(new StringSignature(mImagePath + "_big"))
                .load(mImagePath)
                .into(mBitmapTarget);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menuShare);

        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        shareActionProvider.setShareIntent(FileUtils.createShareIntent(mImagePath));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        final AlertDialog.Builder builder;
        final Bundle args = new Bundle();

        switch (id) {
            case R.id.menuDetails:

                args.clear();
                args.putString(ARG_IMAGE_PATH, mImagePath);

                final ImageDetailsDialog dialog = new ImageDetailsDialog();
                dialog.setArguments(args);
                dialog.show(getChildFragmentManager(), ImageDetailsDialog.TAG);

                return true;
            case R.id.menuDelete:
                builder = DialogUtils.buildConfirmDialog(getActivity());
                builder.setMessage(R.string.dialog_message_deletePicture)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    final boolean success = FileUtils.deleteFile(mImagePath);
                                    if (success)
                                        Toast.makeText(getActivity(), R.string.message_pictureDeleted, Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getActivity(), R.string.message_pictureNotDeleted, Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    String message;
                                    final AlertDialog.Builder errorBuilder = DialogUtils.buildErrorDialog(getActivity());
                                    if (e instanceof FileNotFoundException) {
                                        message = getString(R.string.message_pictureNotDeleted_fileNotExist);
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    } else {
                                        message = getString(R.string.message_pictureNotDeleted);
                                        if (BuildConfig.DEBUG) message += "\n" + e.toString();
                                    }
                                    errorBuilder.setMessage(message);
                                    errorBuilder.show();
                                }
                            }
                        }).show();
                return true;
            case R.id.menuEditDate:
                DateTimePickerDialog pickerDialog = new DateTimePickerDialog();
                args.clear();
                args.putInt(DateTimePickerDialog.ARG_IMAGE_ID, mImageId);
                pickerDialog.setArguments(args);
                pickerDialog.show(getChildFragmentManager(), DateTimePickerDialog.TAG);
                return true;
            case R.id.menuEditLocation:
                LocationInputDialog locationInputDialog = new LocationInputDialog();
                locationInputDialog.setArguments(getArguments());
                locationInputDialog.show(getChildFragmentManager(), LocationInputDialog.TAG);
                return true;
            case R.id.menuEditTags:
                EditLabelsDialog editLabelsDialog = new EditLabelsDialog();
                editLabelsDialog.setArguments(getArguments());
                editLabelsDialog.show(getChildFragmentManager(), EditLabelsDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Glide.clear(photoView);
        if (mBitmapTarget != null) {
            Glide.clear(mBitmapTarget);
        }
        Glide.get(getActivity()).clearMemory();

        if (mDetector != null) {
            mDetector.cancel(true);
        }

        for (int i = 0; i < mLayers.length; i++) {
            if (mLayers[i].getBitmap() != null) {
                mLayers[i].getBitmap().recycle();
                mLayers[i] = null;

            }
        }
        mOverlayDrawable = null;

        getLoaderManager().destroyLoader(mImageId + 100);
    }

    private void drawFaces() {
        //TODO: draw faces on separate bitmaps/layers?
        if (mFacePositions == null || mFacePositions.isEmpty()) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.background_material_light));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.margin_xsmall));
        paint.setStyle(Paint.Style.STROKE);

        final int cornerRadius = getResources().getDimensionPixelSize(R.dimen.margin_small);

        //foreach face create new bitmap drawable with a rectangle
        //new bitmap
        //new canvas
        //draw rect on canvas
        //create drawable

        mLayers = new BitmapDrawable[mFacePositions.size()];

        for (int i = 0; i < mFacePositions.size(); i++) {
            RectF rect = mFacePositions.get(i);
            final Bitmap bitmap = Bitmap.createBitmap(mPhotoDrawable.getBitmap().getWidth(), mPhotoDrawable.getBitmap().getHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
            mLayers[i] = new BitmapDrawable(getResources(), bitmap);
        }

        mOverlayDrawable = new LayerDrawable(mLayers);
        if (((ImagePagerActivity) getActivity()).getSystemUiHider().isVisible()) {
            mOverlayDrawable.setAlpha(VISIBLE_ALPHA);
        } else {
            mOverlayDrawable.setAlpha(0);
        }

        LayerDrawable finalDrawable = new LayerDrawable(new Drawable[]{
                mPhotoDrawable,
                mOverlayDrawable
        });

        photoView.setImageDrawable(finalDrawable);
    }

    private void showFaceTag(final Contact2Image contact) {
        if (contact == null) {
            hideFaceTags();
            return;
        }

        if (mFaceTagViewHolder == null) {
            mFaceTagViewHolder = new FaceTagViewHolder();
            //find views
            mFaceTagViewHolder.staticImage = (ImageView) mFaceTagStatic.findViewById(R.id.imgProfilePic);
            mFaceTagViewHolder.staticName = (TextView) mFaceTagStatic.findViewById(R.id.tvName);
            mFaceTagViewHolder.staticRemove = (ImageButton) mFaceTagStatic.findViewById(R.id.btnRemove);
            mFaceTagViewHolder.editableName = (AutoCompleteTextView) mFaceTagEditable.findViewById(R.id.etName);
            mFaceTagViewHolder.editableName.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mFaceTagViewHolder.editableName.setThreshold(1);
            mFaceTagViewHolder.editableName.setAdapter(mSuggestionsAdapter);

            //set listeners
            mFaceTagViewHolder.editableName.setOnEditorActionListener(mFaceTagViewHolder.onEditorActionListener);
            mFaceTagViewHolder.editableName.addTextChangedListener(mFaceTagViewHolder.textWatcher);
            mFaceTagViewHolder.editableName.setOnItemClickListener(mFaceTagViewHolder.onItemClickListener);
            mFaceTagViewHolder.staticRemove.setOnClickListener(mFaceTagViewHolder.onRemoveClickListener);
        }

        if (contact.getContactId() > 0) {
            //existing contact
            if (mFaceTagEditable.getVisibility() == View.VISIBLE) {
                final Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                fadeOutAnim.setAnimationListener(new FadeOutAnimationListener(new View[]{mFaceTagEditable}));
                mFaceTagEditable.startAnimation(fadeOutAnim);
            }

            final Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
            fadeInAnim.setAnimationListener(new FadeInAnimationListener(new View[]{mFaceTagStatic}));
            mFaceTagStatic.startAnimation(fadeInAnim);

            mFaceTagViewHolder.staticName.setText(contact.getContactName());
            new ContactPhotoLoader(getActivity(), contact.getContactKey()) {
                @Override
                protected void onPostExecute(Drawable drawable) {
                    mFaceTagViewHolder.staticImage.setImageDrawable(drawable);
                }
            }.execute();
        } else {
            //unknown contact
            if (mFaceTagStatic.getVisibility() == View.VISIBLE) {
                final Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                fadeOutAnim.setAnimationListener(new FadeOutAnimationListener(new View[]{mFaceTagStatic}));
                mFaceTagStatic.startAnimation(fadeOutAnim);
            }

            final Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
            fadeInAnim.setAnimationListener(new FadeInAnimationListener(new View[]{mFaceTagEditable}));
            mFaceTagEditable.startAnimation(fadeInAnim);
            mFaceTagViewHolder.editableName.setText(null);
        }
    }

    private void hideFaceTags() {
        for (int i = 0; i < mOverlayDrawable.getNumberOfLayers(); i++) {
            mOverlayDrawable.getDrawable(i).setAlpha(VISIBLE_ALPHA);
        }

        if (mFaceTagViewHolder != null && mFaceTagViewHolder.editableName != null) {
            GeneralUtils.hideSoftKeyboard(getActivity(), mFaceTagViewHolder.editableName);
        }

        final Animation fadeOutAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        fadeOutAnim.setAnimationListener(new FadeOutAnimationListener(new View[]{mFaceTagStatic, mFaceTagEditable}));
        if (mFaceTagStatic.getVisibility() == View.VISIBLE) {
            mFaceTagStatic.startAnimation(fadeOutAnim);
        }
        if (mFaceTagEditable.getVisibility() == View.VISIBLE) {
            mFaceTagEditable.startAnimation(fadeOutAnim);
        }

        mSelectedFaceIndex = -1;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            mSuggestionsAdapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == mImageId + 100) {
            if (data.getCount() > 0) {
                //get column indices
                int iContactId = data.getColumnIndex(Contact2Image.COLUMN_CONTACT_ID);
                int x1 = data.getColumnIndex(Contact2Image.COLUMN_X1);
                int x2 = data.getColumnIndex(Contact2Image.COLUMN_X2);
                int y1 = data.getColumnIndex(Contact2Image.COLUMN_Y1);
                int y2 = data.getColumnIndex(Contact2Image.COLUMN_Y2);
                int iName = data.getColumnIndex(Contact.COLUMN_NAME);
                int iKey = data.getColumnIndex(Contact.COLUMN_CONTACT_KEY);

                mFacePositions = new ArrayList<>();

                Contact2Image c2i;

                //load faces from cursor
                while (data.moveToNext()) {
                    c2i = new Contact2Image();
                    c2i.set_id(data.getInt(0));
                    c2i.setImageId(mImageId);
                    c2i.setContactId(data.getInt(iContactId));
                    c2i.set(data.getFloat(x1), data.getFloat(y1), data.getFloat(x2), data.getFloat(y2));
                    c2i.setContactName(data.getString(iName));
                    c2i.setContactKey(data.getString(iKey));
                    mFacePositions.add(c2i);
                }
                drawFaces();
                if (mSelectedFaceIndex > -1 && mSelectedFaceIndex < mFacePositions.size()) {
                    showFaceTag(mFacePositions.get(mSelectedFaceIndex));
                }
            } else {
                //detect faces
                if (mFacePositions == null && mDetector != null && mDetector.getStatus() != AsyncTask.Status.RUNNING) {
                    mDetector.execute(mPhotoDrawable.getBitmap());
                }
            }
        }

        if (loader.getId() == SUGGESTIONS_LOADER_ID) {
            if (mSuggestionsAdapter == null) {
                mSuggestionsAdapter = new SuggestionAdapter(getActivity());
            }
            mSuggestionsAdapter.changeCursor(data);
        }
    }

    class FaceTagViewHolder {
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSuggestionsLoader.setSelectionArgs(new String[]{"%" + editable.toString().trim().toUpperCase(Locale.getDefault()) + "%", "%" + editable.toString().trim().toLowerCase(Locale.getDefault()) + "%"});
                mSuggestionsLoader.forceLoad();
            }
        };
        final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mSelectedFaceIndex > -1 && mSelectedFaceIndex < mFacePositions.size()) {
                    final Contact2Image selectedFace = mFacePositions.get(mSelectedFaceIndex);
                    if (selectedFace != null) {
                        selectedFace.setContactId((int) l);
                    }
                }
            }
        };
        ImageButton staticRemove;
        TextView staticName;
        AutoCompleteTextView editableName;
        final TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (mSelectedFaceIndex > -1 && mSelectedFaceIndex < mFacePositions.size()) {
                    Contact2Image selectedFace = mFacePositions.get(mSelectedFaceIndex);
                    if (actionId == EditorInfo.IME_ACTION_DONE && selectedFace != null) {
                        selectedFace.setContactName(textView.getText().toString().trim());

                        //validate
                        if (selectedFace.getContactId() == 0) {
                            //no contact was selected, save new contact? only locally
                            ContentValues values = new ContentValues();
                            values.put(Contact.COLUMN_NAME, textView.getText().toString().trim());
                            values.put(Contact.COLUMN_TYPE, LabelType.CONTACT.name());

                            final Uri insertedUri = getActivity().getContentResolver().insert(SPOCContentProvider.CONTACTS_URI, values);
                            selectedFace.setContactId(Integer.parseInt(insertedUri.getLastPathSegment()));
                        }

                        GeneralUtils.hideSoftKeyboard(getActivity(), mFaceTagViewHolder.editableName);

                        ContentValues values = new ContentValues();
                        values.put(Contact2Image.COLUMN_CONTACT_ID, selectedFace.getContactId());

                        int updateCount;
                        try {
                            updateCount = getActivity().getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.CONTACTS_2_IMAGES_URI, String.valueOf(selectedFace.get_id())), values, null, null);
                        } catch (SQLiteConstraintException e) {
                            Toast.makeText(getActivity(), R.string.singleImage_message_personAlreadyTagged, Toast.LENGTH_SHORT).show();
                            selectedFace.setContactId(-1);
                            return true;
                        }

                        if (updateCount > 0) {
                            Toast.makeText(getActivity(), R.string.singleImage_message_faceTagged, Toast.LENGTH_SHORT).show();
                            mFacesLoader.reset();
                            mFacesLoader.startLoading();
                        } else {
                            Toast.makeText(getActivity(), R.string.singleImage_message_faceNotTagged, Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                }
                return false;
            }
        };
        ImageView staticImage;
        final View.OnClickListener onRemoveClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectedFaceIndex > -1 && mSelectedFaceIndex < mFacePositions.size()) {
                    final Contact2Image selectedFace = mFacePositions.get(mSelectedFaceIndex);
                    if (selectedFace != null) {
                    DialogUtils.buildConfirmDialog(getActivity()).setMessage(R.string.singleImage_confirm_untagFace).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final Cursor cursorWithMinId = getActivity().getContentResolver().query(SPOCContentProvider.CONTACTS_2_IMAGES_URI, new String[]{"min(" + Contact2Image.COLUMN_CONTACT_ID + ")"}, "" + Contact2Image.COLUMN_IMAGE_ID + " = ?", new String[]{String.valueOf(mImageId)}, null);
                            try {
                                if (cursorWithMinId.moveToFirst()) {
                                    int lowestId = Math.min(cursorWithMinId.getInt(0) - 1, -1);

                                    ContentValues values = new ContentValues();
                                    values.put(Contact2Image.COLUMN_CONTACT_ID, lowestId);
                                    final int update = getActivity().getContentResolver().update(Uri.withAppendedPath(SPOCContentProvider.CONTACTS_2_IMAGES_URI, String.valueOf(selectedFace.get_id())), values, null, null);
                                    if (update > 0) {
                                        Toast.makeText(getActivity(), R.string.singleImage_message_faceUntagged, Toast.LENGTH_SHORT).show();

                                        selectedFace.setContactId(lowestId);

                                        mFacesLoader.reset();
                                        mFacesLoader.startLoading();

                                        showFaceTag(selectedFace);
                                    } else {
                                        Toast.makeText(getActivity(), R.string.singleImage_message_faceNotUntagged, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } finally {
                                cursorWithMinId.close();
                            }
                        }
                    }).show();
                }
                }
            }
        };
    }

    class FadeInAnimationListener implements Animation.AnimationListener {

        private final View[] views;

        public FadeInAnimationListener(View[] views) {
            this.views = views;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            for (View view : views) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    class FadeOutAnimationListener implements Animation.AnimationListener {

        private final View[] views;

        FadeOutAnimationListener(View[] views) {
            this.views = views;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            for (View view : views) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
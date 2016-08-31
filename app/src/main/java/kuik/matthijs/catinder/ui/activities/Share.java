package kuik.matthijs.catinder.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.User;
import kuik.matthijs.catinder.ui.fragments.Matches;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.util.Net;

public class Share extends Network implements Matches.Adapter {

    private static final String TAG = "Share";
    private Uri[] images = null;
    private RelativeLayout progressOverlay;
    private TextView progressDetails;
    private CircleImageView receiverImage;
    private CircleImageView profileImage;
    private TextView profileName;
    private GridView gridView;
    private Image.UploadUriToGalleryTask task = null;
    private Matches matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_image);

        initAccount();

        progressOverlay = (RelativeLayout) findViewById(R.id.progress_overlay);
        progressDetails = (TextView) findViewById(R.id.progress_details);
        receiverImage = (CircleImageView) findViewById(R.id.reciever_image);
        gridView = (GridView) findViewById(R.id.gridView);
        profileImage = (CircleImageView) findViewById(R.id.foto);
        profileName = (TextView) findViewById(R.id.share_profile_name);
        LinearLayout header = (LinearLayout) findViewById(R.id.share_profile_me);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMatchClick(Net.getProfile());
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }

        setProfile();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        matches = (Matches) fragment;
    }

    @Override
    public void OnWrongCredentials() {
        super.OnWrongCredentials();
        showLoginActivity();
    }

    @Override
    public void OnLogin() {
        super.OnLogin();
        Log.i(TAG, "Onlogin " + Net.getProfile().getEmail());
        setProfile();
    }

    private void setProfile() {
        profileName.setText(Net.getProfile().getName());
        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                return Image.getBitmap(Share.this, Net.getProfile().getThumb(), Image.SOURCE.SERVER_IF_NOT_IN_CACHE);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) profileImage.setImageBitmap(bitmap);
                Image.notifyTaskEnd();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Image.notifyTaskEnd();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Image.notifyTaskStart();
            }
        };
        task.execute();
    }

    void handleSendImage(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        images = new Uri[]{uri};
        Log.i(TAG, images[0].toString());
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            images = new Uri[imageUris.size()];
            for (int i = 0; i != imageUris.size(); ++i) {
                images[i] = imageUris.get(i);
                Log.i(TAG, images[i].toString());
            }
        }
    }

    private String getPath(Uri uri) {
        String path = null;
        if (uri != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        Log.i(TAG, path);
        return path;
    }

    private void showProgress() {
        progressOverlay.setVisibility(View.VISIBLE);
        gridView.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gridView.setContextClickable(false);
        }
    }

    private void hideProgress() {
        progressOverlay.setVisibility(View.GONE);
        gridView.setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gridView.setContextClickable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public void onMatchClick(final User user) {
        Log.i(TAG, user.toString());
        if (task != null) return;

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return Image.getBitmap(Share.this, user.getThumb(), Image.SOURCE.SERVER_IF_NOT_IN_CACHE);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                receiverImage.setImageBitmap(bitmap);
                Image.notifyTaskEnd();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Image.notifyTaskEnd();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Image.notifyTaskStart();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        }.execute();

        task = new Image.UploadUriToGalleryTask(this, user.getEmail(), images){
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                String desc = "Uploading [" + (values[0] + 1) + "/" + images.length + "]";
                progressDetails.setText(desc);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgress();
                Image.notifyTaskStart();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideProgress();
                finish();
                task = null;
                Image.notifyTaskEnd();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                hideProgress();
                finish();
                task = null;
                Image.notifyTaskEnd();
            }
        };
        task.execute();
    }
}

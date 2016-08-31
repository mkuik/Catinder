package kuik.matthijs.catinder.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kuik.matthijs.catinder.ui.GalleryPreviewView;
import kuik.matthijs.catinder.ui.activities.Gallery;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.util.Net;


public class Profile extends Fragment implements
        View.OnClickListener,
        Net.Adapter,
        EditText.OnEditorActionListener {

    private ImageView profilePicView;
    private EditText naam;
    private TextView email;
    private static final String TAG = "Profile";
    private GalleryPreviewView gallery;

    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int SELECT_PROFILE_PIC_FROM_STORAGE = 2;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 3;
    public static final int MEDIA_TYPE_IMAGE = 4;
    private Uri image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ik, container, false);
        profilePicView = (ImageView) view.findViewById(R.id.foto);
        naam = (EditText) view.findViewById(R.id.naam);
        email = (TextView) view.findViewById(R.id.email);
        gallery = (GalleryPreviewView) view.findViewById(R.id.gallery_preview);

        naam.setEnabled(true);
        naam.setOnEditorActionListener(this);

        profilePicView.setOnClickListener(this);
        gallery.setOnClickListener(this);

        setProfile();
        Net.addSyncListener(this);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Net.removeSyncListener(this);
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Catinder");

        if (!mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    protected boolean hasStoragePermissions() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean hasCameraPermissions() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected void getImage() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getContext());
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        getImageFromGallery();
                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        getImageFromCamera();
                    }
                });
        myAlertDialog.show();
    }

    protected void getImageFromGallery() {
        if (hasStoragePermissions()) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PROFILE_PIC_FROM_STORAGE);
        }
    }

    protected void getImageFromCamera() {
        if (hasCameraPermissions()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            image = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            Log.i(TAG, image.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, image);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "permission result " + requestCode);
        switch (requestCode) {
            case SELECT_PROFILE_PIC_FROM_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                }
                break;
            }
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromCamera();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                onImageResult(data.getData());
            } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (data != null && data.getData() != null) {
                    onImageResult(data.getData());
                } else if (image != null) {
                    onImageResult(image);
                }
            }
        }
    }

    protected void onImageResult(Uri image) {
        this.image = image;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), image);
            bitmap = Image.scaleDown(bitmap, 800);
            profilePicView.setImageBitmap(bitmap);
            if (Net.isConnected()) {
                new Net.EditProfile(bitmap).execute(new JSONObject());
                File file = new File(getContext().getCacheDir(), Net.getProfile().getImage());
                new Image.BitmapToFile(bitmap, file).execute();
            }
        } catch (IOException | NullPointerException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.naam: {
                if (Net.isConnected()) {
                    Net.getProfile().setName(naam.getText().toString());
                    new Net.EditProfile(null).execute(new JSONObject());
                }
                break;
            }
        }
        return false;
    }

    public void setProfile() {
        Log.i(TAG, "set profile bitmap");
        if (Net.isConnected()) {
            naam.setText(Net.getProfile().getName());
            email.setText(Net.getProfile().getEmail());
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    return Image.getBitmap(getContext(), Net.getProfile().getImage(), Image.SOURCE.SERVER_IF_NOT_IN_CACHE);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    profilePicView.setImageBitmap(bitmap);
                }
            }.execute();
            gallery.setGallery(Net.getProfile().getEmail());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gallery_preview: {
                Intent intent = new Intent(getActivity(), Gallery.class);
                intent.putExtra("email", Net.getProfile().getEmail());
                intent.putExtra("name", Net.getProfile().getName());
                startActivity(intent);
                break;
            }
            case R.id.foto: {
                getImage();
            }
        }
    }

    @Override
    public void OnConnectionFailed(JSONObject task) {

    }

    @Override
    public void OnDataSyncEnd() {

    }

    @Override
    public void OnDataSyncStart() {

    }

    @Override
    public void OnConnectionSucces(JSONObject out, JSONObject in) {

    }

    @Override
    public void OnWrongCredentials() {

    }

    @Override
    public void OnLogin() {
        setProfile();
    }
}

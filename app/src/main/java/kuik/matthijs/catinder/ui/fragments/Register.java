package kuik.matthijs.catinder.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kuik.matthijs.catinder.*;
import kuik.matthijs.catinder.Profile;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.util.Net;

/**
 * Created by Matthijs Kuik on 7/21/2016.
 */
public class Register extends Fragment implements View.OnClickListener {

    private static final String TAG = "Register";
    private TextInputEditText name;
    private TextInputEditText email;
    private TextInputEditText password1;
    private TextInputEditText password2;
    private Adapter adapter;
    private ImageView imageView;
    private TextView imageViewErrorMessage;
    private Button registerButton;

    public static final int RESULT_LOAD_IMAGE = 1;
    public static final int SELECT_PROFILE_PIC_FROM_STORAGE = 2;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 3;
    public static final int MEDIA_TYPE_IMAGE = 4;
    private Uri image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        name = (TextInputEditText) view.findViewById(R.id.register_name);
        email = (TextInputEditText) view.findViewById(R.id.register_email);
        password1 = (TextInputEditText) view.findViewById(R.id.register_password);
        password2 = (TextInputEditText) view.findViewById(R.id.register_confirm_password);
        registerButton = (Button) view.findViewById(R.id.register_button);
        imageView = (ImageView) view.findViewById(R.id.picture_view);
        imageViewErrorMessage = (TextView) view.findViewById(R.id.picture_error_message);

        registerButton.setOnClickListener(this);
        imageView.setOnClickListener(this);

        setPicture(image);

        return view;
    }

    public void setPicture(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContext().getContentResolver(), uri);
            bitmap = Image.scaleDown(bitmap, 800);
            imageView.setImageBitmap(bitmap);
        } catch (IOException | NullPointerException e) {
            Log.d(TAG, e.toString());
        }
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
        setPicture(image);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Adapter) {
            adapter = (Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement Register.Connect");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button: {
                String name = this.name.getText().toString();
                String email = this.email.getText().toString();
                String password1 = this.password1.getText().toString();
                String password2 = this.password2.getText().toString();

                boolean valid = true;
                View focusView = null;

                if (!password1.equals(password2)) {
                    valid = false;
                    this.password2.setError(getString(R.string.error_invalid_password));
                    focusView = this.password2;
                }
                if (!isPasswordValid(password1)) {
                    valid = false;
                    this.password1.setError(getString(R.string.error_invalid_password));
                    focusView = this.password1;
                }
                if (!isEmailValid(email)) {
                    valid = false;
                    this.email.setError(getString(R.string.error_invalid_email));
                    focusView = this.email;
                }
                if (TextUtils.isEmpty(name)) {
                    valid = false;
                    this.name.setError(getString(R.string.error_invalid_name));
                    focusView = this.name;
                }
                if (image == null) {
                    valid = false;
                    imageViewErrorMessage.setVisibility(View.VISIBLE);
                    focusView = imageView;
                }

                if (valid) {
                    Log.i(TAG, "VALID");
                    register(name, email, password1, image);
                } else {
                    Log.i(TAG, "INVALID");
                    focusView.requestFocus();
                }
                break;
            }
            case R.id.picture_view: {
                getImage();
                imageViewErrorMessage.setVisibility(View.GONE);
                break;
            }
        }
    }

    public void register(final String name, final String email, final String password, final Uri picture) {
        kuik.matthijs.catinder.Profile profile = new Profile(name, email, -1, password);
        Net.setProfile(profile);
        new Net.UserRegisterTask(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                registerButton.setEnabled(false);
            }

            @Override
            protected void onPostExecute(JSONObject response) {
                super.onPostExecute(response);
                registerButton.setEnabled(true);
            }

            @Override
            protected void onReceivedProfile(kuik.matthijs.catinder.Profile profile) {
                super.onReceivedProfile(profile);
                new Image.UploadPictureUriTask(getContext(), profile.getEmail(), picture).execute();
            }
        }.execute(new JSONObject());
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public interface Adapter {
    }


}

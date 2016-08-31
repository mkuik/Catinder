package kuik.matthijs.catinder.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import kuik.matthijs.catinder.R;

/**
 * Created by Matthijs Kuik on 7/21/2016.
 */
public class EditPicture extends Fragment implements View.OnClickListener {

    private static final String TAG = "EditPicture";
    private ImageView imageView;
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int SELECT_PROFILE_PIC_FROM_STORAGE = 1;
    private Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_picture, container, false);
        imageView = (ImageView) view.findViewById(R.id.picture_view);
        ImageButton editButton = (ImageButton) view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(this);
        return view;
    }

    public boolean hasStoragePermissions() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void selectProfilePic() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "permission result " + requestCode);
        switch (requestCode) {
            case SELECT_PROFILE_PIC_FROM_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectProfilePic();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Log.i(TAG, "Selected image " + picturePath);
                imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                adapter.OnSelectedImage(picturePath);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Adapter) {
            adapter = (Adapter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EditPicture.Connect");
        }
    }

    @Override
    public void onClick(View v) {
        if (hasStoragePermissions()) {
            selectProfilePic();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PROFILE_PIC_FROM_STORAGE);
        }
    }

    public interface Adapter {
        void OnSelectedImage(String path);
    }
}

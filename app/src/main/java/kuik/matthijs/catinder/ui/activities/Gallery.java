package kuik.matthijs.catinder.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toolbar;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.util.GalleryAdapter;
import kuik.matthijs.catinder.util.GalleryItem;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.util.Net;

public class Gallery extends SelectImageWithNetwork implements View.OnClickListener,
        AdapterView.OnItemClickListener{

    private static final String TAG = "Gallery";
    private GalleryAdapter gridAdapter;
    private String email;

    public void getContent() {
        new Net.GetGallery(email) {
            @Override
            protected void onReceivedGallery(GalleryItem[] items) {
                gridAdapter.clear();
                for (int i = 0; i != items.length; ++i) {
                    gridAdapter.insert(items[i], i);
                }
                gridAdapter.notifyDataSetChanged();
            }
        }.execute(new JSONObject());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Intent myIntent = getIntent(); // gets the previously created intent
        email = myIntent.getStringExtra("email");
        String name = myIntent.getStringExtra("name");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(name.isEmpty() ? email : name);
            setActionBar(toolbar);
        }

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GalleryAdapter(this, R.layout.gallery_item, new ArrayList<GalleryItem>());
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        getContent();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                getImage();
                break;
            }
        }
    }

    @Override
    protected void onImageResult(Uri image) {
        super.onImageResult(image);
        upload(image);
    }

    public void upload(String path) {
        Log.i(TAG, "upload " + path);
        new Image.UploadToGalleryTask(email, path) {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getContent();
            }
        }.execute();
    }

    public void upload(Uri image) {
        Log.i(TAG, "upload " + image.toString());
        new Image.UploadUriToGalleryTask(this, email, image) {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getContent();
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GalleryItem image = gridAdapter.getItem(position);
        File file = new File(getCacheDir(), image.getImage());
        openImage(file);
    }

    public void openImage(File file) {
        Uri uri = FileProvider.getUriForFile(this, "kuik.matthijs.catinder", file);
        Log.i(TAG, uri.toString());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
}

package kuik.matthijs.catinder.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONObject;

import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.util.GalleryItem;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.util.Net;

/**
 * Created by Matthijs Kuik on 8/12/2016.
 */
public class GalleryPreviewView extends LinearLayout {

    private SquareImageView[] imgs;
    private static final String TAG = "GalleryPreviewView";

    public GalleryPreviewView(Context context) {
        super(context);
        init();
    }

    public GalleryPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GalleryPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.gallery_preview, this);
        imgs = new SquareImageView[3];
        imgs[0] = (SquareImageView) findViewById(R.id.img1);
        imgs[1] = (SquareImageView) findViewById(R.id.img2);
        imgs[2] = (SquareImageView) findViewById(R.id.img3);
    }

    public void setGallery(String email) {
        Net.GetGallery task = new Net.GetGallery(email) {
            @Override
            protected void onReceivedGallery(GalleryItem[] items) {
                for (int i = 0; i != 3; i++) {
                    int img = items.length - 1 - i;
                    if (img < 0) break;
                    new LoadImage(getContext(), items[img].getImage(), imgs[i]).execute();
                }
            }
        };
        task.execute(new JSONObject());
    }

    private class LoadImage extends AsyncTask<Void, Void, Bitmap> {

        SquareImageView view;
        String image;
        Context context;

        public LoadImage(Context context, String image, SquareImageView view) {
            this.image = image;
            this.view = view;
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = Image.getBitmap(context, image, Image.SOURCE.SERVER_IF_NOT_IN_CACHE);
            bitmap = Image.scaleDown(bitmap, 400);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.i(TAG, bitmap != null ? bitmap.toString() : "null");
            view.setImageBitmap(bitmap);
        }
    }
}

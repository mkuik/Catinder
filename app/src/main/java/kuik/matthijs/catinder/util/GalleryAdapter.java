package kuik.matthijs.catinder.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import kuik.matthijs.catinder.R;

/**
 * Created by Matthijs Kuik on 7/26/2016.
 */
public class GalleryAdapter extends ArrayAdapter<GalleryItem> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<GalleryItem> data = new ArrayList<>();

    public GalleryAdapter(Context context, int layoutResourceId, ArrayList<GalleryItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.bar = (ProgressBar) row.findViewById(R.id.progress_indicator);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        GalleryItem item = data.get(position);
        holder.imageTitle.setText(new File(item.getImage()).getName());
        holder.image.setImageBitmap(null);
        new LoadImage(item.getImage(), holder).execute();
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
        ProgressBar bar;
    }

    class LoadImage extends AsyncTask<Void, Void, Bitmap> {

        ViewHolder view;
        String image;

        public LoadImage(String image, ViewHolder view) {
            this.image = image;
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            view.bar.setVisibility(View.VISIBLE);
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
            view.bar.setVisibility(View.GONE);
            view.image.setImageBitmap(bitmap);
        }
    }
}
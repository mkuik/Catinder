package kuik.matthijs.catinder.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.User;

public class UserAdapter extends ArrayAdapter<User> {
    private int layoutResourceId;
    private ImageSize imageSize;
    private static final String TAG = "UserAdapter";

    public UserAdapter(final Context context, int layoutResourceId, ImageSize imageSize) {
        super(context, layoutResourceId);
        this.layoutResourceId = layoutResourceId;
        this.imageSize = imageSize;
    }

    public int getLayoutResourceId() {
        return layoutResourceId;
    }

    public ImageSize getImageSize() {
        return imageSize;
    }

    public void remove(int index) {
        super.remove(getItem(index));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(getLayoutResourceId(), parent, false);
            holder = new ViewHolder();
            holder.naam = (TextView) row.findViewById(R.id.naam);
            holder.foto = (ImageView) row.findViewById(R.id.foto);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final User item = getItem(position);
        holder.naam.setText(item.getName().isEmpty() ? item.getEmail() : item.getName());

        switch (imageSize) {
            case NONE:
                break;
            case SMALL:
                new LoadImage(item.getThumb(), holder.foto).execute();
                break;
            case LARGE:
                new LoadImage(item.getImage(), holder.foto).execute();
                break;
        }

        Log.i(TAG, "getview " + item.toString());

        return row;
    }

    public enum ImageSize {
        NONE,
        SMALL,
        LARGE
    }

    public class LoadImage extends AsyncTask<Void, Void, Bitmap> {

        ImageView view;
        String image;

        public LoadImage(String image, ImageView view) {
            this.image = image;
            this.view = view;
        }

        public String getImage() {
            return image;
        }

        public ImageView getView() {
            return view;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return Image.getBitmap(getContext(), image, Image.SOURCE.SERVER_IF_NOT_IN_CACHE);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) view.setImageBitmap(bitmap);
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
    }

    static class ViewHolder {
        ImageView foto;
        TextView naam;
    }

}
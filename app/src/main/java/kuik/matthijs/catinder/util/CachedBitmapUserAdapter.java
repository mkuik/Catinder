package kuik.matthijs.catinder.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.User;

public class CachedBitmapUserAdapter extends UserAdapter {
    private static final String TAG = "CachedBitmapUserAdapter";
    private Bitmap cache = null;

    public CachedBitmapUserAdapter(final Context context, int layoutResourceId, ImageSize imageSize) {
        super(context, layoutResourceId, imageSize);
    }

    public void clearCache() {
        cache = null;
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

        if (position == 0 && cache != null) {
            holder.foto.setImageBitmap(cache);
            cache = null;
        } else {
            switch (getImageSize()) {
                case NONE:
                    break;
                case SMALL:
                    new LoadImage(item.getThumb(), holder.foto, position).execute();
                    break;
                case LARGE:
                    new LoadImage(item.getImage(), holder.foto, position).execute();
                    break;
            }
        }

        return row;
    }

    public class LoadImage extends UserAdapter.LoadImage {

        int position;

        public LoadImage(String image, ImageView view, int position) {
            super(image, view);
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (getPosition() == 1) cache = bitmap;
        }
    }

}
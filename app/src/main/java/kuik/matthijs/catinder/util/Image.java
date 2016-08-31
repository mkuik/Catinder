package kuik.matthijs.catinder.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static android.graphics.Bitmap.createScaledBitmap;

public class Image {

    private static final String TAG = "Image";
    private static final Object LOCK = new Object();
    private static Set<Adapter> listeners = new HashSet<>();
    private static final int IMG_COMPRESSION = 99;
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int MAX_IMG_DIMENS = 1000;

    private static String hostname;
    private static int port;

    public interface Adapter {
        void OnImageSyncStart();
        void OnImageSyncEnd();
    }

    public static InetSocketAddress getServer() {
        return new InetSocketAddress(hostname, port);
    }

    public static void setServer(String hostname, int port) {
        Image.hostname = hostname;
        Image.port = port;
    }

    public static void addTaskListener(Adapter adapter) {
        listeners.add(adapter);
    }

    public static void removeTaskListener(Adapter adapter) {
        listeners.remove(adapter);
    }

    public static void notifyTaskStart() {
        for (Adapter adapter : listeners) adapter.OnImageSyncStart();
    }

    public static void notifyTaskEnd() {
        for (Adapter adapter : listeners) adapter.OnImageSyncEnd();
    }

    public static void upload(String email, Bitmap bitmap) {
        synchronized (LOCK) {
            notifyTaskStart();
            try {
                final InetSocketAddress server = getServer();
                Socket socket = new Socket();
                socket.connect(server, CONNECTION_TIMEOUT);
                OutputStream out = socket.getOutputStream();
                JSONObject json = new JSONObject();
                json.put("cmd", "UPLOAD");
                json.put("email", email);
                out.write((json.toString() + "\n").getBytes());
                bitmap.compress(Bitmap.CompressFormat.PNG, IMG_COMPRESSION, out);
                Log.i(TAG, "image sent");
                out.close();
                socket.close();
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.toString());
            }
            notifyTaskEnd();
        }
    }

    public static void uploadToGallery(String email, Bitmap bitmap) {
        synchronized (LOCK) {
            notifyTaskStart();
            try {
                final InetSocketAddress server = getServer();
                Socket socket = new Socket();
                socket.connect(server, CONNECTION_TIMEOUT);
                OutputStream out = socket.getOutputStream();
                JSONObject json = new JSONObject();
                json.put("cmd", "ADD_TO_GALLERY");
                json.put("email", email);
                out.write((json.toString() + "\n").getBytes());
                bitmap.compress(Bitmap.CompressFormat.PNG, IMG_COMPRESSION, out);
                Log.i(TAG, "image sent");
                out.close();
                socket.close();
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.toString());
            }
            notifyTaskEnd();
        }
    }

    public static void download(String image, File output) {
        synchronized (LOCK) {
            notifyTaskStart();
            try {
                final InetSocketAddress server = getServer();
                Socket socket = new Socket();
                socket.connect(server, CONNECTION_TIMEOUT);
                OutputStream out = socket.getOutputStream();
                JSONObject json = new JSONObject();
                json.put("cmd", "DOWNLOAD");
                json.put("image", image);
                out.write((json.toString() + "\n").getBytes());
                socket.shutdownOutput();
                output.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(output);
                InputStream in = socket.getInputStream();
                IOUtils.copy(in, fos);
                Log.i(TAG, output.getPath() + " stored in cache");
                in.close();
                out.close();
                socket.close();
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.toString());
            }
            notifyTaskEnd();
        }
    }

    public enum SOURCE {
        CACHE,
        SERVER,
        SERVER_IF_NOT_IN_CACHE
    }

    public static Bitmap getBitmap(Context context, String image, SOURCE mode) {
        File file = new File(context.getCacheDir(), image);
        Bitmap bitmap = null;
        switch (mode) {
            case CACHE:
                if (file.exists()) bitmap = BitmapFactory.decodeFile(file.getPath());
                break;
            case SERVER:
                download(image, file);
                if (file.exists()) bitmap = BitmapFactory.decodeFile(file.getPath());
                break;
            case SERVER_IF_NOT_IN_CACHE:
                if (file.exists()) bitmap = BitmapFactory.decodeFile(file.getPath());
                if (bitmap == null) {
                    download(image, file);
                    bitmap = BitmapFactory.decodeFile(file.getPath());
                }
                break;
        }
        return bitmap;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize) {
        if (realImage == null) return null;
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        ratio = ratio > 1 ? 1 : ratio;
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return createScaledBitmap(realImage, width, height, true);
    }

    public static class BitmapFromFile extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
            bitmap = Image.scaleDown(bitmap, MAX_IMG_DIMENS);
            return bitmap;
        }
    }

    public static class BitmapToFile extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "BitmapToFile";
        private Bitmap bitmap;
        private File file;

        public BitmapToFile(Bitmap bitmap, File file) {
            this.bitmap = bitmap;
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public static class UploadPictureTask extends AsyncTask<Void, Void, Void> {

        private String email;
        private String picture;

        public UploadPictureTask(String email, String picture) {
            this.email = email;
            this.picture = picture;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Bitmap bitmap = BitmapFactory.decodeFile(picture);
            if (bitmap != null) Image.upload(email, bitmap);
            return null;
        }
    }

    public static class UploadPictureUriTask extends AsyncTask<Void, Void, Void> {

        private String email;
        private Uri picture;
        private Context context;

        public UploadPictureUriTask(Context context, String email, Uri picture) {
            this.email = email;
            this.picture = picture;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), picture);
                bitmap = scaleDown(bitmap, MAX_IMG_DIMENS);
                Image.upload(email, bitmap);
            } catch (IOException | NullPointerException e) {
                Log.d(TAG, e.toString());
            }
            return null;
        }
    }

    public static class UploadToGalleryTask extends AsyncTask<Void, Integer, Void> {

        private String email;
        private String[] pictures;

        public UploadToGalleryTask(String email, String picture) {
            this.email = email;
            this.pictures = new String[]{picture};
        }

        public UploadToGalleryTask(String email, String[] pictures) {
            this.email = email;
            this.pictures = pictures;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i != pictures.length; ++i) {
                publishProgress(i);
                Bitmap bitmap = BitmapFactory.decodeFile(pictures[i]);
                if (bitmap != null) {
                    bitmap = scaleDown(bitmap, MAX_IMG_DIMENS);
                    Image.uploadToGallery(email, bitmap);
                }
            }
            return null;
        }
    }

    public static class UploadUriToGalleryTask extends AsyncTask<Void, Integer, Void> {

        private String email;
        private Uri[] pictures;
        private Context context;

        public UploadUriToGalleryTask(Context context, String email, Uri picture) {
            this.email = email;
            this.pictures = new Uri[]{picture};
            this.context = context;
        }

        public UploadUriToGalleryTask(Context context, String email, Uri[] pictures) {
            this.email = email;
            this.pictures = pictures;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i != pictures.length; ++i) {
                publishProgress(i);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),pictures[i]);
                    bitmap = scaleDown(bitmap, MAX_IMG_DIMENS);
                    Image.uploadToGallery(email, bitmap);
                } catch (IOException | NullPointerException e) {
                    Log.d(TAG, e.toString());
                }
            }
            return null;
        }
    }

}

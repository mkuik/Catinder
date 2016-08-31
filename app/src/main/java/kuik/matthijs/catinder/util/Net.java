package kuik.matthijs.catinder.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import kuik.matthijs.catinder.Profile;
import kuik.matthijs.catinder.User;

/**
 * Created by Matthijs Kuik on 1-12-2015.
 */
public class Net {

    private static final String TAG = "Net";
    private static final Object LOCK = new Object();
//    private static InetSocketAddress server = null;
    private static List<Adapter> listeners = new ArrayList<>();
    private static Profile profile;
    private static String hostname;
    private static int port;

    public interface Adapter {
        void OnDataSyncStart();
        void OnDataSyncEnd();
        void OnConnectionSucces(JSONObject out, JSONObject in);
        void OnWrongCredentials();
        void OnConnectionFailed(JSONObject task);
        void OnLogin();
    }

    public static boolean isReady() {
        return getProfile() != null && getServer() != null;
    }

    public static boolean isConnected() {
        return isReady() && getProfile().isConnected();
    }

    public static Profile getProfile() {
        return profile;
    }

    public static void setProfile(Profile profile) {
        Net.profile = profile;
    }

    public static InetSocketAddress getServer() {
        return new InetSocketAddress(hostname, port);
    }

    public static void setServer(String hostname, int port) {
        Net.hostname = hostname;
        Net.port = port;
    }

    public static void addSyncListener(Adapter adapter) {
        listeners.add(adapter);
    }

    public static void removeSyncListener(Adapter adapter) {
        listeners.remove(adapter);
    }

    public static void notifyLogin() {
        for (Adapter adapter : listeners) adapter.OnLogin();
    }

    protected static void notifySyncStart() {
        for (Adapter adapter : listeners) adapter.OnDataSyncStart();
    }

    protected static void notifySyncEnd() {
        for (Adapter adapter : listeners) adapter.OnDataSyncEnd();
    }

    protected static void notifyConnectionSucces(JSONObject out, JSONObject in) {
        for (Adapter adapter : listeners) adapter.OnConnectionSucces(out, in);
    }

    protected static void notifyWrongCredentials() {
        for (Adapter adapter : listeners) adapter.OnWrongCredentials();
    }

    protected static void notifyConnectionFailed(JSONObject out) {
        for (Adapter adapter : listeners) adapter.OnConnectionFailed(out);
    }

    protected static JSONObject sync(InetSocketAddress server, JSONObject message) {
        if (server == null) return null;
        JSONObject response = null;
        synchronized (LOCK) {
            try {
                Socket socket = new Socket();
                socket.connect(server, 5000);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                write(out, message.toString());
//                Log.i(TAG, "message sent " + message.toString());
                socket.shutdownOutput();
                String response_str = read(in);
//                Log.i(TAG, "response received " + response_str);
                out.close();
                in.close();
                socket.close();
                response = new JSONObject(response_str);
            } catch (IOException | JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
        return response;
    }

    protected static void write(OutputStream out, final String message) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw, 8192);
        PrintWriter pw = new PrintWriter(bw);
        pw.println(message);
        pw.flush();
    }

    protected static String read(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8192);
        String str;
        StringBuilder sb = new StringBuilder(8192);
        while ((str = r.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static class Sync extends AsyncTask<JSONObject, Void, JSONObject> {

        private static final String TAG = "Sync";
        private JSONObject in;
        private JSONObject out;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notifySyncStart();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            out = params[0];
            try {
                out.put("profile", getProfile().toJSONSummary());
                final InetSocketAddress server = getServer();
                Log.d(TAG, server.toString() + " OUT " + out.toString());
                in = sync(server, out);
                if (in != null) Log.d(TAG, server.toString() + " IN  " + in.toString());
                else Log.d(TAG, server.toString() + " IN  null");
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            return in;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject == null) onFailed();
            else onConnected();
            notifySyncEnd();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            notifySyncEnd();
        }

        protected void onFailed() {
            notifyConnectionFailed(out);
        }

        protected void onConnected() {
            notifyConnectionSucces(out, in);
        }
    }

    public static class AuthenticatedSync extends Sync {

        private static final String TAG = "AuthenticatedSync";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            if (getProfile() != null && getProfile().isConnected()) {
                JSONObject json = params[0];
                json = super.doInBackground(json);
                return json;
            } else {
                Log.e(TAG, "Account not connected");
                return null;
            }
        }
    }

    public static class UserLoginTask extends Sync {

        private static final String TAG = "UserLoginTask";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "LOGIN");
                json = super.doInBackground(json);
                return json;
            } catch (NullPointerException | JSONException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            try {
                JSONObject jsonUser = response.getJSONObject("user");
                jsonUser.put("password", getProfile().getPassword());
                jsonUser.put("connected", true);
                Profile profile = new Profile(jsonUser);
                onReceivedProfile(profile);
            } catch (JSONException ignore) {
                notifyWrongCredentials();
            } catch (NullPointerException ignore) {}
        }

        protected void onReceivedProfile(Profile profile) {
            setProfile(profile);
            notifyLogin();
        }
    }

    public static class UserRegisterTask extends Sync {

        private static final String TAG = "UserRegisterTask";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = new JSONObject();
                json.put("cmd", "REGISTER");
                json.put("name", getProfile().getName());
                json.put("email", getProfile().getEmail());
                json.put("password", getProfile().getPassword());
                json = super.doInBackground(json);
                return json;
            } catch (NullPointerException | JSONException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            try {
                JSONObject jsonUser = response.getJSONObject("user");
                jsonUser.put("password", getProfile().getPassword());
                jsonUser.put("connected", true);
                Profile p = new Profile(jsonUser);
                onReceivedProfile(p);
            } catch (JSONException ignore) {
                notifyWrongCredentials();
            } catch (NullPointerException ignore) {}
        }

        protected void onReceivedProfile(Profile profile) {
            setProfile(profile);
            notifyLogin();
        }
    }

    public static abstract class GetRandomUser extends AuthenticatedSync {

        private static final String TAG = "GetRandomUser";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "GET_RANDOM_USER");
                json = super.doInBackground(json);
                return json;
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    User user = new User(response.getJSONObject("user"));
                    Log.i(TAG, "New candidate " + user.toString());
                    onReceivedUser(user);
                } catch (JSONException e) {
                    Log.e(TAG, "New candidate " + e.toString());
                    onNoUserAvailable();
                }
            }
        }

        protected abstract void onReceivedUser(User user);

        protected abstract void onNoUserAvailable();
    }

    public static class AddLike extends AuthenticatedSync {

        private static final String TAG = "AddLike";
        private int like;

        public AddLike(int like) {
            this.like = like;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "ADD_LIKE");
                json.put("like", like);
                json = super.doInBackground(json);
                return json;
            } catch(JSONException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public static class AddDislike extends AuthenticatedSync {

        private static final String TAG = "AddDislike";
        private int dislike;

        public AddDislike(int like) {
            this.dislike = like;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "ADD_DISLIKE");
                json.put("dislike", dislike);
                json = super.doInBackground(json);
                return json;
            } catch(JSONException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public static class ReShuffle extends AuthenticatedSync {

        private static final String TAG = "ReShuffle";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "RESHUFFLE");
                json = super.doInBackground(json);
                return json;
            } catch(JSONException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public static abstract class GetMatches extends AuthenticatedSync {

        private static final String TAG = "GetMatches";

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "GET_MATCHES");
                json = super.doInBackground(json);
                return json;
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            try {
                JSONArray array = response.getJSONArray("matches");
                User[] matches = new User[array.length()];
                for (int i = 0; i != array.length(); ++i) {
                    matches[i] = new User(array.getJSONObject(i));
                }
                onReceivedMatches(matches);
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }
        }

        protected abstract void onReceivedMatches(User[] matches);
    }

    public static class EditProfile extends AuthenticatedSync {

        private Bitmap bitmap;
        private static final String TAG = "EditProfile";

        public EditProfile(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... bitmaps) {
            try {
                JSONObject json = new JSONObject();
                json.put("cmd", "EDIT_PROFILE");
                json = super.doInBackground(json);
                if (bitmap != null) {
                    Image.upload(getProfile().getEmail(), bitmap);
                }
                return json;
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }


    public static abstract class GetGallery extends AuthenticatedSync {

        private String email;
        private static final String TAG = "GetGallery";

        public GetGallery(String email) {
            this.email = email;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                JSONObject json = params[0];
                json.put("cmd", "GET_GALLERY");
                json.put("gallery", email);
                json = super.doInBackground(json);
                return json;
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject json) {
            super.onPostExecute(json);
            if (json != null) {
                new AsyncTask<Void, Void, GalleryItem[]>() {
                    @Override
                    protected GalleryItem[] doInBackground(Void... params) {
                        try {
                            JSONArray array = json.getJSONArray("gallery");
                            GalleryItem[] items = new GalleryItem[array.length()];
                            for (int i = 0; i != array.length(); ++i) {
                                items[i] = new GalleryItem(array.getString(i));
                            }
                            return items;
                        } catch (JSONException e) {
                            return new GalleryItem[0];
                        }
                    }

                    @Override
                    protected void onPostExecute(GalleryItem[] galleryItems) {
                        super.onPostExecute(galleryItems);
                        onReceivedGallery(galleryItems);
                    }
                }.execute();
            }
        }

        protected abstract void onReceivedGallery(GalleryItem[] items);
    }















}


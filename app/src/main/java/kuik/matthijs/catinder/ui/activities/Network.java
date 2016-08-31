package kuik.matthijs.catinder.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;

import kuik.matthijs.catinder.Globals;
import kuik.matthijs.catinder.Profile;
import kuik.matthijs.catinder.R;
import kuik.matthijs.catinder.util.Image;
import kuik.matthijs.catinder.util.Net;

/**
 * Created by Matthijs Kuik on 7/27/2016.
 */
public class Network extends FragmentActivity implements Image.Adapter, Net.Adapter {

    private static final String TAG = "Network";
    private static final Object SYNC_LOCK = new Object();
    private static final Object IMAGE_LOCK = new Object();
    private static int syncCount = 0;
    private static InetSocketAddress syncServer = null;
    private static InetSocketAddress imageServer = null;

    Snackbar snackbar = null;

    public Network() {
        Image.addTaskListener(this);
        Net.addSyncListener(this);
    }

    public void initAccount() {
        if (!Net.isConnected()) {
            loadProfile();
            if (!Net.isConnected()) {
                showLoginActivity();
            } else {
                Net.notifyLogin();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Image.removeTaskListener(this);
        Net.removeSyncListener(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = findViewById(android.R.id.content);
        snackbar = Snackbar.make(view, "Can't connect to syncServer", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Retry connect...");
                new Net.UserLoginTask().execute(new JSONObject());
            }
        });
        setupServerAddress();
    }



    public void saveProfile() {
        Log.d(TAG, "saveProfile " + Net.getProfile().toString());
        SharedPreferences pref = getSharedPreferences(Globals.SP_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        try {
            editor.putString("profile", Net.getProfile().toJSON().toString());
        } catch (JSONException e) {
            Log.e(TAG, "saveProfile " + e.toString());
        }
        editor.apply();
    }

    public void loadProfile() {
        Log.d(TAG, "loadProfile");
        SharedPreferences pref = getSharedPreferences(Globals.SP_TAG, Context.MODE_PRIVATE);
        try {
            Net.setProfile(new Profile(new JSONObject(pref.getString("profile", null))));
            Log.i(TAG, "loadProfile " + Net.getProfile().toString());
        } catch (JSONException | NullPointerException e) {
            Log.e(TAG, "loadProfile " + e.toString());
        }
    }

    public void setupServerAddress() {
        final String hostname = getResources().getString(R.string.server_ip);
        final int portSync = getResources().getInteger(R.integer.server_port);
        final int portImage = getResources().getInteger(R.integer.image_port);
        Net.setServer(hostname, portSync);
        Image.setServer(hostname, portImage);
    }

    public void showLoginActivity() {
        Log.d(TAG, "showLoginActivity");
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void OnDataSyncStart() {
        synchronized (this) {
            syncCount++;
            if (syncCount == 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnSyncStart();
                    }
                });
            }
        }
    }

    public void OnDataSyncEnd() {
        synchronized (this) {
            syncCount--;
            if (syncCount == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnSyncEnd();
                    }
                });
            }
        }
    }

    @Override
    public void OnImageSyncStart() {
        synchronized (this) {
            syncCount++;
            if (syncCount == 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnSyncStart();
                    }
                });
            }
        }
    }

    @Override
    public void OnImageSyncEnd() {
        synchronized (this) {
            syncCount--;
            if (syncCount == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnSyncEnd();
                    }
                });
            }
        }
    }

    public void OnSyncStart() {
    }

    public void OnSyncEnd() {
    }

    public void OnConnectionSucces(JSONObject out, JSONObject in) {
        snackbar.dismiss();
    }

    public void OnConnectionFailed(JSONObject task) {
        snackbar.show();
    }

    public void OnWrongCredentials() {
    }

    @Override
    public void OnLogin() {

    }
}

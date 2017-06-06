package io.conex.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.conex.app.dialogs.HSVColorPickerDialog;
import io.conex.brandnewsmarthomeapp.R;

public class MainActivity extends AppCompatActivity {

    private EditText urlText;
    private TextView headline;
    private TextView subText;
    private Button button;
    private ProgressBar progressBar;
    private ImageView animatedCheck;
    private SharedPreferences sharedPref;
    private boolean isSettingsRequest;

    String apiUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isSettingsRequest = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null) isSettingsRequest= extras.getBoolean("coming_from_devices_activity");

        Context context = this.getApplicationContext();
        sharedPref = context.getSharedPreferences(getString(R.string.preferences_file_key), MODE_PRIVATE);

        String urlFromPreferences = sharedPref.getString(getString(R.string.api_url_key), null);

        if (urlFromPreferences != null) {
            apiUrl = urlFromPreferences;
        }

        urlText = (EditText)findViewById(R.id.api_url);
        headline = (TextView) findViewById(R.id.headline);
        subText = (TextView) findViewById(R.id.subtext);
        button = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        animatedCheck = (ImageView) findViewById(R.id.animatedCheck);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            // Perform action on click

            String url = urlText.getText().toString();
            if (!url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                apiUrl = url;
                isAPIReachable(url);
            } else {
                updateUiOnApiReachability(false);
            }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // url is already set
        if (apiUrl != null && !apiUrl.isEmpty()) {
            urlText.setText(apiUrl.replace("http://", "").replace("https://", ""));
            isAPIReachable(apiUrl);
        }
    }

    private URL getURL(String url) {
        if (!url.isEmpty() && URLUtil.isValidUrl(url)) {
            try {
                URL apiUrl = new URL(url);
                return apiUrl;
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    private void updateUiOnDoingNetworkRequest() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        progressBar.setVisibility(View.VISIBLE);
        if (view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        urlText.setFocusable(false);
        urlText.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        urlText.setClickable(false); // user navigates with wheel and selects widget
        urlText.setError(null);
        button.setClickable(false);
    }

    private void updateUiOnApiReachability(boolean isReachable) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        progressBar.setVisibility(View.INVISIBLE);

        urlText.setFocusable(true);
        urlText.setFocusableInTouchMode(true); // user touches widget on phone with touch screen
        urlText.setClickable(true); // user navigates with wheel and selects widget
        button.setClickable(true);

        if (!isReachable) {
            urlText.requestFocus();
            urlText.setError("API is not reachable, please check again!");
            imm.showSoftInput(urlText, InputMethodManager.SHOW_IMPLICIT);
        } else {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.api_url_key), apiUrl);
            editor.commit();

            switchActivity();
        }
    }

    private void isAPIReachable(final String stringURL) {

        Log.d("api", "testing reachability @ "+stringURL);
        updateUiOnDoingNetworkRequest();
        final Context context = this.getApplicationContext();

        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = getURL(stringURL);

                Log.d("api", "From URL host is "+url.getHost()+", path is: "+url.getPath()+", port is "+(url.getPort() == -1 ? url.getDefaultPort() : url.getPort()));

                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    try {
                        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                        urlc.setConnectTimeout(5 * 1000);          // 5 s.

                        urlc.connect();
                        if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                            updateUIOnRequestFinished(true, "");
                        } else {
                            updateUIOnRequestFinished(false, "no positive response code ("+urlc.getResponseCode()+")");
                        }
                    } catch (MalformedURLException e1) {
                        updateUIOnRequestFinished(false, e1.getMessage());
                    } catch (IOException e) {
                        updateUIOnRequestFinished(false, e.getMessage());
                    }
                } else {
                    updateUIOnRequestFinished(false, "no network connection?");
                }
            }

            private void updateUIOnRequestFinished(final boolean isReachable, final String message) {
                Log.d("api", "API is "+(isReachable ? "": "NOT ")+"reachable: "+message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUiOnApiReachability(isReachable);
                    }
                });
            }
        }).start();
    }

    private void switchActivity() {
        Log.d("api", "switching activity ...");

        if (isSettingsRequest) {
            isSettingsRequest = false;
        } else {
            Intent myIntent = new Intent(MainActivity.this, DevicesActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivity(myIntent);
        }
    }
}

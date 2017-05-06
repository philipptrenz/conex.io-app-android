package io.conex.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Device;
import io.swagger.client.model.Devices;
import io.swagger.client.model.Filter;
import io.swagger.client.model.Function;

public class MainActivity extends AppCompatActivity {

    private EditText urlText;
    private TextView headline;
    private TextView subText;
    private Button button;
    private ProgressBar progressBar;
    private ImageView animatedCheck;
    private SharedPreferences sharedPref;

    String apiUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        urlc.setConnectTimeout(3 * 1000);          // 4 s.

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

        Intent myIntent = new Intent(MainActivity.this, DevicesActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    /*
    private void testApi() {
        Log.d("api", "testing api ...");

        new Thread(new Runnable() {

            @Override
            public void run() {

                if (apiUrl != null) {

                    DefaultApi api = new DefaultApi();
                    api.setBasePath(apiUrl);

                    Devices list = null;
                    try {
                        Filter filter = new Filter();

                        Log.d("api", ApiInvoker.serialize(filter));
                        list = api.devicesPost(filter);
                    } catch (Exception e) {

                        if (e.getMessage() != null) {
                            Log.e("api", e.getMessage()+":\n"+e.getCause());
                        } else {
                            Log.e("api", "test failed, unknown cause");
                        }

                    }

                    if (list != null) {
                        for (Device d : list.getDevices()) {
                            for (Function f : d.getFunctions()) {
                                Log.d("api", d.getDeviceId()+" has function "+f.getFunctionId());
                            }
                        }
                    }
                }
            }
        }).start();
    }*/
}

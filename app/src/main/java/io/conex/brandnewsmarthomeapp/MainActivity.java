package io.conex.brandnewsmarthomeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Device;
import io.swagger.client.model.Devices;
import io.swagger.client.model.Filter;
import io.swagger.client.model.Function;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DefaultApi api = new DefaultApi();
        api.setBasePath("http://192.168.0.103:8080/v0");

        Filter filter = new Filter();


        try {
            Devices list = api.devicesPost(filter);

            for (Device d : list.getDevices()) {
                for (Function f : d.getFunctions()) {
                    Log.d("api", d.getDeviceId()+" has function "+f.getFunctionId());
                }
            }

        } catch (ApiException e) {
            Log.e("api", e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}

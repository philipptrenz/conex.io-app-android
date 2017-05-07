package io.conex.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Device;
import io.swagger.client.model.Devices;
import io.swagger.client.model.Dimmer;
import io.swagger.client.model.Filter;
import io.swagger.client.model.Function;
import io.swagger.client.model.OnOff;
import io.swagger.client.model.Patcher;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by philipp on 06.05.17.
 */

public class DevicesAdapter extends ArrayAdapter<Device> {

    private Context context;
    private String url;
    private DevicesFragment fragment;
    private DefaultApi api;
    private ArrayList<Device> devices;

    public DevicesAdapter(Context context, ArrayList<Device> devices, DevicesFragment fragment) {
        super(context, 0, devices);

        this.context = context;
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferences_file_key), MODE_PRIVATE);
        this.url = sharedPref.getString(context.getString(R.string.api_url_key), null);

        this.fragment = fragment;
        this.devices = devices;

        DefaultApi api = new DefaultApi();
        api.setBasePath(url);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Device device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item, parent, false);
        }

        TextView device_id = (TextView) convertView.findViewById(R.id.device_id);
        device_id.setText(device.getDeviceId());

        final Switch onoff = (Switch) convertView.findViewById(R.id.function_onoff);
        onoff.setVisibility(View.GONE);

        final SeekBar dimmer = (SeekBar) convertView.findViewById(R.id.function_slider);
        dimmer.setVisibility(View.GONE);
        dimmer.setMax(255);

        List<Function> functions = device.getFunctions();

        for (final Function f : functions) {
            if (f instanceof OnOff) {
                final OnOff onoff_func = (OnOff) f;
                boolean isOn = onoff_func.getIsOn();

                onoff.setVisibility(View.VISIBLE);
                onoff.setChecked(isOn);

                onoff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("api", "OnOff at device '"+device.getDeviceId()+"' has changed: "+onoff.isChecked());

                        OnOff patch = new OnOff();
                        patch.setIsOn(onoff.isChecked());

                        updateApi(device.getDeviceId(), patch);
                    }
                });

            } else if (f instanceof Dimmer) {

                dimmer.setVisibility(View.VISIBLE);
                final Dimmer dimmer_func = (Dimmer) f;

                dimmer.setProgress(dimmer_func.getValue());

                dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d("api", "Dimmer at device '"+device.getDeviceId()+"' has changed: "+seekBar.getProgress());

                        Dimmer patch = new Dimmer();
                        patch.setValue(seekBar.getProgress());
                        updateApi(device.getDeviceId(), patch);

                        if (seekBar.getProgress() == 0) {
                            onoff.setChecked(false);
                        } else {
                            onoff.setChecked(true);
                        }

                    }
                });

            } else {
                Log.d("api", "found unknown function in device '"+device.getDeviceId()+"', function_id: "+f.getFunctionId());
            }
        }


        return convertView;
    }


    private void updateApi(final String deviceId, final Function patchedFunction) {

        final Patcher patcher = new Patcher();
        Filter filter = new Filter();
        ArrayList<String> devices = new ArrayList<>();
        devices.add(deviceId);
        filter.setDeviceIds(devices);
        patcher.setFilter(filter);

        // workaround for stupid Gson JSON parser, which absorbs function_id while doing polymorphic mapping.
        patchedFunction.setFunctionId(patchedFunction.getClass().getSimpleName().toLowerCase());

        patcher.setFunction(patchedFunction);

        new Thread(new Runnable() {

            @Override
            public void run() {

                if (url != null) {
                    try {
                        Log.d("api", ApiInvoker.serialize(patcher).toString());
                        DefaultApi api = new DefaultApi();
                        api.setBasePath(url);

                        api.devicesPatch(patcher);
                    } catch (ApiException e) {
                        Log.e("api", e.getMessage());
                    } catch (InterruptedException e) {
                        Log.e("api", e.getMessage());
                    } catch (ExecutionException e) {
                        Log.e("api", e.getMessage());
                    } catch (TimeoutException e) {
                        Log.e("api", e.getMessage());
                    }
                }
            }
        }).start();
    }
}
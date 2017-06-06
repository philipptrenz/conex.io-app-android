package io.conex.app.arrayadapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.conex.app.dialogs.HSVColorPickerDialog;
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.ColorDimmer;
import io.swagger.client.model.Device;
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
    private Activity activity;
    private String url;
    private DefaultApi api;
    private ArrayList<Device> devices;
    private Filter filter;
    private HashMap<String, Integer> colorSaver;

    public DevicesAdapter(Context context, Activity activity, ArrayList<Device> devices) {
        super(context, 0, devices);

        this.activity = activity;
        this.context = context;
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferences_file_key), MODE_PRIVATE);
        this.url = sharedPref.getString(context.getString(R.string.api_url_key), null);

        this.devices = devices;

        DefaultApi api = new DefaultApi();
        api.setBasePath(url);

        colorSaver = new HashMap<>();
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

        final Button colorDimmer = (Button) convertView.findViewById(R.id.function_colordimmer);
        colorDimmer.setVisibility(View.GONE);

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

            }
            if (f instanceof ColorDimmer) {

                colorDimmer.setVisibility(View.VISIBLE);
                int currentColor = HSVToColor(((ColorDimmer) f).getHue(), ((ColorDimmer) f).getSaturation(), ((ColorDimmer) f).getValue());
                setButtonBackgroundColor(currentColor, colorDimmer, device.getDeviceId());

                colorDimmer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int currentColor = getButtonBackgroundColor(device.getDeviceId());
                        final HSVColorPickerDialog cpd = new HSVColorPickerDialog(activity, currentColor, new HSVColorPickerDialog.OnColorSelectedListener() {
                            @Override
                            public void colorSelected(Integer color) {
                                int[] hsv = colorToHSV(color);
                                ColorDimmer patch = getColorDimmerPatchFromHSV(hsv);

                                dimmer.setProgress(hsv[2]);
                                setButtonBackgroundColor(color, colorDimmer, device.getDeviceId());

                                updateApi(device.getDeviceId(), patch);
                            }
                        });
                        cpd.setTitle( "Pick a color" );
                        cpd.show();
                    }
                });


            }
            if (f instanceof Dimmer) {

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
                        updateColorFromDimmer(seekBar.getProgress(), colorDimmer, device.getDeviceId());

                        updateApi(device.getDeviceId(), patch);

                        if (seekBar.getProgress() == 0) {
                            onoff.setChecked(false);
                        } else {
                            onoff.setChecked(true);
                        }
                    }
                });

            }
        }


        return convertView;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public ArrayList<Device> getDevices() { return this.devices; }

    private int[] colorToHSV(int color) {
        float[] hsvFloat = new float[3];
        int[] hsv = new int[3];
        Color.colorToHSV(color, hsvFloat);

        hsv[0] = (int) hsvFloat[0];
        hsv[1] = (int)(256*hsvFloat[1]);
        hsv[2] = (int)(256*hsvFloat[2]);

        if (hsv[0] > 359) hsv[0] = 359;
        if (hsv[1] > 255) hsv[1] = 255;
        if (hsv[1] < 0) hsv[1] = 0;
        if (hsv[2] > 255) hsv[2] = 255;
        if (hsv[2] < 0) hsv[2] = 0;

        return hsv;
    }

    private int HSVToColor(int hue, int sat, int val) {
        float[] androidHSV = new float[3];

        androidHSV[0] = (float) hue;
        androidHSV[1] = (float) sat / (float) 256;
        androidHSV[2] = (float) val / (float) 256;

        return Color.HSVToColor(androidHSV);
    }

    private ColorDimmer getColorDimmerPatchFromHSV(int[] hsv) {
        ColorDimmer patch = new ColorDimmer();
        patch.setValue(hsv[2]);
        patch.setHue(hsv[0]);
        patch.setSaturation(hsv[1]);

        return patch;
    }

    private boolean setButtonBackgroundColor(int color, Button b, String deviceId) {

        colorSaver.put(deviceId, color);

        // fix to display always with value = 1 for brighter colors
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1f;
        color = Color.HSVToColor(hsv);

        try {
            LayerDrawable layer2 = (LayerDrawable) b.getBackground();
            GradientDrawable shape = (GradientDrawable) layer2.findDrawableByLayerId(R.id.button_background);
            shape.setColor(color);// set new background color here
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void updateColorFromDimmer(int value, Button b, String deviceId) {
        Integer color = colorSaver.get(deviceId);
        if (color == null || b == null) return;
        int[] hsv = colorToHSV(color);
        int newColor = HSVToColor(hsv[0], hsv[1], value);
        setButtonBackgroundColor(newColor, b, deviceId);
    }

    private int getButtonBackgroundColor(String deviceId) {
        return colorSaver.get(deviceId);
    }

    private void updateApi(final String deviceId, final Function patchedFunction) {

        final Patcher patcher = new Patcher();
        Filter filter = new Filter();
        ArrayList<String> devices = new ArrayList<>();
        devices.add(deviceId);
        filter.setDeviceIds(devices);
        patcher.setFilter(filter);

        // workaround for stupid Gson JSON parser, which absorbs function_id while doing polymorphic mapping.
        patchedFunction.setFunctionId(patchedFunction.getClass().getSimpleName());

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
                        Log.e("api", e.getMessage() != null ? e.getMessage() : "");
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Log.e("api", e.getMessage() != null ? e.getMessage() : "");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        Log.e("api", e.getMessage() != null ? e.getMessage() : "");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        Log.e("api", e.getMessage() != null ? e.getMessage() : "");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
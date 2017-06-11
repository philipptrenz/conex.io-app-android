package io.conex.app.datamodels;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import io.conex.app.arrayadapters.DevicesAdapter;
import io.conex.app.dialogs.HSVColorPickerDialog;
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.ColorDimmer;
import io.swagger.client.model.ColorTemperature;
import io.swagger.client.model.Device;
import io.swagger.client.model.Dimmer;
import io.swagger.client.model.Filter;
import io.swagger.client.model.Function;
import io.swagger.client.model.OnOff;
import io.swagger.client.model.Patcher;
import io.swagger.client.model.Temperature;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static io.conex.brandnewsmarthomeapp.R.id.view;

/**
 * Created by philipp on 08.05.17.
 */

public class MasterFunction {

    private Activity activity;
    private DevicesAdapter adapter;
    private String apiUrl;

    private LinearLayout wrapper;
    private Button masterOnoffOn;
    private Button masterOnoffOff;
    private SeekBar masterDimmer;
    private Button masterColorDimmer;

    private int masterColorDimmerColorValue = 0;

    private enum UpdateMode { ONOFF, DIMMER, COLORDIMMER };

    private UpdateMode currentFunctionMode;

    public MasterFunction(View view, final DevicesAdapter adapter, Activity activity) {
        this.adapter = adapter;
        this.activity = activity;

        Context context = view.getContext();
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preferences_file_key), MODE_PRIVATE);
        this.apiUrl = sharedPref.getString(context.getString(R.string.api_url_key), "");

        this.wrapper = (LinearLayout) view.findViewById(R.id.function_master);

        this.masterOnoffOn = (Button) view.findViewById(R.id.master_onoff_on);
        masterOnoffOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnOff update = new OnOff();
                update.setIsOn(true);
                updateApi(update);

                updateDeviceFunctionStates(update, UpdateMode.ONOFF);
            }
        });

        this.masterOnoffOff = (Button) view.findViewById(R.id.master_onoff_off);
        masterOnoffOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnOff update = new OnOff();
                update.setIsOn(false);
                updateApi(update);

                updateDeviceFunctionStates(update, UpdateMode.ONOFF);
            }
        });

        this.masterDimmer = (SeekBar) view.findViewById(R.id.master_dimmer);
        masterDimmer.setMax(255);
        masterDimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Dimmer update = new Dimmer();
                    update.setValue(progress);
                    updateDeviceFunctionStates(update, UpdateMode.DIMMER);
                    //Log.d("debug", "onSeekBarChangeListener invoked");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();

                Dimmer update = new Dimmer();
                update.setValue(value);
                updateApi(update);

                //updateDeviceFunctionStates(update);
                //Log.d("debug", "onStopTrackingTouch invoked");
            }
        });

        this.masterColorDimmer = (Button) view.findViewById(R.id.master_colordimmer);
        setButtonBackgroundColor(getAverageColorValue());
        final Activity currentActivity = activity;
        masterColorDimmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            int currentColor = getButtonBackgroundColor();
            final HSVColorPickerDialog cpd = new HSVColorPickerDialog(currentActivity, currentColor, new HSVColorPickerDialog.OnColorSelectedListener() {
                @Override
                public void colorSelected(Integer color) {
                    int[] hsv = colorToHSV(color);

                    ColorDimmer update = new ColorDimmer();
                    update.setHue(hsv[0]);
                    update.setSaturation(hsv[1]);
                    update.setValue(null);

                    updateApi(update);
                    updateDeviceFunctionStates(update, UpdateMode.COLORDIMMER);
                }
            });
            cpd.show();
            }
        });

        hideAll();
    }

    private void hideAll() {
        wrapper.setVisibility(GONE);
        masterOnoffOff.setVisibility(GONE);
        masterOnoffOn.setVisibility(GONE);
        masterDimmer.setVisibility(GONE);
        masterDimmer.setProgress(0);
        masterColorDimmer.setVisibility(GONE);
    }

    public void update(List<Device> devicesList) {
        hideAll();
        Mode mode = FilterContainer.getInstance().getMode();

        switch(mode) {
            case FUNCTIONS:
                showFunctionsOnFilter(FilterContainer.getInstance().getFunctionIds());
                break;

            default:
                Set<String> functionIds = new HashSet<>();
                for (Device d : devicesList) {
                    for (Function f : d.getFunctions()) {

                        if (f instanceof OnOff) functionIds.add(OnOff.class.getSimpleName());
                        if (f instanceof Dimmer) functionIds.add(Dimmer.class.getSimpleName());
                        if (f instanceof ColorDimmer) functionIds.add(ColorDimmer.class.getSimpleName());
                        if (f instanceof ColorTemperature) functionIds.add(ColorTemperature.class.getSimpleName());
                        if (f instanceof Temperature) functionIds.add(Temperature.class.getSimpleName());

                        if (functionIds.size() == 5) break;
                    }
                }
                showFunctionsOnFilter(functionIds);
                break;
        }
    }

    private void showFunctionsOnFilter(Collection<String> functionIds) {
        functionIds.removeAll(Collections.singleton(null));

        if( functionIds.contains("OnOff")) {
            //setFilterWithFunction("onoff");
            wrapper.setVisibility(View.VISIBLE);
            masterOnoffOn.setVisibility(View.VISIBLE);
            masterOnoffOff.setVisibility(View.VISIBLE);

            if (functionIds.size() == 1) {
                currentFunctionMode = UpdateMode.ONOFF;
            }
        }

        if (functionIds.contains("ColorDimmer")) {
            wrapper.setVisibility(View.VISIBLE);
            masterColorDimmer.setVisibility(View.VISIBLE);
            masterDimmer.setVisibility(View.VISIBLE);
            masterDimmer.setProgress(getAverageDimmerValue());
            setButtonBackgroundColor(getAverageColorValue());

            if (functionIds.size() == 1) {
                currentFunctionMode = UpdateMode.COLORDIMMER;
            }
        }

        if (functionIds.contains("Dimmer")) {
            //setFilterWithFunction("dimmer");
            wrapper.setVisibility(View.VISIBLE);
            masterDimmer.setVisibility(View.VISIBLE);
            masterDimmer.setProgress(getAverageDimmerValue());

            if (functionIds.size() == 1) {
                currentFunctionMode = UpdateMode.DIMMER;
            }
        }

        if (functionIds.contains("Temperature")) {
            // ...
        }
    }

    private void updateDeviceFunctionStates(Function patch, UpdateMode mode) {

        switch (mode) {
            case ONOFF:
                if (patch instanceof OnOff) {

                    Log.d("debug", "onoff");

                    if (!((OnOff) patch).getIsOn()) masterDimmer.setProgress(0);

                    for (Device d : adapter.getDevices()) {
                        for (Function f : d.getFunctions()) {

                            if (f instanceof OnOff) {
                                ((OnOff) f).setIsOn(((OnOff) patch).getIsOn());
                            }
                            if (!((OnOff) patch).getIsOn() && f instanceof Dimmer) {
                                ((Dimmer) f).setValue(0);
                            }

                        }
                    }
                }
                break;

            case DIMMER:
                if (patch instanceof Dimmer) {

                    Log.d("debug", "dimmer");

                    masterDimmer.setProgress(((Dimmer) patch).getValue());

                    //updateColorFromDimmer(((Dimmer) patch).getValue());

                    for (Device d : adapter.getDevices()) {
                        for (Function f : d.getFunctions()) {

                            if (f instanceof Dimmer) {
                                ((Dimmer) f).setValue(((Dimmer) patch).getValue());
                            }

                            if (f instanceof OnOff) {
                                if (((Dimmer) patch).getValue() > 0) {
                                    ((OnOff) f).setIsOn(true);
                                } else {
                                    ((OnOff) f).setIsOn(false);
                                }
                            }

                        }
                    }
                }
                break;

            case COLORDIMMER:
                if (patch instanceof ColorDimmer) {

                    Log.d("debug", "colordimmer");

                    setButtonBackgroundColor(HSVToColor(((ColorDimmer) patch).getHue(), ((ColorDimmer) patch).getSaturation(), masterDimmer.getProgress()));

                    for (Device d : adapter.getDevices()) {
                        for (Function f : d.getFunctions()) {

                            if (f instanceof ColorDimmer) {
                                ((ColorDimmer) f).setHue(((ColorDimmer) patch).getHue());
                                ((ColorDimmer) f).setSaturation(((ColorDimmer) patch).getSaturation());
                            }

                        }
                    }
                }
                break;
        }

        adapter.notifyDataSetChanged();

    }


    private int getAverageDimmerValue() {
        int i = 0;
        int count = 0;
        for (Device d : adapter.getDevices()) {
            for (Function f : d.getFunctions()){
                if (f instanceof Dimmer) {
                    if (((Dimmer) f).getValue() != null) {
                        i++;
                        count += ((Dimmer) f).getValue();
                    }

                }
            }
        }

        if (i > 0) return count/i;
        return 0;
    }

    private int getAverageColorValue() {
        int i = 0;
        int[] hsv = new int[3];
        for (Device d : adapter.getDevices()) {
            for (Function f : d.getFunctions()){
                if (f instanceof ColorDimmer) {
                    i++;
                    hsv[0] += ((ColorDimmer) f).getHue();
                    hsv[1] += ((ColorDimmer) f).getSaturation();
                    hsv[2] += ((ColorDimmer) f).getValue();
                }
            }
        }
        if (i > 0) return HSVToColor(hsv[0]/i, hsv[1]/i, hsv[2]/i);
        return 0;
    }

    private boolean setButtonBackgroundColor(int color) {

        masterColorDimmerColorValue = color;

        // fix to display always with value = 1 for brighter colors
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1f;
        color = Color.HSVToColor(hsv);

        try {
            LayerDrawable layer2 = (LayerDrawable) masterColorDimmer.getBackground();
            GradientDrawable shape = (GradientDrawable) layer2.findDrawableByLayerId(R.id.button_background);
            shape.setColor(color);// set new background color here
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private int getButtonBackgroundColor() {
        return masterColorDimmerColorValue;
    }

    private void updateColorFromDimmer(int value) {
        Integer color = masterColorDimmerColorValue;
        if (value == 0 || color == null || masterColorDimmer == null) return;
        int[] hsv = colorToHSV(color);
        int newColor = HSVToColor(hsv[0], hsv[1], value);
        setButtonBackgroundColor(newColor);
    }

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

    private void updateApi(final Function patchedFunction) {
        if (patchedFunction != null) {

            final Patcher patcher = new Patcher();
            patcher.setFilter(FilterContainer.getInstance().getPureFilter());

            // workaround for stupid Gson JSON parser, which absorbs function_id while doing polymorphic mapping.
            patchedFunction.setFunctionId(patchedFunction.getClass().getSimpleName());

            if (currentFunctionMode != null) {
                switch (currentFunctionMode) {

                    case ONOFF:
                        patchedFunction.setFunctionId(OnOff.class.getSimpleName());
                        break;

                    case DIMMER:
                        patchedFunction.setFunctionId(Dimmer.class.getSimpleName());
                        break;

                    case COLORDIMMER:
                        patchedFunction.setFunctionId(ColorDimmer.class.getSimpleName());
                        break;
                }
            }

            patcher.setFunction(patchedFunction);

            new Thread(new Runnable() {

                @Override
                public void run() {
                if (apiUrl != null) {
                    try {
                        Log.d("api", ApiInvoker.serialize(patcher).toString());
                        DefaultApi api = new DefaultApi();
                        api.setBasePath(apiUrl);
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

}
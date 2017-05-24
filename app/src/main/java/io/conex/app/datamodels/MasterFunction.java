package io.conex.app.datamodels;

import android.content.Context;
import android.content.SharedPreferences;
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
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
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

    private DevicesAdapter adapter;
    private String apiUrl;

    private LinearLayout wrapper;
    private Button masterOnoffOn;
    private Button masterOnoffOff;
    private SeekBar masterDimmer;

    public MasterFunction(View view, final DevicesAdapter adapter) {
        this.adapter = adapter;

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

                updateDeviceFunctionStates(update);
            }
        });

        this.masterOnoffOff = (Button) view.findViewById(R.id.master_onoff_off);
        masterOnoffOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnOff update = new OnOff();
                update.setIsOn(false);
                updateApi(update);

                updateDeviceFunctionStates(update);
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
                    updateDeviceFunctionStates(update);
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

                updateDeviceFunctionStates(update);
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
                        functionIds.add(f.getFunctionId());
                    }
                }
                showFunctionsOnFilter(functionIds);
                break;
        }
    }

    private void showFunctionsOnFilter(Collection<String> functionIds) {
        functionIds.removeAll(Collections.singleton(null));
        for (String id : functionIds) {

            switch (id) {

                case "OnOff":
                    //setFilterWithFunction("onoff");
                    wrapper.setVisibility(View.VISIBLE);
                    masterOnoffOn.setVisibility(View.VISIBLE);
                    masterOnoffOff.setVisibility(View.VISIBLE);
                    break;

                case "Dimmer":
                    //setFilterWithFunction("dimmer");
                    wrapper.setVisibility(View.VISIBLE);
                    masterDimmer.setVisibility(View.VISIBLE);
                    break;

                case "Temperature":
                    // ...
                    break;
            }
        }
    }

    private void updateDeviceFunctionStates(Function patch) {

        for (Device d : adapter.getDevices()) {
            String patchId = patch.getFunctionId();


            boolean containsPatch = false;
            boolean containsOnOff = false;
            boolean containsDimmer = false;
            boolean containsTemperature = false;

            for (Function f : d.getFunctions()) {
                if (f.getFunctionId().equals(patchId)) {
                    containsPatch = true;
                }
                if (f.getFunctionId().equalsIgnoreCase("OnOff")) {
                    containsOnOff = true;
                }
                if (f.getFunctionId().equalsIgnoreCase("Dimmer")) {
                    containsDimmer = true;
                }
                if (f.getFunctionId().equalsIgnoreCase("Temperature")) {
                    containsTemperature = true;
                }
            }

            if (containsPatch) {
                for (Function f : d.getFunctions()) {

                    if (patch instanceof OnOff) {
                        OnOff patchOnOff = (OnOff) patch;

                        if (f instanceof OnOff) {
                            ((OnOff) f).setIsOn(patchOnOff.getIsOn());
                        }

                        if (f instanceof Dimmer) {
                            if (!patchOnOff.getIsOn())((Dimmer) f).setValue(0);
                        }

                    } else if (patch instanceof Dimmer) {
                        Dimmer patchDimmer = (Dimmer) patch;

                        if (f instanceof Dimmer) {
                            ((Dimmer) f).setValue(patchDimmer.getValue());
                        }

                        if (containsOnOff && f instanceof OnOff) {
                            ((OnOff) f).setIsOn(patchDimmer.getValue() > 0 ? true : false);
                        }

                        try {


                            ((OnOff) f).setIsOn(((OnOff) patch).getIsOn());
                        } catch (ClassCastException e) { }


                    } else if (patch instanceof Temperature) {

                        // ...

                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateApi(final Function patchedFunction) {
        if (patchedFunction != null) {

            final Patcher patcher = new Patcher();
            patcher.setFilter(FilterContainer.getInstance().getPureFilter());

            // workaround for stupid Gson JSON parser, which absorbs function_id while doing polymorphic mapping.
            patchedFunction.setFunctionId(patchedFunction.getClass().getSimpleName());
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
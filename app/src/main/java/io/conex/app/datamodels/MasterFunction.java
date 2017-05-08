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

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static io.conex.brandnewsmarthomeapp.R.id.view;

/**
 * Created by philipp on 08.05.17.
 */

public class MasterFunction {

    private List<Device> devicesList;
    private ArrayAdapter<Device> adapter;
    private String apiUrl;

    private LinearLayout wrapper;
    private Button masterOnoffOn;
    private Button masterOnoffOff;
    private SeekBar masterDimmer;

    public MasterFunction(View view, final List<Device> devicesList, final ArrayAdapter<Device> adapter) {
        this.devicesList = devicesList;
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

                for (Device d : devicesList) {
                    for (Function f : d.getFunctions()) {
                        if (f instanceof OnOff) {
                            ((OnOff) f).setIsOn(true);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        this.masterOnoffOff = (Button) view.findViewById(R.id.master_onoff_off);
        masterOnoffOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnOff update = new OnOff();
                update.setIsOn(false);
                updateApi(update);

                for (Device d : devicesList) {
                    for (Function f : d.getFunctions()) {
                        if (f instanceof OnOff) {
                            ((OnOff) f).setIsOn(false);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        this.masterDimmer = (SeekBar) view.findViewById(R.id.master_dimmer);
        masterDimmer.setMax(255);
        masterDimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                boolean isOn = value > 0 ? true : false;

                Dimmer update = new Dimmer();
                update.setValue(value);
                updateApi(update);


                for (Device d : devicesList) {
                    for (Function f : d.getFunctions()) {
                        if (f instanceof Dimmer) {
                            ((Dimmer) f).setValue(value);
                        }
                        if (f instanceof OnOff) {
                            ((OnOff) f).setIsOn(isOn);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        hideAll();
    }

    private void hideAll() {
        wrapper.setVisibility(GONE);
        masterOnoffOff.setVisibility(GONE);
        masterOnoffOn.setVisibility(GONE);
        masterDimmer.setVisibility(GONE);
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

                case "onoff":
                    //setFilterWithFunction("onoff");
                    wrapper.setVisibility(View.VISIBLE);
                    masterOnoffOn.setVisibility(View.VISIBLE);
                    masterOnoffOff.setVisibility(View.VISIBLE);
                    break;

                case "dimmer":
                    //setFilterWithFunction("dimmer");
                    wrapper.setVisibility(View.VISIBLE);
                    masterDimmer.setVisibility(View.VISIBLE);
                    break;

                case "temperature":
                    // ...
                    break;
            }
        }
    }

    private void updateApi(final Function patchedFunction) {
        if (patchedFunction != null) {

            final Patcher patcher = new Patcher();
            patcher.setFilter(FilterContainer.getInstance().getPureFilter());

            // workaround for stupid Gson JSON parser, which absorbs function_id while doing polymorphic mapping.
            patchedFunction.setFunctionId(patchedFunction.getClass().getSimpleName().toLowerCase());
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
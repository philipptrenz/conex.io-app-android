package io.conex.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

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
import static android.view.View.GONE;
import static io.conex.app.Mode.FUNCTIONS;

/**
 * Created by philipp on 06.05.17.
 */

public class DevicesFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String apiUrl;

    private ArrayList<Device> devicesList;
    private ArrayAdapter<Device> arrayAdapter;
    private ViewPager viewPager;

    private Mode mode;
    private String categoryId;

    private MasterFunction masterFunction;

    public DevicesFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DevicesFragment newInstance(int sectionNumber, ViewPager viewPager) {
        DevicesFragment fragment = new DevicesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.viewPager = viewPager;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_devices, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.devices_list_view);
        devicesList = new ArrayList<>();

        arrayAdapter = new DevicesAdapter(getActivity().getApplicationContext(), devicesList, this);
        listView.setAdapter(arrayAdapter);

        Context context = getActivity().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preferences_file_key), MODE_PRIVATE);
        apiUrl = sharedPref.getString(getString(R.string.api_url_key), "");

        masterFunction = new MasterFunction(rootView, devicesList, arrayAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    public void updateListView() {
        updateListView(mode, categoryId);
    }

    public void updateListView(final Mode mode, final String categoryId) {
        this.mode = mode;
        this.categoryId = categoryId;

        masterFunction.showMasterFunction(mode, categoryId);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            Filter filter = new Filter();
            List<String> list = new ArrayList<>();
            if (categoryId != null && !categoryId.isEmpty()) {
                list.add(categoryId);
            }
            if (mode != null) {
                switch(mode) {
                    case FUNCTIONS:
                        filter.setFunctionIds(list);
                        masterFunction.showMasterFunction(mode, categoryId);
                        break;
                    case ROOMS:
                        filter.setRoomIds(list);
                        break;
                    case GROUPS:
                        filter.setGroupIds(list);
                        break;
                    case DEVICES:
                        filter.setDeviceIds(list);
                        break;
                }
                doApiRequest(apiUrl, filter);
            }
            }
        });

    }


    private void doApiRequest(final String url, final Filter filter) {

        final Activity activity = getActivity();

        new Thread(new Runnable() {

            @Override
            public void run() {

                if (apiUrl != null) {

                    DefaultApi api = new DefaultApi();
                    api.setBasePath(url);

                    try {
                        Devices list = api.devicesPost(filter);
                        onUIThread(list);

                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e("api", e.getMessage()+":\n"+e.getCause());
                        } else {
                            Log.e("api", "test failed, unknown cause");
                        }
                    }
                }
            }

            private void onUIThread(final Devices list) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (list.getDevices() != null) {

                        devicesList.clear();
                        devicesList.addAll(list.getDevices());
                        arrayAdapter.notifyDataSetChanged();

                        Log.d("api", devicesList.size()+" devices received");
                    }
                    }
                });
            }
        }).start();
    }

    private static class MasterFunction {

        private List<Device> devicesList;
        private ArrayAdapter<Device> adapter;

        private LinearLayout wrapper;
        private Button masterOnoffOn;
        private Button masterOnoffOff;
        private SeekBar masterDimmer;
        private Filter filter;

         private MasterFunction(View view, final List<Device> devicesList, final ArrayAdapter<Device> adapter) {
             this.devicesList = devicesList;
             this.adapter = adapter;

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

        void showMasterFunction(Mode mode, String categoryId) {
            hideAll();

            if (categoryId != null) {
                switch(mode) {
                    case FUNCTIONS:
                        switch (categoryId) {
                            case "onoff":
                                setFilterWithFunction("onoff");
                                wrapper.setVisibility(View.VISIBLE);
                                masterOnoffOn.setVisibility(View.VISIBLE);
                                masterOnoffOff.setVisibility(View.VISIBLE);
                                break;

                            case "dimmer":
                                setFilterWithFunction("dimmer");
                                wrapper.setVisibility(View.VISIBLE);
                                masterDimmer.setVisibility(View.VISIBLE);
                                break;
                        }
                        break;

                    case ROOMS:
                        break;

                    case GROUPS:
                        break;

                    case ALL:
                        break;
                }
            }
        }

        private void setFilterWithFunction(String functionId) {
            ArrayList<String> functionIds = new ArrayList<>();
            functionIds.add(functionId);
            filter = new Filter();
            filter.setFunctionIds(functionIds);
        }

        private void updateApi(final Function patchedFunction) {
            if (patchedFunction != null && filter != null && filter.getFunctionIds() != null && filter.getFunctionIds().size() > 0) {

                final Patcher patcher = new Patcher();
                patcher.setFilter(filter);

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
}


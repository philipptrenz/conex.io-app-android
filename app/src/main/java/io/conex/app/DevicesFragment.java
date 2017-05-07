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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Device;
import io.swagger.client.model.Devices;
import io.swagger.client.model.Filter;

import static android.content.Context.MODE_PRIVATE;

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
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    public void updateListView(Mode mode, String categoryId) {
        this.mode = mode;
        this.categoryId = categoryId;

        Filter filter = new Filter();
        List<String> list = new ArrayList<>();
        if (categoryId != null && !categoryId.isEmpty()) {
            list.add(categoryId);
        }
        if (mode != null) {
            switch(mode) {
                case FUNCTIONS:
                    filter.setFunctionIds(list);
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

    public void updateListView() {
        updateListView(mode, categoryId);
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
}


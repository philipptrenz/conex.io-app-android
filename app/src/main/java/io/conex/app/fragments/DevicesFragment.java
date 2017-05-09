package io.conex.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.conex.app.arrayadapters.DevicesAdapter;
import io.conex.app.datamodels.CategoryItem;
import io.conex.app.datamodels.MasterFunction;
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.model.Device;
import io.swagger.client.model.Filter;

/**
 * Created by philipp on 06.05.17.
 */

public class DevicesFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayList<Device> devicesList;
    private DevicesAdapter arrayAdapter;
    private ViewPager viewPager;
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
        arrayAdapter = new DevicesAdapter(getActivity().getApplicationContext(), devicesList);
        listView.setAdapter(arrayAdapter);
        masterFunction = new MasterFunction(rootView, arrayAdapter);

        return rootView;
    }

    public void update(List<Device> devicesList) {
        devicesList.removeAll(Collections.singleton(null));
        this.devicesList = (ArrayList<Device>) devicesList;
        arrayAdapter.clear();
        arrayAdapter.addAll(devicesList);
        arrayAdapter.notifyDataSetChanged();
        masterFunction.update(devicesList);
    }
}


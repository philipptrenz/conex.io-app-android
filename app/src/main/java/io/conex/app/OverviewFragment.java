package io.conex.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.model.Device;

import static io.conex.app.Mode.ALL;

/**
 * Created by philipp on 06.05.17.
 */

public class OverviewFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String apiUrl;

    private ArrayList<OverviewItem> overview;
    private ArrayAdapter<OverviewItem> arrayAdapter;
    private ViewPager viewPager;

    private Mode filterMode;

    public OverviewFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static OverviewFragment newInstance(int sectionNumber, ViewPager viewPager) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.viewPager = viewPager;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.overview_list_view);
        overview = new ArrayList<>();

        overview.add(new OverviewItem("Rooms", Mode.ROOMS, "Select devices by room"));
        overview.add(new OverviewItem("Groups", Mode.GROUPS, "Select devices by group"));
        overview.add(new OverviewItem("Functions", Mode.FUNCTIONS, "Select devices by functionality"));
        overview.add(new OverviewItem("All", ALL, "Show all devices"));

        arrayAdapter = new OverviewAdapter(getActivity().getApplicationContext(), overview);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            OverviewItem item = (OverviewItem) parent.getAdapter().getItem(position);
            filterMode = item.mode;

            Log.d("api", "selected: "+item.name+", "+item.mode.name());

            if (item.mode.equals(ALL)) {
                viewPager.setCurrentItem(2);
            } else {
                viewPager.setCurrentItem(1);
            }
            }
        });

        filterMode = ALL;

        return rootView;
    }

    public Mode getFilterMode() {
        return filterMode;
    }

    private class OverviewItem {

        String name;
        String description;
        Mode mode;

        OverviewItem(String name, Mode mode, String description) {
            this.name = name;
            this.description = description;
            this.mode = mode;
        }
    }

    private class OverviewAdapter extends ArrayAdapter<OverviewItem> {

        public OverviewAdapter(Context context, ArrayList<OverviewItem> categories) {
            super(context, 0, categories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            OverviewItem item = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.overview_list_item, parent, false);
            }

            // Lookup view for data population
            TextView name = (TextView) convertView.findViewById(R.id.overview_name);
            TextView description = (TextView) convertView.findViewById(R.id.overview_description);
            // Populate the data into the template view using the data object
            name.setText(item.name);
            description.setText(item.description);
            // Return the completed view to render on screen

            return convertView;
        }
    }
}


package io.conex.app.fragments;

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

import java.util.ArrayList;

import io.conex.app.arrayadapters.OverviewAdapter;
import io.conex.app.datamodels.FilterContainer;
import io.conex.app.datamodels.Mode;
import io.conex.app.datamodels.OverviewItem;
import io.conex.brandnewsmarthomeapp.R;

import static io.conex.app.datamodels.Mode.ALL;

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
            FilterContainer.getInstance().setMode(item.mode);
            FilterContainer.getInstance().resetFilterIds();

            Log.d("api", "selected: "+item.name+", "+item.mode.name());

            if (item.mode.equals(ALL)) {
                viewPager.setCurrentItem(2);
            } else {
                viewPager.setCurrentItem(1);
            }
            }
        });

        return rootView;
    }
}


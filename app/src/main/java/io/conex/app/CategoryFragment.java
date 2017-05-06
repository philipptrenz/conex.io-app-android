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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.ApiException;
import io.swagger.client.ApiInvoker;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.Filter;
import io.swagger.client.model.Ids;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by philipp on 06.05.17.
 */

public class CategoryFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String apiUrl;

    private ArrayList<String> categories;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager viewPager;

    private Mode filterMode;
    private String filterCategoryId;


    public CategoryFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CategoryFragment newInstance(int sectionNumber, ViewPager viewPager) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        fragment.viewPager = viewPager;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.category_list_view);
        categories = new ArrayList<>();

        arrayAdapter = new IdsAdapter(getActivity().getApplicationContext(), categories, filterMode);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String category = (String) parent.getAdapter().getItem(position);
            filterCategoryId = category;
            viewPager.setCurrentItem(2);
            }
        });

        Context context = getActivity().getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preferences_file_key), MODE_PRIVATE);
        apiUrl = sharedPref.getString(getString(R.string.api_url_key), "");

        return rootView;
    }

    public Mode getFilterMode() {
        return this.filterMode;
    }

    public String getFilterCategoryId() {
        return this.filterCategoryId;
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    public void updateListView(Mode filterMode) {

        final String url = apiUrl;
        final Mode mode = filterMode;
        this.filterMode = filterMode;

        ((IdsAdapter) arrayAdapter).setMode(filterMode);

        try {
            Log.d("api", "Mode: "+mode.name());
        } catch (NullPointerException e) {
            Log.d("api", "Mode is NULL");
        }

        final Activity activity = getActivity();

        new Thread(new Runnable() {

            @Override
            public void run() {

                if (apiUrl != null && mode != null) {

                    DefaultApi api = new DefaultApi();
                    api.setBasePath(url);
                    Filter filter = new Filter();

                    Log.d("api", "requesting "+mode.name()+" from api");

                    try {
                        List<String> ids = new ArrayList<String>();
                        Ids newIds;
                        switch(mode) {
                            case ROOMS:
                                newIds = api.roomsPost(filter);
                                ids.addAll(newIds.getIds());
                                onUIThread(ids);
                                break;
                            case GROUPS:
                                newIds = api.groupsPost(filter);
                                ids.addAll(newIds.getIds());
                                onUIThread(ids);
                                break;
                            case FUNCTIONS:
                                newIds = api.functionsPost(filter);
                                ids.addAll(newIds.getIds());
                                onUIThread(ids);
                        }
                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e("api", e.getMessage()+":\n"+e.getCause());
                        } else {
                            Log.e("api", "test failed, unknown cause");
                        }
                    }
                }
            }

            private void onUIThread(final List<String> list) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (list != null) {

                            categories.clear();
                            categories.addAll(list);
                            arrayAdapter.notifyDataSetChanged();

                            Log.d("api", categories.size()+" ids received");

                        }
                    }
                });
            }
        }).start();
    }

    private class IdsAdapter extends ArrayAdapter<String> {

        private Mode mode;

        public IdsAdapter(Context context, ArrayList<String> ids, Mode mode) {
            super(context, 0, ids);
            this.mode = mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String id = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_list_item, parent, false);
            }

            TextView catName = (TextView) convertView.findViewById(R.id.category_name);
            TextView catDescription = (TextView) convertView.findViewById(R.id.category_description);

            catName.setText(id);

            switch(mode){
                case FUNCTIONS:
                    catDescription.setText("Show all devices with functionality '"+id+"'");
                    break;
                case GROUPS:
                    catDescription.setText("Show all devices in group '"+id+"'");
                    break;
                case ROOMS:
                    catDescription.setText("Show all devices in room '"+id+"'");
                    break;
            }
            return convertView;
        }
    }
}


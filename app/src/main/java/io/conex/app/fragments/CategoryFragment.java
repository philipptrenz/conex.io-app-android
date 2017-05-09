package io.conex.app.fragments;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.conex.app.arrayadapters.CategoryAdapter;
import io.conex.app.datamodels.CategoryItem;
import io.conex.app.datamodels.FilterContainer;
import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.model.Filter;

/**
 * Created by philipp on 06.05.17.
 */

public class CategoryFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ArrayList<CategoryItem> categoryList;
    private ArrayAdapter<CategoryItem> arrayAdapter;
    private ViewPager viewPager;

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
        categoryList = new ArrayList<>();

        arrayAdapter = new CategoryAdapter(getActivity().getApplicationContext(), categoryList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CategoryItem item = (CategoryItem) parent.getAdapter().getItem(position);
            FilterContainer.getInstance().setSingleFilterId(item.category, item.mode);
            viewPager.setCurrentItem(2);
            }
        });
        return rootView;
    }

    public void update(List<CategoryItem> categoryList) {
        categoryList.removeAll(Collections.singleton(null));

        arrayAdapter.clear();
        arrayAdapter.addAll(categoryList);
        arrayAdapter.notifyDataSetChanged();
    }
}
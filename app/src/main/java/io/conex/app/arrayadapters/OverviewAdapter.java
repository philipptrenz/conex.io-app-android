package io.conex.app.arrayadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.conex.app.datamodels.OverviewItem;
import io.conex.app.fragments.OverviewFragment;
import io.conex.brandnewsmarthomeapp.R;

/**
 * Created by philipp on 08.05.17.
 */

public class OverviewAdapter extends ArrayAdapter<OverviewItem> {

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
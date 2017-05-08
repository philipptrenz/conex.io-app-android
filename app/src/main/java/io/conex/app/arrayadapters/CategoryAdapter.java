package io.conex.app.arrayadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.conex.app.datamodels.CategoryItem;
import io.conex.app.datamodels.FilterContainer;
import io.conex.brandnewsmarthomeapp.R;

/**
 * Created by philipp on 08.05.17.
 */

public class CategoryAdapter extends ArrayAdapter<CategoryItem> {

    public CategoryAdapter(Context context, ArrayList<CategoryItem> ids) {
        super(context, 0, ids);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CategoryItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_list_item, parent, false);
        }

        TextView catName = (TextView) convertView.findViewById(R.id.category_name);
        TextView catDescription = (TextView) convertView.findViewById(R.id.category_description);

        catName.setText(item.category);
        catDescription.setText(item.description);

        return convertView;
    }
}

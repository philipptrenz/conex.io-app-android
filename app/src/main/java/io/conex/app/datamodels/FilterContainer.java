package io.conex.app.datamodels;

import android.util.Log;

import java.util.ArrayList;

import io.swagger.client.model.Filter;

/**
 * Created by philipp on 08.05.17.
 *
 * Threadsafe singleton of io.swagger.client.model.Filter
 */

public class FilterContainer extends Filter {

    private static FilterContainer instance = null;

    protected FilterContainer() {}

    // Lazy Initialization (If required then only)
    public static FilterContainer getInstance() {
        if (instance == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (FilterContainer.class) {
                if (instance == null) {
                    instance = new FilterContainer();
                }
            }
        }
        return instance;
    }


    private Mode mode;

    public Mode getMode() {
        if (mode == null) {
            mode = Mode.ALL;
        }
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void resetFilterIds() {
        this.setFunctionIds(null);
        this.setRoomIds(null);
        this.setGroupIds(null);
        this.setDeviceIds(null);
    }

    public void setSingleFilterId(String id, Mode mode) {
        resetFilterIds();
        Log.d("ui", "setting "+id+", "+mode.name());

        ArrayList<String> list = new ArrayList<String>();
        list.add(id);
        switch (mode) {
            case FUNCTIONS:
                this.setFunctionIds(list);
                break;
            case ROOMS:
                this.setRoomIds(list);
                break;
            case GROUPS:
                this.setGroupIds(list);
                break;
        }
    }

    public Filter getPureFilter() {
        return (Filter) this;
    }

}

package io.conex.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import io.conex.brandnewsmarthomeapp.R;
import io.swagger.client.model.Device;
import io.swagger.client.model.Dimmer;
import io.swagger.client.model.Function;
import io.swagger.client.model.OnOff;

/**
 * Created by philipp on 06.05.17.
 */

public class DevicesAdapter extends ArrayAdapter<Device> {

    public DevicesAdapter(Context context, ArrayList<Device> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Device device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item, parent, false);
        }

        TextView device_id = (TextView) convertView.findViewById(R.id.device_id);
        device_id.setText(device.getDeviceId());



        final ToggleButton onoff = (ToggleButton) convertView.findViewById(R.id.function_onoff);
        onoff.setVisibility(View.GONE);

        final SeekBar dimmer = (SeekBar) convertView.findViewById(R.id.function_slider);
        dimmer.setVisibility(View.GONE);
        dimmer.setMax(255);

        List<Function> functions = device.getFunctions();

        for (final Function f : functions) {
            if (f instanceof OnOff) {
                OnOff onoff_func = (OnOff) f;
                boolean isOn = onoff_func.getIsOn();

                onoff.setVisibility(View.VISIBLE);
                onoff.setChecked(isOn);

                onoff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("api", "OnOff at device '"+device.getDeviceId()+"' has changed: "+onoff.isChecked());
                    }
                });

            } else if (f instanceof Dimmer) {

                dimmer.setVisibility(View.VISIBLE);
                Dimmer dimmer_func = (Dimmer) f;

                dimmer.setProgress(dimmer_func.getValue());

                dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d("api", "Dimmer at device '"+device.getDeviceId()+"' has changed: "+seekBar.getProgress());
                    }
                });

            } else {
                Log.d("api", "found unknown function in device '"+device.getDeviceId()+"', function_id: "+f.getFunctionId());
            }
        }


        return convertView;
    }
}
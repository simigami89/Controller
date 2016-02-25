package com.example.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Екатерина Захарова on 25.02.2016.
 */
public class DeviceAdapter extends BaseAdapter{
    Context ctx;
    private LayoutInflater inflater;
    private List<BleDeviceInfo> objects;

    DeviceAdapter(Context context, List<BleDeviceInfo> products) {
        ctx = context;
        objects = products;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return objects.size();
    }

    public Object getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.item_device, parent, false);
        }
        final BleDeviceInfo deviceItem = getDevice(position);
        ((TextView) view.findViewById(R.id.dev_name)).setText(deviceItem.getBluetoothDevice().getName());
        ((TextView) view.findViewById(R.id.dev_address)).setText(deviceItem.getBluetoothDevice().getAddress());
        ((TextView) view.findViewById(R.id.rssi_value)).setText(Integer.toString(deviceItem.getRssi()));
        view.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BluetoothDevice device = deviceItem.getBluetoothDevice();
                Intent intent = new Intent(ctx.getApplicationContext(), ConnectedActivity.class);
                intent.putExtra("device", device);
                ctx.startActivity(intent);
            }
        });
        return view;
    }

    BleDeviceInfo getDevice(int position) {
        return (BleDeviceInfo) getItem(position);
    }
}

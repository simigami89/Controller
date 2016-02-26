package com.example.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    final static String DEBUG = MainActivity.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQ_DEVICE_ACT = 1;
    private static final int REQ_ENABLE_BT = 0;
    private static final long SCAN_PERIOD = 5000;

    BluetoothAdapter mBluetoothAdapter;

    Intent browserIntent;
    ListView listFoundDevice;
    Handler mHandler;
    Timer mTimer;

    MenuItem search;

    DeviceAdapter mDeviceAdapter;
    boolean bleEnabled = false;

    private static List<BleDeviceInfo> mDevices = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.antrax-energo.ru"));
        listFoundDevice = (ListView) findViewById(R.id.listFoundDevice);
        mDeviceAdapter = new DeviceAdapter(this, mDevices);
        listFoundDevice.setAdapter(mDeviceAdapter);

        enableBLE();
        if(bleEnabled){
            startScan();
        }
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                search.setActionView(null);
                mDeviceAdapter.notifyDataSetChanged();
            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        search = menu.findItem(R.id.action_search);
        if(bleEnabled) {
            search.setActionView(R.layout.progress_bar);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_search){
            search.setActionView(R.layout.progress_bar);
            startScan();
        }

        return super.onOptionsItemSelected(item);
    }

    public void enableBLE(){
        //check BLE avaible
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();

        }
        else {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                bleEnabled = false;

            }else {
                bleEnabled = true;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0 :
            case 1 :
                if (resultCode == -1) {
                    Toast.makeText(this, "enabled", Toast.LENGTH_SHORT).show();
                    search.setActionView(R.layout.progress_bar);
                    bleEnabled = true;
                    startScan();
                    return;
                }
                Toast.makeText(this, "disabled", Toast.LENGTH_SHORT).show();
                finish();
                return;
            default:
                return;
        }
    }

    private void startScan() {
        Log.d(DEBUG, "Searching for devices ...");

        if (mTimer != null) {
            this.mTimer.cancel();
        }
        scanBLEDevice();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(MainActivity.REQ_ENABLE_BT));
                Log.d(MainActivity.DEBUG, "Search complete");

            }
        }, SCAN_PERIOD);
    }

    private void scanBLEDevice() {
        new Thread() {
            public void run() {
                MainActivity.this.mBluetoothAdapter.startLeScan(MainActivity.this.mLeScanCallback);
                try {
                    Thread.sleep(MainActivity.SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.this.mBluetoothAdapter.stopLeScan(MainActivity.this.mLeScanCallback);
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (deviceInfoExists(device.getAddress())) {
                        BleDeviceInfo deviceInfo = findDeviceInfo(device);
                        if (deviceInfo != null) {
                            deviceInfo.updateRssi(rssi);
                            return;
                        }
                        return;
                    }
                    MainActivity.mDevices.add(createDeviceInfo(device, rssi));
                }
            });
        }
    };

    private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
        return new BleDeviceInfo(device, rssi);
    }

    private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
        for (int i = 0; i < mDevices.size(); i++ ) {
            if (((BleDeviceInfo) mDevices.get(i)).getBluetoothDevice().getAddress().equals(device.getAddress())) {
                return (BleDeviceInfo) mDevices.get(i);
            }
        }
        return null;
    }

    private boolean deviceInfoExists(String address) {
        for (int i = 0; i < mDevices.size(); i ++) {
            if (((BleDeviceInfo) mDevices.get(i)).getBluetoothDevice().getAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }



}

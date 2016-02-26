package com.example.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ConnectedActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static String CHAR_CUR_VAL_I_A = "5ac70ac1-717e-11e5-a837-0800200c9a66";
    private static String CHAR_CUR_VAL_I_B = "5ac70ac3-717e-11e5-a837-0800200c9a66";
    private static String CHAR_CUR_VAL_I_C = "5ac70ac5-717e-11e5-a837-0800200c9a66";
    private static String CHAR_CUR_VAL_U_A = "5ac70ac2-717e-11e5-a837-0800200c9a66";
    private static String CHAR_CUR_VAL_U_B = "5ac70ac4-717e-11e5-a837-0800200c9a66";
    private static String CHAR_CUR_VAL_U_C = "5ac70ac6-717e-11e5-a837-0800200c9a66";
    private static String CHAR_LED_BLINK = "5ac70AE1-717e-11e5-a837-0800200c9a66";
    private static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static String DEVICE_1_ADDRESS = "5ac70ad2-717e-11e5-a837-0800200c9a66";
    private static String DEVICE_2_ADDRESS = "5ac70ad3-717e-11e5-a837-0800200c9a66";
    private static String FIRMWARE_VERSION_CHARACTERISTIC = "00002a26-0000-1000-8000-00805f9b34fb";
    private static String HARDWARE_VERSION_CHARACTERISTIC = "00002a27-0000-1000-8000-00805f9b34fb";
    private static String LED_SERVICE = "5ac70ae0-717e-11e5-a837-0800200c9a66";
    private static String MANUFACTURED_CHARACTERISTIC = "00002a29-0000-1000-8000-00805f9b34fb";
    private static String MODEL_VERSION_CHARACTERISTIC = "00002a24-0000-1000-8000-00805f9b34fb";
    private static String PHASE = "5ac70ad4-717e-11e5-a837-0800200c9a66";
    private static String SERIAL_NUMBER_CHARACTERISTIC = "00002a25-0000-1000-8000-00805f9b34fb";
    private static String SERVICE_BANK_INFO = "5ac70ad0-717e-11e5-a837-0800200c9a66";
    private static String SERVICE_CUR_VALS = "5ac70ac0-717e-11e5-a837-0800200c9a66";
    private static String SERVICE_DEVICE_INFO = "0000180a-0000-1000-8000-00805f9b34fb";

    private static BluetoothGattCharacteristic gattCharacteristic1;

    private static BluetoothGattService mBluetoothGattServiceBankInfo;
    private static BluetoothGattService mBluetoothGattServiceCurVals;
    private static BluetoothGattService mBluetoothGattServiceDeviceInfo;
    private static BluetoothGattService mBluetoothGattServiceLed;

    static ArrayList<BluetoothGattService> deviceServices = new ArrayList();

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static BluetoothManager mBluetoothManager;
    private static BluetoothDevice mDevice;
    private static DeviceInfo mDeviceInfo;
    private static BluetoothGatt mBluetoothGatt;

    final static String DEBUG = ConnectedActivity.class.getName();

    private static Handler mHandler;
    private static Timer rssiTimer;
    private static TimerTask task;

    private static Timer configTimer;
    private static TimerTask taskConfig;

    private static Timer readCharTimer;
    private static TimerTask taskReadCharConfig;

    final int STATE_CONNECTING = 0;
    final int STATE_CONNECTED = 1;
    final int STATE_DISCONNECTED = 2;
    final int UPDATE_CURVALS = 3;

    static int charEnabledCount = 0;
    static int charReadCount = 0;

    public CurVals mCurVals;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Connecting...");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG, "back");
                finish();
            }
        });
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE_CURVALS) {
                    CurvalsFragment.showValues();
                } else if (msg.what == STATE_DISCONNECTED) {
                    toolbar.setSubtitle("Disconncected");
                    //switchBtnConnect.setIcon(R.drawable.ic_check_circle_white_24dp);
                } else if (msg.what == STATE_CONNECTED) {
                    //switchBtnConnect.setIcon(R.drawable.ic_cancel_white_24dp);
                    toolbar.setSubtitle("Conncected");
                } else if (msg.what == STATE_CONNECTING) {
                    toolbar.setSubtitle("Conncecting...");
                }
            }
        };

        mCurVals = CurVals.getInstance();
    }

    @Override
    protected void onResume() {
        mDeviceInfo = DeviceInfo.getInstance();
        mDevice = (BluetoothDevice) getIntent().getParcelableExtra("device");
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        connect();
        Log.d(DEBUG, "onResume");
        super.onResume();
    }

    private void connect() {
        if (mBluetoothGatt == null) {
            mBluetoothGatt = mDevice.connectGatt(getApplicationContext(), false, mGattCallback);
            Log.d(DEBUG, "connectGatt()");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connected, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == 2) {
                Log.d(ConnectedActivity.DEBUG, "Connected");
                Log.d(ConnectedActivity.DEBUG, "Searching for services");
                mHandler.sendMessage(mHandler.obtainMessage(STATE_CONNECTED));
                ConnectedActivity.mBluetoothGatt.discoverServices();
                rssiTimer = new Timer();
                task = new TimerTask() {
                    public void run() {
                        mBluetoothGatt.readRemoteRssi();
                    }
                };
                rssiTimer.schedule(task, 2000, 2000);
            } else if (newState == 0) {
                Log.d(ConnectedActivity.DEBUG, "Device disconnected");
                try {
                    rssiTimer.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(STATE_DISCONNECTED));
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0) {
                for (BluetoothGattService gattService : ConnectedActivity.mBluetoothGatt.getServices()) {
                    Log.d(ConnectedActivity.DEBUG, "Service discovered: " + gattService.getUuid());
                    if (SERVICE_CUR_VALS.equals(gattService.getUuid().toString())) {
                        ConnectedActivity.mBluetoothGattServiceCurVals = gattService;
                        ConnectedActivity.deviceServices.add(ConnectedActivity.mBluetoothGattServiceCurVals);
                        Log.d(ConnectedActivity.DEBUG, "Found cur_vals Service");
                    }
                    if (ConnectedActivity.LED_SERVICE.equals(gattService.getUuid().toString())) {
                        ConnectedActivity.mBluetoothGattServiceLed = gattService;
                        ConnectedActivity.deviceServices.add(ConnectedActivity.mBluetoothGattServiceLed);
                        Log.d(ConnectedActivity.DEBUG, "Found led Service");
                    }
                    if (ConnectedActivity.SERVICE_DEVICE_INFO.equals(gattService.getUuid().toString())) {
                        ConnectedActivity.mBluetoothGattServiceDeviceInfo = gattService;
                        ConnectedActivity.deviceServices.add(ConnectedActivity.mBluetoothGattServiceDeviceInfo);
                        Log.d(ConnectedActivity.DEBUG, "Found deviceInfo Service");
                    }
                    if (ConnectedActivity.SERVICE_BANK_INFO.equals(gattService.getUuid().toString())) {
                        ConnectedActivity.mBluetoothGattServiceBankInfo = gattService;
                        ConnectedActivity.deviceServices.add(ConnectedActivity.mBluetoothGattServiceBankInfo);
                        Log.d(ConnectedActivity.DEBUG, "Found BankInfo Service");
                    }
                }
                enableNotification();
                return;
            }
            Log.d(ConnectedActivity.DEBUG, "onServicesDiscovered received: " + status);
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == 0) {
                String access$000 = ConnectedActivity.DEBUG;
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(rssi);
                Log.d(access$000, String.format("BluetoothGatt ReadRssi[%d]", objArr));
                mCurVals.setRSSI(String.valueOf(rssi));
                mHandler.sendMessage(mHandler.obtainMessage(UPDATE_CURVALS));
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String uuidString = characteristic.getUuid().toString();
            String str = null;
            try {
                str = new String(characteristic.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_I_A)) {
                mCurVals.setI_A(str);
            } else if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_U_A)) {
                mCurVals.setU_A(str);
            } else if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_I_B)) {
                mCurVals.setI_B(str);
            } else if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_U_B)) {
               mCurVals.setU_B(str);
            } else if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_I_C)) {
                mCurVals.setI_C(str);
            } else if (uuidString.equals(ConnectedActivity.CHAR_CUR_VAL_U_C)) {
                mCurVals.setU_C(str);
            }
            mHandler.sendMessage(mHandler.obtainMessage(ConnectedActivity.this.UPDATE_CURVALS));
            Log.d(DEBUG, str + " " + characteristic.getUuid().toString());
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(DEBUG, characteristic.toString() + " status: " + Integer.toString(status));
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] tx = characteristic.getValue();
            String str = null;
            try {
                str = new String(tx, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (characteristic.getUuid().equals(UUID.fromString(MODEL_VERSION_CHARACTERISTIC))) {
                mDeviceInfo.setModel(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(FIRMWARE_VERSION_CHARACTERISTIC))) {
                mDeviceInfo.setFirmvare(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(HARDWARE_VERSION_CHARACTERISTIC))) {
                mDeviceInfo.setHardware(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(MANUFACTURED_CHARACTERISTIC))) {
                mDeviceInfo.setManufartured(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(SERIAL_NUMBER_CHARACTERISTIC))) {
                mDeviceInfo.setSerial(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(DEVICE_1_ADDRESS))) {
                str = byteToHex(tx);
                mDeviceInfo.setDevice1(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(DEVICE_2_ADDRESS))) {
                str = byteToHex(tx);
               mDeviceInfo.setDevice2(str);
            } else if (characteristic.getUuid().equals(UUID.fromString(PHASE))) {
                mDeviceInfo.setPhase(str);
            }
            Log.d(ConnectedActivity.DEBUG, str);
        }

        public String byteToHex(byte[] b) {
            String str = "";
            for (int i = 0; i < b.length; i ++) {
                str = str + Integer.toHexString(b[i] & 255);
            }
            return str;
        }

        private void enableNotification() {
            charEnabledCount = 0;
            if (configTimer != null) {
                configTimer.cancel();
            }
            if (taskConfig != null) {
                taskConfig.cancel();
            }
            configTimer = new Timer();
            taskConfig = new TimerTask() {
                public void run() {
                    switch (charEnabledCount) {
                        case 0:
                            gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_I_A));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                        case 1:
                           gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_U_A));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                        case 2:
                           gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_I_B));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                        case 3:
                           gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_U_B));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                        case 4:
                            gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_I_C));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                        case 5:
                            gattCharacteristic1 = mBluetoothGattServiceCurVals.getCharacteristic(UUID.fromString(CHAR_CUR_VAL_U_C));
                            mBluetoothGatt.setCharacteristicNotification(ConnectedActivity.gattCharacteristic1, true);
                            break;
                    }
                    BluetoothGattDescriptor descriptor = gattCharacteristic1.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                    charEnabledCount++;
                    if (charEnabledCount == 6) {
                        configTimer.cancel();
                        readInfo();
                    }
                }
            };
            configTimer.schedule(taskConfig, 200, 200);
        }

        private void readInfo() {
            charReadCount = 0;
            if (readCharTimer != null) {
                readCharTimer.cancel();
            }
            if (taskReadCharConfig != null) {
                taskReadCharConfig.cancel();
            }
            readCharTimer = new Timer();
            taskReadCharConfig = new TimerTask() {
                public void run() {
                    switch (ConnectedActivity.charReadCount) {
                        case 0:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceDeviceInfo.getCharacteristic(UUID.fromString(MODEL_VERSION_CHARACTERISTIC)));
                            break;
                        case 1:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceDeviceInfo.getCharacteristic(UUID.fromString(SERIAL_NUMBER_CHARACTERISTIC)));
                            break;
                        case 2:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceDeviceInfo.getCharacteristic(UUID.fromString(HARDWARE_VERSION_CHARACTERISTIC)));
                            break;
                        case 3:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceDeviceInfo.getCharacteristic(UUID.fromString(FIRMWARE_VERSION_CHARACTERISTIC)));
                            break;
                        case 4:
                           mBluetoothGatt.readCharacteristic(mBluetoothGattServiceDeviceInfo.getCharacteristic(UUID.fromString(MANUFACTURED_CHARACTERISTIC)));
                            break;
                        case 5:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceBankInfo.getCharacteristic(UUID.fromString(DEVICE_1_ADDRESS)));
                            break;
                        case 6:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceBankInfo.getCharacteristic(UUID.fromString(DEVICE_2_ADDRESS)));
                            break;
                        case 7:
                            mBluetoothGatt.readCharacteristic(mBluetoothGattServiceBankInfo.getCharacteristic(UUID.fromString(PHASE)));
                            break;
                    }
                    if (ConnectedActivity.charReadCount == 7) {
                        readCharTimer.cancel();
                    }
                    charReadCount++;
                }
            };
            readCharTimer.schedule(taskReadCharConfig, 100, 100);
        }

    };





    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = CurvalsFragment.newInstance();
                    break;
                case 1:
                    fragment = ConfigFragment.newInstance();
                    break;
                case 2:
                    fragment = InfoFragment.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CURVALS";
                case 1:
                    return "CONFIG";
                case 2:
                    return "INFO";
            }
            return null;
        }
    }

}

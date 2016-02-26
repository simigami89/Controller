package com.example.controller;

/**
 * Created by Екатерина Захарова on 26.02.2016.
 */
public class DeviceInfo {
    private static String Firmvare;
    private static String Hardware;
    private static String Manufartured;
    private static String Model;
    private static String Phase;
    private static String Serial;
    private static DeviceInfo _instance = null;
    private static String device1Address;
    private static String device2Address;

    private DeviceInfo() {
    }

    public static synchronized DeviceInfo getInstance() {
        DeviceInfo deviceInfo;
        synchronized (DeviceInfo.class) {
            if (_instance == null) {
                _instance = new DeviceInfo();
            }
            deviceInfo = _instance;
        }
        return deviceInfo;
    }

    public void setModel(String model) {
        Model = model;
    }

    public void setSerial(String serial) {
        Serial = serial;
    }

    public void setHardware(String hardware) {
        Hardware = hardware;
    }

    public void setFirmvare(String firmvare) {
        Firmvare = firmvare;
    }

    public void setManufartured(String manufartured) {
        Manufartured = manufartured;
    }

    public String getModel() {
        return Model;
    }

    public String getSerial() {
        return Serial;
    }

    public String getHardware() {
        return Hardware;
    }

    public String getFirmware() {
        return Firmvare;
    }

    public String getManufactured() {
        return Manufartured;
    }

    public String getDevice1() {
        return device1Address;
    }

    public String getDevice2() {
        return device2Address;
    }

    public void setDevice1(String address) {
        device1Address = address;
    }

    public void setDevice2(String address) {
        device2Address = address;
    }

    public void setPhase(String phase) {
        Phase = phase;
    }

    public String getPhase() {
        return Phase;
    }
}

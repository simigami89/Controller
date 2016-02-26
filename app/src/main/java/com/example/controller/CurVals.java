package com.example.controller;

/**
 * Created by Екатерина Захарова on 26.02.2016.
 */
public class CurVals {
    private static CurVals _instance = null;
    private static String I_A = "----";
    private static String I_B = "----";
    private static String I_C = "----";
    private static String RSSI = "--";
    private static String U_A = "----";
    private static String U_B = "----";
    private static String U_C = "----";

    private CurVals() {
    }

    public static synchronized CurVals getInstance() {
        CurVals curVals;
        synchronized (CurVals.class) {
            if (_instance == null) {
                _instance = new CurVals();
            }
            curVals = _instance;
        }
        return curVals;
    }

    public void setI_A(String i) {
        I_A = i;
    }

    public void setU_A(String u) {
        U_A = u;
    }

    public void setI_B(String i1) {
        I_B = i1;
    }

    public void setU_B(String u1) {
        U_B = u1;
    }

    public void setI_C(String i2) {
        I_C = i2;
    }

    public void setU_C(String u2) {
       U_C = u2;
    }

    public void setRSSI(String rssi) {
        RSSI = rssi;
    }

    public String getU_C() {
        return U_C;
    }

    public String getI_C() {
        return I_C;
    }

    public String getU_B() {
        return U_B;
    }

    public String getI_B() {
        return I_B;
    }

    public String getU_A() {
        return U_A;
    }

    public String getI_A() {
        return I_A;
    }

    public String getRSSI() {
        return RSSI;
    }
}

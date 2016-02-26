package com.example.controller;

/**
 * Created by Екатерина Захарова on 26.02.2016.
 */
public class Ustavki {
    private static Ustavki _instance = null;
    private String Imfz = "10";
    private String Iozz = "10";

    private Ustavki() {
    }

    public static synchronized Ustavki getInstance() {
        Ustavki ustavki;
        synchronized (Ustavki.class) {
            if (_instance == null) {
                _instance = new Ustavki();
            }
            ustavki = _instance;
        }
        return ustavki;
    }

    public String getIozz() {
        return this.Iozz;
    }

    public String getImfz() {
        return this.Imfz;
    }

    public void setIozz(String iozz) {
        this.Iozz = iozz;
    }

    public void setImfz(String imfz) {
        this.Imfz = imfz;
    }
}

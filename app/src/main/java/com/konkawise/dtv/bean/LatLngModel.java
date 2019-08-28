package com.konkawise.dtv.bean;

public class LatLngModel {
    public static final int MODE_LONGITUDE = 0;
    public static final int MODE_LATITUDE = 1;
    public static final int LONGITUDE_THRESHOLD = 1800;
    public static final int LATITUDE_THRESHOLD = 900;

    private static final String[] DIRECTION = {"E", "W", "N", "S"};

    private int mode;
    private int threshold;
    private int value;

    public LatLngModel() {

    }

    public LatLngModel(int mode, int threshold, int value) {
        this.mode = mode;
        this.threshold = threshold;
        if (value > threshold) {
            this.value = value - 2 * threshold;
        }
    }

    public int getRawValue() {
        return value;
    }

    public int getValueForStorage() {
        if (value < 0) {
            return value + 2 * threshold;
        }
        return value;
    }

    public String getLatLngText() {
        return getUpdateText(value);
    }

    public String inputNumber(int num) {
        int oldValue = value;
        int newValue = Math.abs(oldValue * 10) + num;
        boolean positive = oldValue > 0;

        if (Math.abs(newValue) > threshold) {
            newValue = num;
        }
        if (!positive) {
            newValue = -newValue;
        }
        this.value = newValue;

        return getUpdateText(value);
    }

    public String deleteNumber() {
        this.value = value / 10;
        return getUpdateText(value);
    }

    public String switchDirection() {
        this.value = -value;
        return getUpdateText(value);
    }

    private String getUpdateText(int value) {
        int direction = 0;
        if (value < 0) {
            value = -value;
            direction = 1;
        }
        int val = value / 10;
        int fraction = value % 10;
        return DIRECTION[(mode * 2 + direction) % 4] + " " + val + "." + fraction;
    }
}

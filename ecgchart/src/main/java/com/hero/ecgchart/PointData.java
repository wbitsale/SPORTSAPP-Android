package com.hero.ecgchart;

/**
 * Created by admin on 6/30/2017.
 */

public class PointData {
    public int value;
    public int info;

    public PointData() {
        this(0);
    }

    public PointData(int _value) {
        this(_value, -1);
    }

    public PointData(int _value, int _info) {
        this.value = _value;
        this.info = _info;
    }

}

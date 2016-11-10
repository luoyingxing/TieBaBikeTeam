package com.lt.tiebabiketeam.entity;

import java.io.Serializable;

/**
 * RentNumber
 * <p/>
 * Created by luoyingxing on 16/8/28.
 */
public class RentNumber implements Serializable {
    private int rentingCount; //已出租车辆数
    private int remainCount; //剩余可租车辆数

    public RentNumber() {
    }

    public int getRentingCount() {
        return rentingCount;
    }

    public void setRentingCount(int rentingCount) {
        this.rentingCount = rentingCount;
    }

    public int getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }
}

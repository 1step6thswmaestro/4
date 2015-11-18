package com.seojunkyo.soma.controlhome.util;

/**
 * Created by seojunkyo on 15. 8. 17..
 */
public class AddressList {

    public String space;

    public String address;

    public AddressList(String space, String address) {
        this.space = space;
        this.address = address;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getSpace() {
        return space;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}

package com.coasapp.coas.utils;

import com.coasapp.coas.connectycube.data.MyConnectycubeUser;

import java.util.Comparator;
import java.util.Map;

public class SortContacts1 implements Comparator<MyConnectycubeUser> {
    @Override
    public int compare(MyConnectycubeUser o1,MyConnectycubeUser o2) {

        return o1.getStoredName().compareTo(o2.getStoredName());

    }
}
package com.coasapp.coas.connectycube;

import com.connectycube.users.model.ConnectycubeUser;

import java.util.Comparator;

public class SortConnectycubeUser implements Comparator<ConnectycubeUser> {
    @Override
    public int compare(ConnectycubeUser o1, ConnectycubeUser o2) {
        return o1.getFullName().compareTo(o2.getFullName());
    }
}

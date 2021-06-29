package com.coasapp.coas.connectycube.data;

import com.connectycube.users.model.ConnectycubeUser;

public class MyConnectycubeUser extends ConnectycubeUser {
    public String getStoredName() {
        if(storedName==null){
            return "";
        }
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String storedName = fullName;

}

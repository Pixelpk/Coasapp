package com.coasapp.coas.general;

public class AppUser {

    int userId;
    String name, login, password;

    public AppUser(int userId, String name, String login, String password) {
        this.userId = userId;
        this.name = name;
        this.login = login;
        this.password = password;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

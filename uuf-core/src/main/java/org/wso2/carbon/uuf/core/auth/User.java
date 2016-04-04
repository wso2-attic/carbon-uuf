package org.wso2.carbon.uuf.core.auth;

public class User {
    private final String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

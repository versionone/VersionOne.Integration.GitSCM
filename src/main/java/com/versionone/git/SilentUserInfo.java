package com.versionone.git;

import com.jcraft.jsch.UserInfo;

public class SilentUserInfo implements UserInfo {
    private final String password;
    private final String passphrase;

    public SilentUserInfo(String password, String passphrase) {
        this.password = password;
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getPassword() {
        return password;
    }

    public boolean promptPassword(String s) {
        return true;
    }

    public boolean promptPassphrase(String s) {
        return true;
    }

    public boolean promptYesNo(String s) {
        return false;
    }

    public void showMessage(String s) {
        System.out.println("[UserInfo Message]: " + s);
    }
}
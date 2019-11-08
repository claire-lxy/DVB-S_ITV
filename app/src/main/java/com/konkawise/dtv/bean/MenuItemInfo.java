package com.konkawise.dtv.bean;

import java.util.List;

public class MenuItemInfo {
    private String text;
    private String page;
    private boolean checkProg;
    private boolean checkPassword;
    private String callback;
    private List<MenuItemInfo> subItems;

    public String getText() {
        return text;
    }

    public String getPage() {
        return page;
    }

    public boolean isCheckProg() {
        return checkProg;
    }

    public boolean isCheckPassword() {
        return checkPassword;
    }

    public String getCallback() {
        return callback;
    }

    public List<MenuItemInfo> getSubItems() {
        return subItems;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setCheckProg(boolean checkProg) {
        this.checkProg = checkProg;
    }

    public void setCheckPassword(boolean checkPassword) {
        this.checkPassword = checkPassword;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setSubItems(List<MenuItemInfo> subItems) {
        this.subItems = subItems;
    }
}

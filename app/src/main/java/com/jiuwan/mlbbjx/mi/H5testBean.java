package com.jiuwan.mlbbjx.mi;

class H5testBean {
    String game_num;
    String value;
    String props_name;
    String role_id;
    String role_name;
    String server_id;
    String server_name;
    String productID;
    String callback_url;
    String extend_data;

    public H5testBean(String game_num, String value, String props_name, String role_id, String role_name, String server_id, String server_name, String productID, String callback_url, String extend_data) {
        this.game_num = game_num;
        this.value = value;
        this.props_name = props_name;
        this.role_id = role_id;
        this.role_name = role_name;
        this.server_id = server_id;
        this.server_name = server_name;
        this.productID = productID;
        this.callback_url = callback_url;
        this.extend_data = extend_data;
    }

    public String getGame_num() {
        return game_num;
    }

    public void setGame_num(String game_num) {
        this.game_num = game_num;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProps_name() {
        return props_name;
    }

    public void setProps_name(String props_name) {
        this.props_name = props_name;
    }

    public String getRole_id() {
        return role_id;
    }

    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getServer_name() {
        return server_name;
    }

    public void setServer_name(String server_name) {
        this.server_name = server_name;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getCallback_url() {
        return callback_url;
    }

    public void setCallback_url(String callback_url) {
        this.callback_url = callback_url;
    }

    public String getExtend_data() {
        return extend_data;
    }

    public void setExtend_data(String extend_data) {
        this.extend_data = extend_data;
    }
}

package com.rfid.app.model;

public class item_warehouse {
    private String _id;
    private String name;

    public item_warehouse(String _id, String name) {
        this._id = _id;
        this.name = name;
    }
    public item_warehouse() {

    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

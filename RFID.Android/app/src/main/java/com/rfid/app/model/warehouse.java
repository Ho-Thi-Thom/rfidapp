package com.rfid.app.model;

import java.util.List;

public class warehouse {
    private int ms;
    private String query;
    private List<item_warehouse> result;

    public warehouse(int ms, String query, List<item_warehouse> result) {
        this.ms = ms;
        this.query = query;
        this.result = result;
    }

    public int getMs() {
        return ms;
    }

    public void setMs(int ms) {
        this.ms = ms;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<item_warehouse> getResult() {
        return result;
    }

    public void setResult(List<item_warehouse> result) {
        this.result = result;
    }
}

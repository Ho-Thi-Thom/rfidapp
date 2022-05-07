package com.rfid.app.model;

public class barcode_mapping {
    private String barcodeRFID,barcodeTS;

    public barcode_mapping() {
    }

    public barcode_mapping(String barcodeRFID, String barcodeTS) {
        this.barcodeRFID = barcodeRFID;
        this.barcodeTS = barcodeTS;
    }

    public String getBarcodeRFID() {
        return barcodeRFID;
    }

    public void setBarcodeRFID(String barcodeRFID) {
        this.barcodeRFID = barcodeRFID;
    }

    public String getBarcodeTS() {
        return barcodeTS;
    }

    public void setBarcodeTS(String barcodeTS) {
        this.barcodeTS = barcodeTS;
    }
}

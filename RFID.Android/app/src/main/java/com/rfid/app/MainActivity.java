package com.rfid.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rfid.app.model.barcode_mapping;
import com.rfid.app.model.item_warehouse;
import com.rfid.app.model.product;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, IBarcodeResult {

    Spinner mySpinner;
    ArrayList<String> name_warehouse;
    ArrayList<item_warehouse> list;
    ArrayAdapter<String> myAdapter;
    EditText edtRFID, edtTaiSan;
    ImageButton btnTaiSan, btnReset, btnView, btnUpdate, btnSave;
    TextView tvVerify_barcodeRFID, tvVerify_RFID, tvVerify_Property, tvThongBao, tv_sl;
    Button btnScan;
    private boolean fIsEmulator = false;
    private RFIDWithUHFUART mReader;
    private Barcode2D barcode2D;
    private product product;
    private item_warehouse item_warehouse;
    private barcode_mapping barcode_mapping;
    ArrayList<barcode_mapping> listMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {

//            String apiT = "{\"mutations\":[{\"create\":{\"_type\":\"mapping\",\"_id\":\"556566555555565656454565654\",\"rfid\":\"4545656565555\",\"code_product\":{\"_type\":\"reference\",\"_ref\":\"822c088d-a2c9-4c11-bb43-098cbea757ba\"},\"warehouse\":{\"_type\":\"reference\",\"_ref\":\"630fda29-0bcc-46ad-b523-4b257d0bd84a\"}}}]}";
//            System.out.println("mutations==> " + apiT);
            super.onCreate(savedInstanceState);
            setContentView((R.layout.activity_main));
            Anhxa();
            apiwarehouse();
            fIsEmulator = UIHelper.isEmulator();
            UIHelper.initSound(MainActivity.this);
            initUHF();
            btnScan.setOnClickListener(new btnScanClickListener());
        } catch (Exception ex) {
            UIHelper.showExceptionError(MainActivity.this, ex);
        }
        btnTaiSan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apigetName(edtTaiSan.getText().toString());
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetText();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String barCodeRfid = tvVerify_barcodeRFID.getText().toString().trim();
//                String barCodeRfid = "CCDCMICA36";
                String rfid = tvVerify_RFID.getText().toString().trim();
//                String rfid = "49666400000000000000277D";
                String idProduct = product.getId().trim();
//                String idProduct = "c56347ec-f0ae-470c-aefd-19ab34c2429a";
                String wareHouseId = item_warehouse.get_id();
//                String wareHouseId = "13da227c-97e6-499e-a9a0-7348cc10c681";
                if (barCodeRfid.length() > 0 && rfid.length() > 0 && idProduct.length() > 0 && wareHouseId.length() > 0) {
                    postApiMapping(barCodeRfid, rfid, idProduct, wareHouseId);
                } else {
                    Toast.makeText(MainActivity.this, "Thiếu thông tin", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listMapping.clear();
                resetProduct();
                resetText();
                tvThongBao.setText("");
                tv_sl.setText("0");
            }
        });
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog(listMapping);
            }
        });
    }

    private void Dialog(ArrayList<barcode_mapping> list) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ListView lv = dialog.findViewById(R.id.lvMapping);
        ArrayAdapter adapterListMapping = new AdapterListMapping(this, R.layout.item_dialog, list);
        lv.setAdapter(adapterListMapping);
        dialog.show();
        Button btnClose = dialog.findViewById(R.id.btnDong);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btnSave = dialog.findViewById(R.id.btnLuu);
        EditText edtnote = dialog.findViewById(R.id.edtNote);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = String.valueOf(edtnote.getText());
                if (text.length() != 0) {
                    if (listMapping.size() > 0) {
                        postApiBatch(text.trim(), String.valueOf(listMapping.size()));
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "Chưa có dữ liệu trong list", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Cần nhập gợi nhớ", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void postApiBatch(String name, String count) {
//        System.out.printf("===>>>> data summit " + barCodeRfid + " " + rfid + " " + idProduct + " " + wareHouseId);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bfsmrbin.api.sanity.io/v2021-03-25/data/mutate/production?returnIds=true&returnDocuments=true&visibility=sync";
        String apiT = "{\"mutations\":[{\"create\":{\"_type\":\"batch\",\"count\":\"" + count + "\",\"name\":\"" + name + "\"}}]}";
        try {
            JSONObject obj = new JSONObject(apiT.toString() + "/oauth-token?access_token=skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray json = (JSONArray) response.get("results");
                        JSONObject jsonobject = json.getJSONObject(0);
                        String idBatch = jsonobject.getString("id");
                        //{\"patch\":{\"id\":\""+barcodeRFID+"\",\"set\":{\"batch\":{\"_type\":\"reference\",\"_ref\":\"" + idBatch + "\"}}}}
                        String abc = "";
                        if (idBatch.length() > 0) {
                            for (int i = 0; i < listMapping.size(); i++) {
                                abc += "{\"patch\":{\"id\":\"" + listMapping.get(i).getBarcodeRFID() + "\",\"set\":{\"batch\":{\"_type\":\"reference\",\"_ref\":\"" + idBatch + "\"}}}},";
                            }
                            abc = abc.substring(0, abc.length() - 1);
                            UpdateAPI(abc);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("=============================================>lỗi");
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", "Bearer " + "skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    public void UpdateAPI(String abc) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bfsmrbin.api.sanity.io/v2021-03-25/data/mutate/production?returnIds=true&returnDocuments=true&visibility=sync";
        String apiT = "{\"mutations\":[" + abc + "]}";
        System.out.println("=============abcd======" + apiT);
        try {
            JSONObject obj = new JSONObject(apiT.toString() + "/oauth-token?access_token=skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(MainActivity.this, "Xuất thông tin báo cáo thành công", Toast.LENGTH_LONG).show();
                    listMapping.clear();
                    resetProduct();
                    resetText();
                    tvThongBao.setText("");
                    tv_sl.setText("0");

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("=============================================>lỗi");
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", "Bearer " + "skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    public void resetText() {
        edtRFID.setText("");
        edtTaiSan.setText("");
        tvVerify_Property.setText("");
        tvVerify_RFID.setText("");
        tvVerify_barcodeRFID.setText("");
    }

    public void resetProduct() {
        product.setId("");
        product.setBarcode("");
    }

    @Override
    public void getBarcode(String barcode) {
        if (edtRFID.isFocused()) {
            edtRFID.setText(barcode);
            tvVerify_barcodeRFID.setText(barcode);
        }
        if (edtTaiSan.isFocused()) {
            edtTaiSan.setText(barcode);
            apigetName(barcode);
        }
    }

    public void apigetName(String barcodeTS) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bfsmrbin.api.sanity.io/v2021-10-21/data/query/production?query=*%5B_type%20%3D%3D%20'product'%20%26%26%20barcode.current%3D%3D'" + barcodeTS + "'%5D%20%7B%0A%20%20%20%20_id%2C%0A%20%20%20%20barcode%2C%0A%20%20%20%20name%2C%0A%7D";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                product = new product();
                try {
                    JSONArray json = (JSONArray) response.get("result");
                    JSONObject jsonobject = json.getJSONObject(0);
                    String _id = jsonobject.getString("_id");
                    String name = jsonobject.getString("name");
                    JSONObject barcodeoj = jsonobject.getJSONObject("barcode");
                    String barcode = barcodeoj.getString("current");
                    product.setId(_id);
                    product.setBarcode(barcode);
                    tvVerify_Property.setText(name);
                } catch (JSONException e) {
                    tvVerify_Property.setText("");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);

    }

    public void postApiMapping(String barCodeRfid, String rfid, String idProduct, String wareHouseId) {
//        System.out.printf("===>>>> data summit " + barCodeRfid + " " + rfid + " " + idProduct + " " + wareHouseId);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bfsmrbin.api.sanity.io/v2021-03-25/data/mutate/production?returnIds=true&returnDocuments=true&visibility=sync";
        String apiT = "{\"mutations\":[{\"create\":{\"_type\":\"mapping\",\"_id\":\"" + barCodeRfid + "\",\"rfid\":\"" + rfid + "\",\"code_product\":{\"_type\":\"reference\",\"_ref\":\"" + idProduct + "\"},\"warehouse\":{\"_type\":\"reference\",\"_ref\":\"" + wareHouseId + "\"}}}]}";
        try {
            JSONObject obj = new JSONObject(apiT.toString() + "/oauth-token?access_token=skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray json = (JSONArray) response.get("results");
                        JSONObject jsonobject = json.getJSONObject(0);
                        String barcodeRFID = jsonobject.getString("id");
                        tvThongBao.setText("Mapping thành công");
                        tvThongBao.setTextColor(Color.BLUE);
                        int sl = Integer.parseInt(tv_sl.getText().toString().trim()) + 1;
                        tv_sl.setText(String.valueOf(sl));
                        resetText();
                        barcode_mapping = new barcode_mapping();
                        barcode_mapping.setBarcodeRFID(barcodeRFID);
                        barcode_mapping.setBarcodeTS(product.getBarcode());
                        listMapping.add(barcode_mapping);
                        resetProduct();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    tvThongBao.setText("Mapping không thành công");
                    tvThongBao.setTextColor(Color.RED);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Authorization", "Bearer " + "skgAo7LHns2hrjuTT2KDMeqCxIo4rWNExaR8JGhpYeKDikAXBnIQzvMI6N2fhvTQr6M9UciwrIEmpnIFZFC26S5gSkE4F48n8rmwzaQrukUmSVAKan6ZBjmGD7K7HSeE6ng0FMk5vTxVLy4aiIx0xWvZawwfMpDFBiZvoP3P2tQsbT6qutPw");
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    public class btnScanClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (edtRFID.isFocused()) {
                readTag();
                start();
            } else if (edtTaiSan.isFocused()) {
                start();
            }
        }
    }

    private void start() {
        barcode2D.startScan(this);
    }

    private void readTag() {
        if (btnScan.getText().equals(getString(R.string.btnScan))) {
            if (mReader == null) {
                UIHelper.ToastMessage(MainActivity.this, R.string.uhf_msg_sdk_open_fail);
                return;
            }
            UHFTAGInfo strUII = mReader.inventorySingleTag();
            if (strUII != null) {
                String strEPC = strUII.getEPC();
                if (strEPC.length() > 0) {
                    edtRFID.setText(strEPC);
                    tvVerify_RFID.setText(strEPC);
                }
                UIHelper.playSoundSuccess();
            } else {
                UIHelper.ToastMessage(MainActivity.this, R.string.uhf_msg_inventory_fail);
            }
        } else {
            btnScan.setText("Scan");
        }
    }

    public void initUHF() {
        // temporary check this, on emulator device mReader InitTask cause crash application
        if (!fIsEmulator) {
            if (mReader == null) {
                try {
                    mReader = RFIDWithUHFUART.getInstance();
                } catch (Exception ex) {
                    UIHelper.showExceptionError(MainActivity.this, ex);
                    return;
                }

                if (mReader != null) {
                    new InitTask().execute();
                }
            }
        }
    }

    private class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                open();
                Thread.sleep(1000);
                return mReader.init();
            } catch (Exception ex) {
                return false;
            }
        }

        private void open() {
            barcode2D.open(MainActivity.this, MainActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                Toast.makeText(MainActivity.this, "init fail", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            try {
                super.onPreExecute();

                mypDialog = new ProgressDialog(MainActivity.this);
                mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mypDialog.setMessage("init...");
                mypDialog.setCanceledOnTouchOutside(false);
                mypDialog.show();

            } catch (Exception ex) {
                UIHelper.showExceptionError(MainActivity.this, ex);
                return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (event.getRepeatCount() == 0) {
                if (edtRFID.isFocused()) {
                    readTag();
                    start();
                } else if (edtTaiSan.isFocused()) {
                    start();
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void Anhxa() {
        name_warehouse = new ArrayList<>();
        list = new ArrayList<>();
        mySpinner = (Spinner) findViewById(R.id.planets_spinner);
        mySpinner.setOnItemSelectedListener(this);
        myAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, name_warehouse);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);
        edtRFID = findViewById(R.id.edtRFID);
        btnTaiSan = findViewById(R.id.btnTaiSan);
        edtTaiSan = findViewById(R.id.edtTaiSan);
        tvThongBao = findViewById(R.id.tvThongBao);
        tvVerify_barcodeRFID = findViewById(R.id.tvVerify_barcodeRFID);
        tvVerify_RFID = findViewById(R.id.tvVerify_RFID);
        tvVerify_Property = findViewById(R.id.tvVerify_Property);
        tv_sl = findViewById(R.id.tv_sl);
        btnReset = findViewById(R.id.btnReset);
        btnView = findViewById(R.id.btnView);
        btnSave = findViewById(R.id.btnSave);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnScan = findViewById(R.id.btnScan);
        barcode2D = new Barcode2D(this);
        product = new product();
        item_warehouse = new item_warehouse();
        listMapping = new ArrayList<>();
//        listMapping.add(new barcode_mapping("TBVS180308", "ad9b6d5a-89f5-4f15-ac4e-b40b0bfd0bac"));
//        listMapping.add(new barcode_mapping("CCDCMICA36", "ad9b6d5a-89f5-4f15-ac4e-b40b0bfd0bac"));

    }

    public void apiwarehouse() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://bfsmrbin.api.sanity.io/v2021-10-21/data/query/production?query=*%5B_type%20%3D%3D%20%27warehouse%27%5D%20%7B%0A%20%20%20%20%20%20%20%20_id%2C%0A%20%20%20%20%20%20%20%20name%2C%0A%20%20%20%20%7D";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray json = (JSONArray) response.get("result");
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject jsonobject = json.getJSONObject(i);
                        String _id = jsonobject.getString("_id");
                        String name = jsonobject.getString("name");
                        name_warehouse.add(name);
                        item_warehouse item = new item_warehouse(_id, name);
                        list.add(item);
                    }
                    myAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(name_warehouse.get(0));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        item_warehouse.set_id(list.get(i).get_id());
        item_warehouse.setName(list.get(i).getName());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        System.out.println("=========lỗi");
    }
}
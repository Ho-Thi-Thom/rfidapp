package com.rfid.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rfid.app.model.barcode_mapping;

import java.util.List;

public class AdapterListMapping extends ArrayAdapter<barcode_mapping> {
    private final Context context;
    private final List<barcode_mapping> userList;

    public AdapterListMapping(@NonNull Context context, int resource, @NonNull List<barcode_mapping> objects) {
        super(context, resource, objects);
        userList = objects;
        this.context = context;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_dialog, parent, false);
        TextView rfid = rowView.findViewById(R.id.tvRFID);
        TextView ts = rowView.findViewById(R.id.tvTaiSan);
        barcode_mapping user = userList.get(position);

        rfid.setText(user.getBarcodeRFID().trim());
        ts.setText(user.getBarcodeTS().trim());

        return rowView;
    }
}
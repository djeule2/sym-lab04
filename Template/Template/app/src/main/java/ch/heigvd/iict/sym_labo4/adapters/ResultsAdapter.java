package ch.heigvd.iict.sym_labo4.adapters;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.heigvd.iict.sym_labo4.R;

public class ResultsAdapter extends BaseAdapter {

    private static final String TAG = ResultsAdapter.class.getSimpleName();

    private Context context;

    private List<ScanResult> results = new ArrayList<>();

    public ResultsAdapter(Context context) {
        this.context = context;
    }

    public void clear() {
        this.results.clear();
        notifyDataSetChanged();
    }

    public void addDevice(ScanResult newResult) {
        if(newResult == null) return;
        boolean alreadyInAdapter = false;
        for(ScanResult device : results) {
            if(device.getDevice().getAddress().equalsIgnoreCase(newResult.getDevice().getAddress())) {
                alreadyInAdapter = true;
                break;
            }
        }
        if(!alreadyInAdapter) {
            this.results.add(newResult);
            Collections.sort(this.results, (ScanResult o1, ScanResult o2) -> o1.getRssi() - o2.getRssi());
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return this.results == null ? 0 : this.results.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getItem(int position) {
        return this.results == null ? null : this.results.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.scan_item, parent, false);
        }
        //link top gui
        TextView name    = convertView.findViewById(R.id.scan_item_name);
        TextView address = convertView.findViewById(R.id.scan_item_address);

        //fill gui
        ScanResult result = (ScanResult) getItem(position);
        String deviceName = result.getDevice().getName();
        if(deviceName == null || deviceName.trim().isEmpty())
            deviceName = this.context.getString(android.R.string.unknownName);
        name.setText(deviceName);
        address.setText(result.getDevice().getAddress());

        return convertView;
    }

}

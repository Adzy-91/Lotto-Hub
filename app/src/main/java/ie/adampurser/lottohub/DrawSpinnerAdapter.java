package ie.adampurser.lottohub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawSpinnerAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mTitles;

    public DrawSpinnerAdapter(Context context, String[] titles){
        super(context, 0);

        mContext = context;
        mTitles = titles;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.draw_spinner_item, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinnerItem);
        textView.setText(mTitles[position]);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.draw_spinner_dropdown_item, parent, false);
            convertView.setTag("DROPDOWN");
        }

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(mTitles[position]);

        return convertView;
    }
}

package net.flowgrammer.flowsmssender.jobs;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.flowgrammer.flowsmssender.R;
import net.flowgrammer.flowsmssender.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by neox on 5/17/16.
 */
public class JobsAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JobsAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }

    public void clear() {
        mJsonArray = new JSONArray();
        notifyDataSetChanged();
    }

    public void updateData(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.optJSONObject(i);
            mJsonArray.put(object);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mJsonArray.length();
    }

    @Override
    public Object getItem(int position) {
        return mJsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.row_job, null);

            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.text_title);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.text_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject jsonObject = (JSONObject) getItem(position);

        String title = jsonObject.optString("name");
        String date = jsonObject.optString("created");
        date = getDateStringFromGMT(date);



        holder.titleTextView.setText(title);
        holder.dateTextView.setText(date);

        convertView.setMinimumHeight((int) Util.convertDpToPixel(45, mContext));

        return convertView;
    }

    private String getDateStringFromGMT(String inputText) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");

        Date date = null;
        try {
            date = inputFormat.parse(inputText);
        } catch (ParseException e) {
            e.printStackTrace();
            return inputText;
        }
        String outputText = outputFormat.format(date);
        return outputText;
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;
    }
}

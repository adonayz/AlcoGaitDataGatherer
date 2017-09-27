package edu.wpi.alcogaitdatagatherer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Created by Adonay on 9/26/2017.
 */

public class SurveyListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private LinkedList<File> files;

    private class ViewHolder {
        TextView fileIDTextView;
        TextView dateModifiedTextView;
    }

    public SurveyListAdapter(Context context, LinkedList<File> files) {
        inflater = LayoutInflater.from(context);
        this.files = files;
    }

    public int getCount() {
        return files.size();
    }

    public File getItem(int position) {
        return files.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.survey_list_view, null);
            holder.fileIDTextView = (TextView) convertView.findViewById(R.id.fileIDTextView);
            holder.dateModifiedTextView = (TextView) convertView.findViewById(R.id.dateModifiedTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File file = files.get(position);
        String fileName = file.getName();
        String fileShouldStartWith = "ID_";
        String fileShouldEndWith = ".csv";

        String ID = fileName.substring(fileShouldStartWith.length(), fileName.length() - fileShouldEndWith.length());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy   hh:mm a");
        String lastDateModified = sdf.format(file.lastModified());

        holder.fileIDTextView.setText("Subject ID " + ID);
        holder.dateModifiedTextView.setText(lastDateModified);
        return convertView;
    }
}
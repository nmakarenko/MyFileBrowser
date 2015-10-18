package com.samanta.myfilebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class MySimpleArrayAdapter  extends BaseAdapter {//extends ArrayAdapter<DownloadedFile> {
    private LayoutInflater inflater;
    private List<DownloadedFile> data;
    private Context ct;

    public MySimpleArrayAdapter(Context context, List<DownloadedFile> objects) {
     //   super(context, R.layout.dwnload_item, objects);
        ct = context;
        inflater= LayoutInflater.from(context);
        this.data=objects;
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView iv;
        TextView name;
        TextView location;
        TextView size;
        TextView date;

        final String fileLocation;
        final View parentView = parent;

        File checkFile = new File(data.get(position).getLocation() + "/" + data.get(position).getName());

        //if it's not create convertView yet create new one and consume it
        if(convertView == null) {
            //instantiate convertView using our employee_list_item
            convertView = inflater.inflate(R.layout.dwnload_item, null);
        }

        iv = (ImageView) convertView.findViewById(R.id.imDownload);
        name = (TextView) convertView.findViewById(R.id.tvName);
        location = (TextView) convertView.findViewById(R.id.tvLocation);
        size = (TextView) convertView.findViewById(R.id.tvSize);
        date = (TextView) convertView.findViewById(R.id.tvDate);

        switch(data.get(position).getExtension()){
            case "png": iv.setBackgroundResource(R.drawable.png_icon);
                break;
            case "doc": iv.setBackgroundResource(R.drawable.doc_icon);
                break;
            case "pdf": iv.setBackgroundResource(R.drawable.pdf_icon);
                break;
            default: iv.setBackgroundResource(R.drawable.file_icon);
                break;
        }

        if(checkFile.exists()) location.setText(data.get(position).getLocation());
        else location.setText("Deleted");

        int fileSize = data.get(position).getSize();
        if(fileSize < 1024) size.setText("" + fileSize + " B");
        else if(fileSize < 1024 * 1024) size.setText("" + fileSize/1024 + " Kb");
        else if(fileSize < 1024 * 1024 * 1024) size.setText("" + fileSize/(1024 * 1024) + " Mb");
        else size.setText("" + fileSize/(1024 * 1024 * 1024) + " Gb");

        date.setText(data.get(position).getDate());
        name.setText(data.get(position).getName());


        fileLocation = data.get(position).getLocation();
        final String fileName = data.get(position).getName();

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  displayListView FileBrowser
                ListView listView = (ListView) ((View)parentView.getParent().getParent()).findViewById(R.id.lvFileBrowser);
                File file = new File(fileLocation + "/" + fileName);
                if(file.exists()) {
                    if (file.getParentFile() != null)
                        listView.setAdapter(new FileArrayAdapter(ct, fileLocation, true));
                    else listView.setAdapter(new FileArrayAdapter(ct, fileLocation, false));
                }
            }
        });
        return convertView;
    }
}
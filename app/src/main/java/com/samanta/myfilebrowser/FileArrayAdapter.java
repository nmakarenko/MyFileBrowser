package com.samanta.myfilebrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;

import java.io.File;
import java.util.List;

/**
 * Created by Наталия on 13.09.2015.
 */
public class FileArrayAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private String path;
    private Context ct;
    private  File[] listOfFiles;
    private String begin = "...";
    private boolean hasParentFile;

    public FileArrayAdapter(Context context, String path, boolean hasParentFile) {
  //      super(context, R.layout.file_item, objects);
        this.ct = context;
        inflater = LayoutInflater.from(context);
        this.path = path;
        this.hasParentFile = hasParentFile;

        File folder = null;
        try {
            folder = new File(this.path);
            File[] fileList = folder.listFiles();
            this.listOfFiles = fileList;

            int j = 0;
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    this.listOfFiles[j] = fileList[i];
                    fileList = folder.listFiles();
                    j++;
                }
            }
            for (int i = 0; i < fileList.length; i++) {
                if (!fileList[i].isDirectory()) {
                    this.listOfFiles[j] = fileList[i];
                    fileList = folder.listFiles();
                    j++;
                }
            }
        }catch(NullPointerException e){
            this.listOfFiles = null;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        int res;
        if(listOfFiles == null) res = 0;
        else res = listOfFiles.length;
        return hasParentFile? res + 1 : res;
    }

    @Override
    public Object getItem(int position) {
        return hasParentFile? (position == 0? begin : this.listOfFiles[position - 1]) : this.listOfFiles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;
        TextView name;

        int curr = hasParentFile? position - 1 : position;

        final View parentView = parent;

        //if it's not create convertView yet create new one and consume it
        if(convertView == null)
            //instantiate convertView using our employee_list_item
            convertView = inflater.inflate(R.layout.file_item, null);

        iv = (ImageView) convertView.findViewById(R.id.imFile);
        name = (TextView) convertView.findViewById(R.id.tvFileName);

        if(position == 0 && hasParentFile){
            iv.setBackgroundResource(R.drawable.back);
            name.setText(new File(this.path).getParentFile().getName());
        }
        else {
            if (listOfFiles[curr].isDirectory()) {
                iv.setBackgroundResource(R.drawable.folder_icon);
            } else {
                iv.setBackgroundResource(R.drawable.file_icon);
            }
            name.setText(listOfFiles[curr].getName());
        }

        //settings for onClick
        final int pos = position;
        final boolean isDir;
        final String absPathDir;

        if(hasParentFile) {
            if (pos > 0) {
                isDir = listOfFiles[curr].isDirectory();
                if (isDir == true)
                    absPathDir = listOfFiles[curr].getAbsolutePath();
                else absPathDir = this.path;
            } else {
                isDir = false;
                absPathDir = new File(this.path).getParentFile().getAbsolutePath();
            }
        }
        else{
            isDir = listOfFiles[curr].isDirectory();
            if (isDir == true)
                absPathDir = listOfFiles[curr].getAbsolutePath();
            else absPathDir = this.path;
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView listView = (ListView) ((View)parentView.getParent()).findViewById(R.id.lvFileBrowser);

                File file = new File(absPathDir);
                if (file.getParentFile() != null)
                    listView.setAdapter(new FileArrayAdapter(ct, absPathDir, true));
                else listView.setAdapter(new FileArrayAdapter(ct, absPathDir, false));
//                if(pos == 0 || isDir == true) listView.setAdapter(new FileArrayAdapter(ct, absPathDir, true));
            }
        });
        return convertView;
    }
}

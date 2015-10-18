package com.samanta.myfilebrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    String myHTTPUrl = "http://www.wingnity.com/files/wingnity_logo.png";
    ProgressBar progressBar;
    TextView tvPercent;
    String fileLocation = null;
    String fileName = null;
    File rootDirectory = null;
    int fileSize;
    String fileExtension;
    List<DownloadedFile> data;

    private static Context context;

    private AlertDialog.Builder dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button dwnload = (Button) findViewById(R.id.btnDownload);
        EditText tvURL = (EditText) findViewById(R.id.etUrl);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvPercent = (TextView) findViewById(R.id.tvPercent);

        tvPercent.setText("0 %");

        MainActivity.context = getApplicationContext();

        final ListView listview = (ListView) findViewById(R.id.lvDownloads);

        //create file list
        data = new ArrayList<DownloadedFile>();

        //get downloaded files before running app
        deserialization();

        listview.setAdapter(new MySimpleArrayAdapter(this, data));

        tvURL.setText(myHTTPUrl);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDialog();
            }
        };

        dwnload.setOnClickListener(onClickListener);
    }

    //shown if chosen filename already exists
    void myWarning(){
        dialogBuilder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);

        textView.setText("File with this name exists. Do you want to reload it?");

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(textView);

        dialogBuilder.setView(ll);

        dialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //start downloading file
                URL myUrl = null;
                try {
                    myUrl = new URL(myHTTPUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                new MyTask().execute(myUrl);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new Dialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDialog();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    void incorrectPathWarning(){
        dialogBuilder = new AlertDialog.Builder(this);
        TextView textView = new TextView(this);

        textView.setText("Wrong path of file.");

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(textView);

        dialogBuilder.setView(ll);

        dialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    void myDialog(){
        dialogBuilder = new AlertDialog.Builder(this);
        final EditText etLocation = new EditText(this);
        final EditText etNewFileName = new EditText(this);
        final CheckBox chbNewFileName = new CheckBox(this);
        TextView tvLocation = new TextView(this);

        tvLocation.setText("Location:");
        chbNewFileName.setText("Enter new file name:");

        etLocation.setText("/My files");

        LinearLayout ll=new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(tvLocation);
        ll.addView(etLocation);
        ll.addView(chbNewFileName);
        ll.addView(etNewFileName);

        dialogBuilder.setView(ll);

        dialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //choosing/creating directory
                fileLocation = "" + Environment.getExternalStorageDirectory();
                String filePath = etLocation.getText().toString();
                if(!etLocation.getText().toString().isEmpty()) {
                    if(filePath.charAt(0) != '/') filePath = "/" + filePath;
                    fileLocation += filePath;
                }
                else {
                    fileLocation += "/Download";
                }
                try {
                    rootDirectory = new File(fileLocation);
                }catch(Exception e){
                    incorrectPathWarning();
                }

                if (!rootDirectory.exists()) rootDirectory.mkdirs();

                //getting name of file
                fileExtension = MimeTypeMap.getFileExtensionFromUrl(myHTTPUrl);
                String nameOfFile = etNewFileName.getText().toString();

                if(chbNewFileName.isChecked() && !etNewFileName.getText().toString().isEmpty()) {
                    fileName = nameOfFile + "." + fileExtension;
                    nameOfFile = fileName;
                }
                else nameOfFile = URLUtil.guessFileName(myHTTPUrl, null, fileExtension);

                File newFile = new File(rootDirectory, nameOfFile);
                if(newFile.exists()) myWarning();
                else {
                    //start downloading
                    URL myUrl = null;
                    try {
                        myUrl = new URL(myHTTPUrl);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    new MyTask().execute(myUrl);
                }


            }
        });

        dialogBuilder.setNegativeButton("Cancel", new Dialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }




    public class MyTask extends AsyncTask<URL, Integer, Integer>{

        private int getFileSize(URL url) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD");
                conn.getInputStream();
                return conn.getContentLength();
            } catch (IOException e) {
                return -1;
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected Integer doInBackground(URL... params) {

            try {
                URL myurl = new URL(myHTTPUrl);

                HttpURLConnection connection = (HttpURLConnection) myurl.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.connect();

                //check before creating a new file
                if(fileLocation == null) {
                    fileLocation = Environment.getExternalStorageDirectory() + "/Download";
                }

                fileExtension = MimeTypeMap.getFileExtensionFromUrl(myHTTPUrl);
                String nameOfFile = null;
                if(fileName == null)
                    fileName = URLUtil.guessFileName(myHTTPUrl, null, fileExtension);

                File file = new File(rootDirectory, fileName);


                file.createNewFile();

                fileSize = getFileSize(myurl);
                progressBar.setMax(fileSize);
                progressBar.setProgress(0);

                InputStream inputStream = connection.getInputStream();

                FileOutputStream output = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int byteCount = 0;

                int count = 0;

                while((byteCount = inputStream.read(buffer)) > 0){
                    System.out.println(byteCount + " ");
                    output.write(buffer, 0, byteCount);
                    count += byteCount;
                    progressBar.setProgress(count);
                    publishProgress(count, fileSize);
                }

                output.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            int current = progress[0];
            int total = progress[1];

            float percentage = 100 * (float)current / (float)total;

            // Display your progress here
            tvPercent.setText(percentage + " %");

            if(Math.abs(percentage - 100) < 0.0001){
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                DownloadedFile downloadedFile = new DownloadedFile(fileName, fileLocation, fileSize, fileExtension,
                        dateFormat.format(date));
                data.add(0, downloadedFile);

                //write new downloaded file to the list of downloaded files
                //using object serialization
                try
                {
                    File dir = new File(Environment.getExternalStorageDirectory() + "/DownloadManager");
                    if(!dir.exists()) dir.mkdirs();
                    File f = new File(dir, "downloads.txt"); //get data from f
                    File fo = new File(dir, "downloadsOutput.txt");//put data into fo
                    if(!fo.exists()) fo.createNewFile();

                    if(f.exists()) {
                        FileOutputStream out = new FileOutputStream(fo);
                        ObjectOutputStream oout = new ObjectOutputStream(out);

                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream
                                (Environment.getExternalStorageDirectory() + "/DownloadManager/downloads.txt"));

                        // read what we wrote before


                        oout.writeObject(downloadedFile);

                        downloadedFile = (DownloadedFile) ois.readObject();

                        while(downloadedFile != null) {
                            // write something in the file
                            //check if this file exists
                            File checkFile = new File(downloadedFile.getLocation() + "/" + downloadedFile.getName());
                            if(!checkFile.exists()) downloadedFile.setIsDeleted(true);

                            oout.writeObject(downloadedFile);
                            downloadedFile = (DownloadedFile) ois.readObject();
                        }

                        // close the stream
                        oout.close();
                        ois.close();



                    }
                    else{
                        f.createNewFile();
                        FileOutputStream out = new FileOutputStream(fo);
                        ObjectOutputStream oout = new ObjectOutputStream(out);
                        oout.writeObject(downloadedFile);
                        oout.close();

                    }


                }catch(IOException e)
                {
                    e.printStackTrace();
                }
                catch(ClassNotFoundException e){
                    e.printStackTrace();
                }
                //copy from downloadsOutput to downloads
                FileChannel inputChannel = null;
                FileChannel outputChannel = null;
                File dir = new File(Environment.getExternalStorageDirectory() + "/DownloadManager");
                dir.mkdirs();
                File f = new File(dir, "downloads.txt");
                File fo = new File(dir, "downloadsOutput.txt");
                try {
                    inputChannel = new FileInputStream(fo).getChannel();
                    outputChannel = new FileOutputStream(f).getChannel();
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());

                    inputChannel.close();
                    outputChannel.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    void deserialization(){
        DownloadedFile downloadedFile = null;
        try
        {
            ObjectInputStream ois =
                    new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + "/DownloadManager/downloads.txt"));

            // read what we downloaded before running app and add items to list
            downloadedFile = (DownloadedFile) ois.readObject();

            while(downloadedFile != null){

                data.add(downloadedFile);
                downloadedFile = (DownloadedFile) ois.readObject();

            }

        }catch(IOException i)
        {
            i.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

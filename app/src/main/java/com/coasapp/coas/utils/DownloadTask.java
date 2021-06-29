package com.coasapp.coas.utils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTask extends AsyncTask<String, String, String> {

    Context context;
    Activity activity;
    public String requestCode, extension;
    DownloadCallbacks downloadCallbacks;
    String filename, filePath;

    public DownloadTask(Context context, Activity activity, String requestCode, String filename, String extension, DownloadCallbacks downloadCallbacks) {
        this.context = context;
        this.activity = activity;
        this.requestCode = requestCode;
        this.filename = filename;
        this.extension = extension;
        this.downloadCallbacks = downloadCallbacks;
    }


    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //showProgressDialog(R.string.please_wait);
        //showDialog(progress_bar_type);
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            // filename = url.getPath().substring(url.getPath().lastIndexOf("/") + 1)+ ".mp4";

            String filenameToSave = filename + "." + extension;
            URLConnection conection = url.openConnection();
            conection.setRequestProperty("Accept-Encoding", "identity");

            conection.connect();
            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);


            //  filePath = Environment.getExternalStorageDirectory().toString()+ "/lkf/" + filename ;

            String extr = Environment.getExternalStorageDirectory().toString();

            File coasFolder = new File(extr + "/COASAPP");

            if (!coasFolder.exists()) {
                coasFolder.mkdir();
            }

            File cDir = activity.getApplication().getExternalFilesDir(null);
            //  File saveFilePath = new File(cDir.getPath() + "/lkf/" + filename);

            filePath = coasFolder +"/"+ filenameToSave;

            Log.i("DownloadPath",filePath);

           /* String lkf = cDir.getPath() + "/.download/";

            File dir = new File(lkf);

            if (!dir.exists())
                dir.mkdir();
*/
            // Output stream
            // OutputStream output = new FileOutputStream(Environment
            //        .getExternalStorageDirectory().toString() + "/lkf/" + filename);


            OutputStream output = new FileOutputStream(filePath);


            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return filePath;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        downloadCallbacks.downloadProgressUpdate(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {

        downloadCallbacks.downloadComplete(requestCode, filePath);
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
        //dismissProgress();
        //showToast(String.valueOf("Download File Success to ") + filename);
          /*  if (filePath != null) {
                File file = new File(filePath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(getContext(),
                        "com.myapp.learnkoreanforfun",
                        file);
                intent.setData(fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } */


    }
}

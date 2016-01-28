package nusem.oz.uploadmanagersamples;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import nusem.oz.uploadmanager.Builders.FileUploadDataBuilder;
import nusem.oz.uploadmanager.UploadManager;

public class UploadManagerActivity extends AppCompatActivity {

    // SET THIS TO YOUR URL //
    private static final String UPLOAD_URL = "";

    private static final String DEMO_FILE_NAME = "skydive.jpg";
    private static final String TAG = "oznusem.uploadmanger";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile(view);
            }
        });

        saveFile();
    }

    private void saveFile() {
        try {
            Log.d(TAG, "saveFile - enter");

            final InputStream is = getResources().getAssets().open(DEMO_FILE_NAME);
            File file = new File(getFilesDir(),DEMO_FILE_NAME);
            OutputStream out = new FileOutputStream(file);
            copyFile(is,out);
            is.close();
            out.flush();
            out.close();

        } catch (IOException e) {
            Log.e(TAG,"something went wrong" + e);
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void uploadFile(View view) {

        if (UPLOAD_URL.equals("")) {
            showNoUrlSnackBar(view);
            return;
        }

        File file = new File(getFilesDir(),DEMO_FILE_NAME);
        Uri uri = Uri.fromFile(file);

        try {
            UploadManager.with(this)
                    .uploadFile(new FileUploadDataBuilder(uri, this).setParamName("userPhoto").build())
                    .to(UPLOAD_URL)
                    .execute();

        } catch (MalformedURLException | URISyntaxException e) {
            Log.e(TAG,"something went wrong" + e);
        }

    }

    private void showNoUrlSnackBar(View view) {
        final Snackbar snackBar = Snackbar.make(view, R.string.fill_url, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
    }
}

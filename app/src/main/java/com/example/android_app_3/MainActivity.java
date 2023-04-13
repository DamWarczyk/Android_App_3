package com.example.android_app_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.Manifest;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    DownloadService downloadService = new DownloadService();

    //Params for permission
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 111;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 112;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 113;
    private static final int FOREGROUND_SERVICE_REQUEST_CODE = 114;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText addressInput = findViewById(R.id.adresEditText);

        findViewById(R.id.downloadInfoButton).setOnClickListener(view -> {
            DownloadAsync downloadAsync = new DownloadAsync();
            try {
                List<String> downloadInfo = downloadAsync.execute(addressInput.getText().toString()).get();
                TextView size = findViewById(R.id.numberSizeTextLabel);
                TextView type = findViewById(R.id.numberTypeFileTextLabel);
                size.setText(downloadInfo.get(0));
                type.setText(downloadInfo.get(1));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        findViewById(R.id.downloadButton).setOnClickListener(view -> downloadFile(addressInput.getText().toString()));
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            ProgressInfo progressInfo = bundle.getParcelable("INFO");
            TextView numberDownload = findViewById(R.id.numberByteDownloadTextLabel);
            numberDownload.setText(String.valueOf(progressInfo.downloadBytes));
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setProgress(progressInfo.progress);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(DownloadService.NOTIFICATOR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void downloadFile(String link) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent getPermission = new Intent();
                getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getPermission);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_REQUEST_CODE);
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        FOREGROUND_SERVICE_REQUEST_CODE);
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                        MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }

        DownloadService.runService(MainActivity.this, link);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        TextView dbytes = findViewById(R.id.numberByteDownloadTextLabel);
        outState.putString("DBytes", dbytes.getText().toString());

        TextView size = findViewById(R.id.numberSizeTextLabel);
        outState.putString("SIZE", size.getText().toString());

        ProgressBar progressBar = findViewById(R.id.progressBar);
        outState.putInt("PROGRESS", progressBar.getProgress());

        TextView type = findViewById(R.id.numberTypeFileTextLabel);
        outState.putString("TYPE", type.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView dbytes = findViewById(R.id.numberByteDownloadTextLabel);
        dbytes.setText(savedInstanceState.getString("DBytes"));

        TextView size = findViewById(R.id.numberSizeTextLabel);
        size.setText(savedInstanceState.getString("SIZE"));

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(savedInstanceState.getInt("PROGRESS"));

        TextView type = findViewById(R.id.numberTypeFileTextLabel);
        type.setText(savedInstanceState.getString("TYPE"));
    }
}
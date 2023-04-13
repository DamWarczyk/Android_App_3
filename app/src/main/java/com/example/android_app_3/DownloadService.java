package com.example.android_app_3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class DownloadService extends IntentService {
    private static final String DOWNLOAD_ACTION = "com.example.android_app_3.action.DOWNLOAD_ACTION";
    private static final String DOWNLOAD_LINK = "com.example.android_app_3.extra.DOWNLOAD_LINK";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private final ProgressInfo progressInfo = new ProgressInfo();
    public static final String NOTIFICATOR = "com.example.android_app_3.MainActivity";

    public DownloadService() {
        super("Download Service");
    }

    public static void runService(Context context, String param){
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DOWNLOAD_ACTION);
        intent.putExtra(DOWNLOAD_LINK, param);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prepareNotification();
        startForeground(NOTIFICATION_ID, createNotification());

        if(intent != null) {
            final String action = intent.getAction();

            if(DOWNLOAD_ACTION.equals(action)){
                final String downloadLink = intent.getStringExtra(DOWNLOAD_LINK);
                downloadFile(downloadLink);

                notificationManager.notify(NOTIFICATION_ID, createNotification());
            } else {
                Log.e("DownloadFileService", "Unknown action");
            }
        }
        Log.d("DownloadFileService", "Done");

    }

    private void downloadFile(String link){
        DownloadAsync downloadAsync = new DownloadAsync();
        try {
            List<String> effect = downloadAsync.execute(link).get();
            progressInfo.size = Integer.parseInt(effect.get(0));
        } catch (ExecutionException | InterruptedException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
            toast.show();
            progressInfo.status = "Error";
        }
        Log.d("Downloading" , link);
        InputStream webStream = null;

        FileOutputStream fileOutputStream = null;
        try {
            URL url = new URL(link);
            File workingFile = new File(url.getFile());
            File outputFile = new File(Environment.getExternalStorageDirectory() + File.separator + workingFile.getName());
            if (!outputFile.exists()){
                outputFile.createNewFile();}

            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert connection != null;
            DataInputStream reader = new DataInputStream(connection.getInputStream());
            fileOutputStream = new FileOutputStream(outputFile.getPath());
            byte[] buffer = new byte[progressInfo.size];
            int downloaded = reader.read(buffer, 0, progressInfo.size);
            progressInfo.downloadBytes = downloaded;
            progressInfo.status = "Downloading file";
            while(downloaded != -1){
                fileOutputStream.write(buffer, 0, downloaded);
                downloaded = reader.read(buffer, 0, progressInfo.size);
                progressInfo.downloadBytes += downloaded;
                if(downloaded != -1){
                    progressInfo.progress = (int)(((float)progressInfo.downloadBytes / (float) progressInfo.size) * 100);
                    Log.d(getString(R.string.pobrano), progressInfo.progress + "%");
                    Intent intent = new Intent(NOTIFICATOR);
                    intent.putExtra("INFO", progressInfo);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    notificationManager.notify(NOTIFICATION_ID, createNotification());
                }
            }
            progressInfo.status = "Finished";

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (webStream != null) {
                try {
                    webStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "666");
        notificationBuilder.setContentTitle(getString(R.string.pobieraniePliku))
                .setProgress(100, progressInfo.progress, false)
                .setContentText(getString(R.string.pobieranieWybranegoPliku))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH);

        notificationBuilder.setOngoing(progressInfo.progress <100);

        return notificationBuilder.build();
    }

    private void prepareNotification(){
        notificationManager = (NotificationManager) getSystemService(NotificationManager.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Downloading file";
            String description = "Notification channel";
            NotificationChannel channel = new NotificationChannel("666", name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

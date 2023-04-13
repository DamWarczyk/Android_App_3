package com.example.android_app_3;

import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class DownloadAsync extends AsyncTask<String, Integer, List<String>> {
    private Integer mRozmiar;
    private String mTyp;

    @Override
    protected List<String> doInBackground(String... strings) {

        HttpsURLConnection polaczenie = null;
        try {
            URL url = new URL(strings[0]);
            polaczenie = (HttpsURLConnection) url.openConnection();
            polaczenie.setRequestMethod("GET");
            this.mRozmiar = polaczenie.getContentLength();
            this.mTyp = polaczenie.getContentType();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (polaczenie != null) polaczenie.disconnect();
        }
        if(mRozmiar != null && mTyp != null)
            return List.of(String.valueOf(mRozmiar), mTyp);
        return List.of("0", "0");
    }

    public DownloadAsync() {
        super();
    }
}

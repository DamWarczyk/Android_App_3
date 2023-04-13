package com.example.android_app_3;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProgressInfo implements Parcelable {
    public int downloadBytes;
    public int size;
    public int progress;
    public String status;


    public ProgressInfo(Parcel in) {
        downloadBytes = in.readInt();
        size = in.readInt();
    }

    public ProgressInfo(){
        this.downloadBytes = 0;
        this.size = 0;
        this.progress = 0;
        this.status = "";
    }

    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }

        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(downloadBytes);
        parcel.writeInt(size);
    }
}

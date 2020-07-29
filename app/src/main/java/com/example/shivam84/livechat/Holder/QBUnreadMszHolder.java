package com.example.shivam84.livechat.Holder;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by shivam84 on 7/12/2017.
 */
public class QBUnreadMszHolder {
    private static QBUnreadMszHolder instance;
    private Bundle bundle;

    public static synchronized QBUnreadMszHolder getInstance(){
        QBUnreadMszHolder qbUnreadMszHolder;
        synchronized (QbChatMessageHolder.class){
            if(instance==null)
                instance=new QBUnreadMszHolder();
            qbUnreadMszHolder=instance;
        }
        return qbUnreadMszHolder;
    }
    private QBUnreadMszHolder(){
        bundle=new Bundle();
    }
    public void setBundle(Bundle bundle){
        this.bundle=bundle;
    }
    public Bundle getBundle(){
        return this.bundle;
    }

    public int getUnreadMszByDialogId(String id){
        return this.bundle.getInt(id);
    }

    //goto chatDialogAdapter
}

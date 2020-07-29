package com.example.shivam84.livechat.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shivam84 on 7/11/2017.
 */
public class QbChatMessageHolder {
    //class to cache mesage
    private static QbChatMessageHolder instance;
    private HashMap<String,ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QbChatMessageHolder getInstance(){
        QbChatMessageHolder qbChatMessageHolder;
        synchronized (QbChatMessageHolder.class){
            if(instance == null)
                instance=new QbChatMessageHolder();
            qbChatMessageHolder=instance;
        }
        return qbChatMessageHolder;

    }

    private QbChatMessageHolder(){
        this.qbChatMessageArray= new HashMap<>();

    }

    public void putMessages(String dialogId,ArrayList<QBChatMessage> qbChatMessages){
        this.qbChatMessageArray.put(dialogId,qbChatMessages);
    }

    public void putMessage(String dialogId,QBChatMessage qbChatMessage){
        List<QBChatMessage> listResult=(List)this.qbChatMessageArray.get(dialogId);
        listResult.add(qbChatMessage);
        ArrayList<QBChatMessage> listAdded=new ArrayList(listResult.size());
        listAdded.addAll(listResult);
        putMessages(dialogId,listAdded);
    }

    public ArrayList<QBChatMessage> getChatMessageByDialogId(String dialogId){
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogId);
    }

//Create ChatMessageAdapter in Adapter package to customize ListChatMesage
}

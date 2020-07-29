package com.example.shivam84.livechat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shivam84.livechat.Holder.QBUserHolder;
import com.example.shivam84.livechat.R;
import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

/**
 * Created by shivam84 on 7/11/2017.
 */
public class ChatMessageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
      return   qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;

        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //here we need to check if send from current user,we just use template list_send_message
            if(qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId()))
            {
                view=inflater.inflate(R.layout.list_send_message,null);
                BubbleTextView bubbleTextView=(BubbleTextView)view.findViewById(R.id.msz_content_list_sendmsz);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
            }
            //use list_recive_msx template

            else{
                view=inflater.inflate(R.layout.list_receive_message,null);
                BubbleTextView bubbleTextView=(BubbleTextView)view.findViewById(R.id.msz_content_list_receivemsz);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
                TextView textName=(TextView)view.findViewById(R.id.text_receive_msz_user);
                textName.setText(QBUserHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getFullName());

            }





        }
        return view;
    }
}

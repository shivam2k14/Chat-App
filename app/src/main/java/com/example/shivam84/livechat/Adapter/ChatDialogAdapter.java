package com.example.shivam84.livechat.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.shivam84.livechat.Holder.QBUnreadMszHolder;
import com.example.shivam84.livechat.R;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;

/**
 * Created by shivam84 on 6/23/2017.
 */

//crete chatDialog Adapter for customize the ListView
public class ChatDialogAdapter extends BaseAdapter {

    TextView textTitle;
    TextView textMessage;
    ImageView imageview,imageView_unread;


    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

//Alt+insert and see constuctor and select both and then enter

    public ChatDialogAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

//right click on BaseAdapter class an see red bulb and click on that and import all methods
    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view =convertView;
        if(view==null){
//LayoutInflator class are used to convert to viewGroup class like Layout to dispaly on View class
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //create the list_chat_dialog layout
          view=inflater.inflate(R.layout.list_chat_dialog,null);


            textTitle=(TextView)view.findViewById(R.id.list_chat_dialog_title);

            textMessage=(TextView)view.findViewById(R.id.list_chat_dialog_message);
            imageview=(ImageView)view.findViewById(R.id.image_list_chat_dialog);
            imageView_unread=(ImageView)view.findViewById(R.id.UnreadMsz_list_chat_dialog);


            //set chat titlle and msz for chat dialog so we can get first charcter form dialog

            textMessage.setText(qbChatDialogs.get(position).getLastMessage());
            textTitle.setText(qbChatDialogs.get(position).getName());


            //Random color From Material Color Gallery



            ColorGenerator generator=ColorGenerator.MATERIAL;
            int randomColor=generator.getRandomColor();

            //build round drawable with library

            TextDrawable.IBuilder builder=TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            //set text and color for drawable
            //get girst character from chatDialog title for create chat dialog image


            TextDrawable drawable=builder.build(textTitle.getText().toString().substring(0,1).toUpperCase(),randomColor);

            //set drawable to imageView

            imageview.setImageDrawable(drawable);

            //Now go to ChatDialogActivity.java

            //set msz unread count


            TextDrawable.IBuilder unreadBuilder=TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            //get unread msz count from cache(QBunreadMSZholder

            int unread_count= QBUnreadMszHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if(unread_count>0){
                TextDrawable unread_drawable=unreadBuilder.build(""+unread_count, Color.RED);
                imageView_unread.setImageDrawable(unread_drawable);

                //now open chatdialogActivity and inside loadchatdialog remove code load adapter
            }

        }
        return view;
    }
}
//now go to list_chat_dialog and create two text view and one imageView
package com.example.shivam84.livechat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.shivam84.livechat.Adapter.ChatMessageAdapter;
import com.example.shivam84.livechat.Common.Common;
import com.example.shivam84.livechat.Holder.QbChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.w3c.dom.Text;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {
    QBChatDialog qbChatDialog;
    ListView listViewChatMsz;
    ImageButton submitButton;
    EditText editTextContent;
    ChatMessageAdapter adapter;

    //update online user
    ImageView img_onlineUserCount;
    TextView txtView_onlineUserCount;



    //override onDestroy and removeListner for QBChatDialog
    //initialize some variable for update/dlt msz
    int comtextMenuIndexIsClicked=-1;
    boolean isEditMode=false;
    QBChatMessage editMsz;

    //register context mrnu inside initviews


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(qbChatDialog.getType()== QBDialogType.GROUP || qbChatDialog.getType()==QBDialogType.PUBLIC_GROUP) {
            getMenuInflater().inflate(R.menu.chat_msz_group, menu);
        }
     



        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat_group_name:
                editGroupName();
                break;
            case R.id.chat_group_addUser:
                addUser();
                break;
            case R.id.chat_group_removeUser:
                removeUser();
                break;

        }

        return true;

    }


    private void removeUser() {
        //similar to adduser just change mode add to remove
        Intent intent=new Intent(this,ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
        //now go to LIstUserActivity and add intent
    }

    private void addUser() {
        //inthis method we will get al user as list who not join chat group
        //open commmon class declare some string
        //we will start listUserAcivity and change only dataSource
        Intent intent=new Intent(this,ListUserActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editGroupName() {
        //we will create alert dialog and editGroupName and after press ok
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.edit_group,null);
        AlertDialog.Builder alertdialogBuilder=new AlertDialog.Builder(this);
        alertdialogBuilder.setView(view);
        final EditText editTextGroupName=(EditText)view.findViewById(R.id.edit_groupName);

        //The dialog msz
        alertdialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //when user press ok we will get froupname from edittext and updated

                        qbChatDialog.setName(editTextGroupName.getText().toString());
                        QBDialogRequestBuilder requestBuilder=new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(ChatMessageActivity.this, "Group name edited..", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //create alertDialog
        AlertDialog alertDialog=alertdialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
       //get index item context menu
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        comtextMenuIndexIsClicked=info.position;
        switch (item.getItemId()){
            case R.id.chat_msz_context_updated:
                updateMsz();
                break;
            case R.id.chat_msz_context_deleted:
                deleteMsz();
                break;
        }
        return true;
    }

    private void deleteMsz() {
        //progressDialog to wait user untill process will done
        final ProgressDialog deleteDialog=new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setMessage("Please wait....");
        deleteDialog.show();

        editMsz=QbChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(comtextMenuIndexIsClicked);
        QBRestChatService.deleteMessage(editMsz.getId(),false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrivewAllMsz();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    private void updateMsz() {
        //use index from contexmenu ,we will get cache of mszs
        //set msz for editText



        editMsz=QbChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId())
                .get(comtextMenuIndexIsClicked);
        editTextContent.setText(editMsz.getBody());
        isEditMode=true; //set editmode true
        //now goto btnSend and enable two mode one new mode and edit mode

    }

    @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_msz_context_menu,menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);



        initViews();
        initChatDialog();
        
        //load all messages of this dialog
        retrivewAllMsz();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditMode) {
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setBody(editTextContent.getText().toString());
                    chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                    chatMessage.setSaveToHistory(true);

                    try {
                        qbChatDialog.sendMessage(chatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }


                    //AGAIN WE SAVED MESSAGE TO CACHE AND REFRESH list view
/*
                QbChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(),chatMessage);
                ArrayList<QBChatMessage> messages=QbChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId());
                adapter=new ChatMessageAdapter(getBaseContext(),messages);
                listViewChatMsz.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                */


                    //fix private chat don't show msz

                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {

                        //save message to cache and refresh listView


                        QbChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                        ArrayList<QBChatMessage> message = QbChatMessageHolder.getInstance().getChatMessageByDialogId(chatMessage.getDialogId());
                        adapter = new ChatMessageAdapter(getBaseContext(), message);
                        listViewChatMsz.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }


                    //remove text from edit text
                    editTextContent.setText("");
                    editTextContent.setFocusable(true);
                }
                else{
                    ////progressDialog to wait user untill process will done
                    final ProgressDialog updateDialog=new ProgressDialog(ChatMessageActivity.this);
                    updateDialog.setMessage("Please wait....");
                    updateDialog.show();


                    //code for edit mode
                    QBMessageUpdateBuilder mszUpdateBuilder=new QBMessageUpdateBuilder();
                    mszUpdateBuilder.updateText(editTextContent.getText().toString()).markDelivered().markRead();
                    QBRestChatService.updateMessage(editMsz.getId(),qbChatDialog.getDialogId(),mszUpdateBuilder)
                            .performAsync(new QBEntityCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid, Bundle bundle) {
                                    //refresh data just reload all msz
                                    retrivewAllMsz();
                                    isEditMode=false; //reset editmode
                                    updateDialog.dismiss();

                                    editTextContent.setText("");
                                    editTextContent.setFocusable(true);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });
                }


            }
        });
    }

    private void retrivewAllMsz() {
        QBMessageGetBuilder messageGetBuilder=new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);

        if(qbChatDialog != null){
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //save list mszs to cache and refresh listVIew
                    QbChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);
                    adapter=new ChatMessageAdapter(getBaseContext(),qbChatMessages);
                    listViewChatMsz.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }

    }

    private void initChatDialog() {
        //parse ChatDIALOg is sent from ListDialogsActivity and initialize
        qbChatDialog=(QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());


        //register listner incoming message to processong Incoming message
        QBIncomingMessagesManager incomingMessanger=QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessanger.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //add code to join group chat
        if(qbChatDialog.getType()== QBDialogType.PUBLIC_GROUP || qbChatDialog.getType()==QBDialogType.GROUP){
            DiscussionHistory discussionHistory=new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d("ERROR",""+e.getMessage());

                }
            });
        }




        qbChatDialog.addMessageListener(this);


        //code for omline user count
        QBChatDialogParticipantListener participantListener=new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if(dialogId==qbChatDialog.getDialogId()){
                    QBRestChatService.getChatDialogById(dialogId)
                            .performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    //get onlineUser
                                    try {
                                        Collection<Integer> onlineUsers=qbChatDialog.getOnlineUsers();
                                        TextDrawable.IBuilder builder=TextDrawable.builder()
                                                .beginConfig()
                                                .withBorder(4)
                                                .endConfig()
                                                .round();
                                        TextDrawable online=builder.build("", Color.BLUE);
                                        img_onlineUserCount.setImageDrawable(online);
                                        txtView_onlineUserCount.setText(String.format("id/id online",onlineUsers.size(),qbChatDialog.getOccupants().size()));

                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onError(QBResponseException e) {

                                }
                            });
                }

            }
        };

        qbChatDialog.addParticipantListener(participantListener);

        //new QBChatDialogMessageListener()
        //{

           // @Override
          // public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
                //save message to cache and refresh listView

                /*
                QbChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
                ArrayList<QBChatMessage> message=QbChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
                adapter=new ChatMessageAdapter(getBaseContext(),message);
                listViewChatMsz.setAdapter(adapter);
                adapter.notifyDataSetChanged(); */
            //}

           // @Override
           // public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
              //  Log.e("ERROR",e.getMessage());

            //}
       // }


    }

    private void initViews() {
        listViewChatMsz=(ListView)findViewById(R.id.listViewChatMsz);
        submitButton=(ImageButton)findViewById(R.id.send_button);
        editTextContent=(EditText)findViewById(R.id.edt_content);
        img_onlineUserCount=(ImageView)findViewById(R.id.countUser_chatMsz);
        txtView_onlineUserCount=(TextView)findViewById(R.id.TxtcountUser_chatMsz);
        //add contextMenu
        registerForContextMenu(listViewChatMsz);

    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {


        //save message to cache and refresh listView


                QbChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
                ArrayList<QBChatMessage> message=QbChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
                adapter=new ChatMessageAdapter(getBaseContext(),message);
                listViewChatMsz.setAdapter(adapter);
                adapter.notifyDataSetChanged();


    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR",""+e.getMessage());

    }
}

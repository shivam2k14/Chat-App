package com.example.shivam84.livechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.shivam84.livechat.Adapter.ChatDialogAdapter;
import com.example.shivam84.livechat.Common.Common;
import com.example.shivam84.livechat.Holder.QBChatDialogHolder;
import com.example.shivam84.livechat.Holder.QBUnreadMszHolder;
import com.example.shivam84.livechat.Holder.QBUserHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatDialogActivity extends AppCompatActivity implements QBSystemMessageListener,QBChatDialogMessageListener{
    Button floatingActionButton;
    ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_dialog_activity);
        //add toolbar
        /*
        Toolbar toolbar=(Toolbar)findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("ANDROID LiveChat App");
        setSupportActionBar(toolbar);*/


        createSessionForChat();


        listView =(ListView)findViewById(R.id.listChatDialog_listView);
        // after create group and private chat we need to for sending messages
        //first create setONItemClickLisnter for listView


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //send chatdialog information to other activity
                QBChatDialog qbChatDialog=(QBChatDialog)listView.getAdapter().getItem(position);
                Intent intent=new Intent(ChatDialogActivity.this,ChatMessageActivity.class);
                //go to common class add global variable
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
                //create list_send_message and list_receive_message.xml file
            }
        });



      loadChatDialog();


        floatingActionButton =(Button)findViewById(R.id.chatDialog_addUser);



//creat listUserActivity
        click();






    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chatDialogMenuUser:
                showUserProfile();
                break;
            default:
                break;
        }
        return true;
    }

    private void showUserProfile() {
        Intent intent=new Intent(ChatDialogActivity.this,UserProfile.class);
        startActivity(intent);

    }

    public void click(){

        //when user click floatingActionButton  we will direct user to select list of friend
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDialogActivity.this,ListUserActivity.class);
                startActivity(intent);


                //now go to ListUserActivity to design that layout


            }
        });
    }

    //OVERRIDE OnResume method and load dialog again ,it will refresh list chat dialog after we creat new dialog


    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialog();
    }


    private void loadChatDialog() {
        QBRequestGetBuilder requestBuilder=new QBRequestGetBuilder();
        requestBuilder.setLimit(500);
        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                //code later first crete chatDialog Adapter for customize the ListView
                //now create adappter and set to listview

                //put ALL dialog to cache
                /*
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                ChatDialogAdapter adapter= new ChatDialogAdapter(getBaseContext(),qbChatDialogs);

              listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                */

                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);
                //set unreadsETTING
                Set<String> setIds=new HashSet<>();
                for (QBChatDialog chatDialog:qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());
                //GET MSZ UNREAD
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMszHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //paste code load adapter here don't forgot save unread msz count cache

                                //save to cache
                                QBUnreadMszHolder.getInstance().setBundle(bundle);
                                //paste code load adapter


                                ChatDialogAdapter adapter= new ChatDialogAdapter(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());

                                listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });


            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());

            }
        });


    }



    private void createSessionForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialogActivity.this);
        mDialog.setMessage("please waiting......");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();


        String user,pass;
        user = getIntent().getStringExtra("user");
        pass = getIntent().getStringExtra("pass");

        //we need add cache user function
        //load all user and save to cache


        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUserHolder.getInstance().putUsers(qbUsers);

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });



        final QBUser qbuser=new QBUser(user,pass);
        QBAuth.createSession(qbuser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbuser.setId(qbSession.getUserId());
                try {
                    qbuser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }
                QBChatService.getInstance().login(qbuser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();

                        //system msz create
                        QBSystemMessagesManager qbSystemMessagesManager=QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogActivity.this);

                        //add incomingmsz lisner
                        QBIncomingMessagesManager qbIncomingMessagesManager=QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogActivity.this);

                    }



                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("error",""+e.getMessage());

                    }
                });


            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("error",""+e.getMessage());


            }
        });


    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //put dialog to cache
        //because we send system msz with content is dialogid so we can get dialog by dialogId

        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //put cache
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource=QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapter adapters=new ChatDialogAdapter(getBaseContext(),adapterSource);
                listView.setAdapter(adapters);
                adapters.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        //
        Log.e("ERROR",""+e.getMessage());
        //now we need to create QBChatDialog holder to cache dialog

    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //add code loadDialog here it will refresh listdialog when we will get msz
        loadChatDialog();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}

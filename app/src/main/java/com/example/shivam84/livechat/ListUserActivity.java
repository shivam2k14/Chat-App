package com.example.shivam84.livechat;

import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shivam84.livechat.Adapter.ListUsersAdapter;
import com.example.shivam84.livechat.Common.Common;
import com.example.shivam84.livechat.Holder.QBUserHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUserActivity extends AppCompatActivity {

    ListView lstView_lstUser;
    Button btn_create_chat;
    String mode="";
    QBChatDialog qbChatDialog;
    List<QBUser> addUser=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        mode=getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog=(QBChatDialog)getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);


        retrieveAllUsers();

        lstView_lstUser = (ListView)findViewById(R.id.listUser_listView);
        lstView_lstUser.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btn_create_chat = (Button)findViewById(R.id.listUser_btn_create_chat);
        btn_create_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            //mode is null then create chat
                if(mode == null) {
                    int countChoice = lstView_lstUser.getCount();
                    //we will check if use click one chat then it will private chat otherwise they will group chay
                    if (lstView_lstUser.getCheckedItemPositions().size() == 1) {
                        createPrivateChat(lstView_lstUser.getCheckedItemPositions());
                    } else if (lstView_lstUser.getCheckedItemPositions().size() > 1) {
                        createGroupChat(lstView_lstUser.getCheckedItemPositions());
                    }
                    //else tell  users select their frirnds

                    else {
                        Toast.makeText(ListUserActivity.this, "Please Select Friend to Chat", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog!=null){
                    //i mode is add just addd user to chat group
                    if(addUser.size()>0){
                        QBDialogRequestBuilder requestBuildwer=new QBDialogRequestBuilder();
                        int cntChoice=lstView_lstUser.getCount();
                        SparseBooleanArray checkedItem=lstView_lstUser.getCheckedItemPositions();
                        for (int i=0;i<cntChoice;i++){
                            if(checkedItem.get(i)){
                                QBUser user=(QBUser)lstView_lstUser.getItemAtPosition(i);
                                requestBuildwer.addUsers(user);
                            }
                        }
                        //call services
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuildwer)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(),"Add User Successfully",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                }
                //if mode is remove we remove user from group
                else if(mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog!=null){
                    if(addUser.size() >0){

                        QBDialogRequestBuilder requestBuildwer=new QBDialogRequestBuilder();
                        int cntChoice=lstView_lstUser.getCount();
                        SparseBooleanArray checkedItem=lstView_lstUser.getCheckedItemPositions();
                        for (int i=0;i<cntChoice;i++){
                            if(checkedItem.get(i)){
                                QBUser user=(QBUser)lstView_lstUser.getItemAtPosition(i);
                                requestBuildwer.removeUsers(user);
                            }
                        }
                        //call services
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuildwer)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(),"Remove User Successfully",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });

                    }
                }


            }
        });

        if(mode==null && qbChatDialog==null)
        retrieveAllUsers();
        else
        {
            if(mode.equals(Common.UPDATE_ADD_MODE))
                loadListAvaibaleUser();
            else if(mode.equals(Common.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }


        //write method to get all users


    }

    private void loadListUserInGroup() {
        //we just show all user in a group
        btn_create_chat.setText("Remove User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer> occupantsId=qbChatDialog.getOccupants();
                        List<QBUser> listUserAllreadyInGroup=QBUserHolder.getInstance().getUserByIds(occupantsId);
                        ArrayList<QBUser> users=new ArrayList<QBUser>();
                        users.addAll(listUserAllreadyInGroup);

                        ListUsersAdapter listUsersAdapter=new ListUsersAdapter(getBaseContext(),users);
                        lstView_lstUser.setAdapter(listUsersAdapter);
                        listUsersAdapter.notifyDataSetChanged();
                        addUser=users;


                        //now just modify btncreatechat

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadListAvaibaleUser() {
        btn_create_chat.setText("ADD USER");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        //refrsh all data from this dialog
                        //get all information of all user
                        ArrayList<QBUser> listAllUser=QBUserHolder.getInstance().getAllUser();
                        //now get all occuptants id from chat dialog
                        List<Integer> occupantsId=qbChatDialog.getOccupants();
                        //get all information of occupants id
                        List<QBUser> listUserAlreadyChatGroup=QBUserHolder.getInstance().getUserByIds(occupantsId);
                        //now we will remove all user available in group from list of all user! we just show user not join group
                        for(QBUser user:listUserAlreadyChatGroup)
                            listAllUser.remove(user);
                        //after set all data in listview
                        if(listAllUser.size() >0)
                        {
                            ListUsersAdapter listUsersAdapter=new ListUsersAdapter(getBaseContext(),listAllUser);
                            lstView_lstUser.setAdapter(listUsersAdapter);
                            listUsersAdapter.notifyDataSetChanged();
                            addUser=listAllUser;
                        }

                        
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {


     final ProgressDialog mDial = new ProgressDialog(ListUserActivity.this);
        mDial.setMessage("Pleae Waiting....");
        mDial.setCanceledOnTouchOutside(false);
        mDial.show();

        int countChoice = lstView_lstUser.getCount();
        ArrayList<Integer> occupantIdsLst = new ArrayList<>();
        //we will loop user,if use has selected then we just build chat dialog with those users

        for (int i = 0; i < countChoice; i++) {
            if (checkedItemPositions.get(i)) {
                QBUser user = (QBUser) lstView_lstUser.getItemAtPosition(i);

                //add user id to array list

                occupantIdsLst.add(user.getId());
            }

        }

        //create chat dialog

        QBChatDialog dialog=new QBChatDialog();
        //dialog.setName();  //we use name later

        //now create Common package and Common Class


        dialog.setName(Common.createChatDialogName(occupantIdsLst));


        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsLst);


        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDial.dismiss();
                Toast.makeText(getBaseContext(),"Create Chat Dialog successfully",Toast.LENGTH_SHORT).show();

                //send system msz to reciept id user
                QBSystemMessagesManager qbSystemMessagesManager=QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage=new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for(int i=0;i<qbChatDialog.getOccupants().size();i++) {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));

                    //each user we send msz

                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }




                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERRROR",e.getMessage());


            }
        });

    }



    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {


        //now just create private chat by  method has been provide from sdk
        //copy pasre progress dialog from group chat


        final ProgressDialog mDial = new ProgressDialog(ListUserActivity.this);
        mDial.setMessage("Pleae Waiting....");
        mDial.setCanceledOnTouchOutside(false);
        mDial.show();



        int countChoice = lstView_lstUser.getCount();

        //we will loop user,if use has selected then we just build chat dialog with those users

        for (int i = 0; i < countChoice; i++) {
            if (checkedItemPositions.get(i)) {
                final QBUser user = (QBUser) lstView_lstUser.getItemAtPosition(i);

                QBChatDialog dialog= DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                        mDial.dismiss();
                        Toast.makeText(getBaseContext(),"Create Private Chat Dialog successfully",Toast.LENGTH_SHORT).show();


                        //send system msz to reciept id user
                        QBSystemMessagesManager qbSystemMessagesManager=QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage=new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        finish();

                    }

                    @Override
                    public void onError(QBResponseException e) {

                        Log.e("ERRROR",e.getMessage());

                    }
                });

            }

        }


    }

    private void retrieveAllUsers() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {


                //just add cache Holder here because at Common.createDialog method her we use data from cache
                QBUserHolder.getInstance().putUsers(qbUsers);



                //create new arraylist to add all users from web Service Without current user LOgged
                ArrayList<QBUser> qbUserWtithoutCurrent = new ArrayList<QBUser>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUserWtithoutCurrent.add(user);
                }

                ListUsersAdapter adapter1 = new ListUsersAdapter(getBaseContext(), qbUserWtithoutCurrent);
                lstView_lstUser.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();


            }

            //create listUsersAdapter to customize the user Show at ListView so go to Adapter and create
            @Override
              public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());

            }
        });


    }

}
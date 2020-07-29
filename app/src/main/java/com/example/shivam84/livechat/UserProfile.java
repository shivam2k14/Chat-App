package com.example.shivam84.livechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shivam84.livechat.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfile extends AppCompatActivity {
    EditText editTextOldPass, editTextNewPass, editTextFullName, editTextEmail, editTextPhone;
    Button btn_update, btn_cancel;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.userUpdate_logout:
                logout();
                break;
            default:
                break;
        }
        return true;
    }

    private void logout() {
        //call qbuser logout method
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                //for full logout lession
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfile.this, "You Are Logged Out!!!", Toast.LENGTH_SHORT).show();
                        //after this call mainActivity and suspended all session
                        Intent intent=new Intent(UserProfile.this,MainActivity.class);
                        //remove all previous activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

      /*  //add toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_update_toolbar);
        toolbar.setTitle("Android LiveChat App");
        setSupportActionBar(toolbar);
        */

        //write method in common class to check string is null or empty

        initViews();

        //now write function to load profile from web service
        loadUserProfile();

        //load userprofile pic


        //just close the activity with this cancel
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //now just update profile with update button

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPass=editTextNewPass.getText().toString();
                String oldPass=editTextOldPass.getText().toString();
                String fullName=editTextFullName.getText().toString();
                String email=editTextEmail.getText().toString();
                String phone=editTextPhone.getText().toString();

                //first set id of QbUser from current user logged
                QBUser user=new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());

                //now check all editText that shold not empty or null and set that property

                if(!Common.isNullOrEmpty(oldPass))
                    user.setOldPassword(oldPass);

                if(!Common.isNullOrEmpty(newPass))
                    user.setPassword(newPass);

                if(!Common.isNullOrEmpty(fullName))
                    user.setFullName(fullName);

                if(!Common.isNullOrEmpty(email))
                    user.setEmail(email);

                if(!Common.isNullOrEmpty(phone))
                    user.setPhone(phone);

                final ProgressDialog mDialog = new ProgressDialog(UserProfile.this);
               mDialog.setMessage("Please Wait...");
                mDialog.show();
                //callUpdateUserMethod For RestChatService
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>()  {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfile.this,"User:"+qbUser.getLogin()+"Updated",Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfile.this,"ERROR:"+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    private void loadUserProfile() {
        QBUser currentUser=QBChatService.getInstance().getUser();

        String fullName=currentUser.getFullName();
        String email=currentUser.getEmail();
        String phone=currentUser.getEmail();

        editTextEmail.setText(email);
        editTextFullName.setText(fullName);
        editTextPhone.setText(phone);
    }

    private void initViews() {
        editTextOldPass = (EditText) findViewById(R.id.update_user_old_password);
        editTextNewPass = (EditText) findViewById(R.id.update_user_New_password);
        editTextFullName = (EditText) findViewById(R.id.update_user_fullName);
        editTextEmail = (EditText) findViewById(R.id.update_user_emailId);
        editTextPhone = (EditText) findViewById(R.id.update_user_Phonenum);

        btn_update = (Button) findViewById(R.id.update_userBtn_update);
        btn_cancel = (Button) findViewById(R.id.update_userBtn_Cancel);

    }
}

package com.example.shivam84.livechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpActivity extends AppCompatActivity {
    Button btnSignUp;
    Button btnCancel;

    EditText edit_user;
    EditText edit_pass,edit_fullName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        registerSession();

        btnSignUp=(Button)findViewById(R.id.signup_btn_Signup);
        btnCancel=(Button)findViewById(R.id.signup_btn_cancel);



        edit_user=(EditText)findViewById(R.id.signup_userName);
        edit_fullName=(EditText)findViewById(R.id.signup_user_fullName);
        edit_pass=(EditText)findViewById(R.id.signup_user_password);



        //This button Just close the activity
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
  //this button get information from edit texts and processing to signup

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user=edit_user.getText().toString();
                String pass=edit_pass.getText().toString();
                QBUser qbUser= new QBUser(user,pass);
                qbUser.setFullName(edit_fullName.getText().toString());

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {

                        //if signup ok we just show toast and close this activity

                        Toast.makeText(getBaseContext(),"SIGN UP SUCCESSFULLY",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                        //else we just show error
                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });


            }
        });

    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());

            }
        });
    }
}

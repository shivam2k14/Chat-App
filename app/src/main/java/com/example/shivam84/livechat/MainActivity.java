package com.example.shivam84.livechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {
    static final String APP_ID = "59403";
    static final String AUTH_KEY = "QKrVBH2ZOcu4cTO";
    static final String AUTH_SECRET = "Y9Ue9N58zyJuQAn";
    static final String ACCOUNT_KEY= "6_fQv2m4_G6PnkKGsjk1";

    Button btn_login,btn_signup;


    EditText edit_user;
    EditText edit_pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializwFrameWork();

        btn_login=(Button)findViewById(R.id.main_btn_login);
        btn_signup=(Button)findViewById(R.id.main_btn_signUp);

        edit_user=(EditText)findViewById(R.id.main_user_login);
        edit_pass=(EditText)findViewById(R.id.main_user_password);

//this button will start new activity for signup

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));

            }
        });

        //this button get information from login and password edit texts and use for login processing

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user=edit_user.getText().toString();
                final String pass=edit_pass.getText().toString();
                QBUser qbUser= new QBUser(user,pass);
                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        //if login ok we just show Toast msz

                        Toast.makeText(getBaseContext(),"LOGIN SUCCESSFUL",Toast.LENGTH_SHORT).show();
                        //now login successfully so this activity go to chatDialogActivity
                        Intent intent = new Intent(MainActivity.this,ChatDialogActivity.class);
                        intent.putExtra("user",user);
                        intent.putExtra("pass",pass);
                        startActivity(intent);

                        finish();  //close login activity after logged

                    }

                    @Override
                    public void onError(QBResponseException e) {

                        //if login is not ok we just show Toast msz
                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });



    }

    private void initializwFrameWork() {
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}

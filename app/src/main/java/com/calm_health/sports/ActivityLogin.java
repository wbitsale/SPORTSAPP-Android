package com.calm_health.sports;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityLogin extends AppCompatActivity {
    Button btn_login;
    LinearLayout lyt_facebook, lyt_google;
    EditText et_email, et_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    public void initView(){
        btn_login = (Button) findViewById(R.id.btn_login);
        lyt_facebook = (LinearLayout) findViewById(R.id.btn_sign_in_fb);
        lyt_google = (LinearLayout) findViewById(R.id.btn_sign_in_google);
        et_email = (EditText) findViewById(R.id.edt_email);
        et_pass = (EditText) findViewById(R.id.edt_password);
//        btn_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String strEmail = et_email.getText().toString();
//                String strPass = et_pass.getText().toString();
//                if(strEmail.length() > 0 && strEmail.length() > 0) {
//                    if (!strEmail.contains("@")) {
//                        Toast.makeText(ActivityLogin.this, "Wrong Email Type.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    else if(!(strPass.length() > 5))
//                    {
//                        Toast.makeText(ActivityLogin.this, "Wrong Password.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    else{
//                        auth(strEmail, strPass);
//                    }
//                }
//                else{
//                    Toast.makeText(ActivityLogin.this, "Please Fill the Gaps.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    public void auth(String strEmail, String strPass)
    {

    }
}

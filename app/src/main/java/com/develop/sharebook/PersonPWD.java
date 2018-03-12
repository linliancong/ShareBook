package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.develop.util.AppManager;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/15.
 */

public class PersonPWD extends StatusBarUtil implements TextWatcher{

    private ImgTxtLayout person_imgtxt_update_pwd;
    private Button person_btn_update_pwd;
    private EditText person_edit_user_pwd;
    private EditText person_edit_newpass_pwd;
    private EditText person_edit_newpass2_pwd;

    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private Context context;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View view;

    private TextView txt;

    private long state=0;


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    if(state==1){
                        //密码更改成功
                        dialog.show();
                        txt= view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("密码修改成功，请返回重新登录。");
                    }
                    else{
                        //更改密码失败
                        dialog.show();
                        txt=view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("密码修改失败，请稍后再试。");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.person_passwd);

        context =PersonPWD.this;
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        //提示信息相关设置
        builder=new AlertDialog.Builder(context);
        inflater=getLayoutInflater();
        view=inflater.inflate(R.layout.ad_pass_erro,null,false);
        builder.setView(view);
        dialog=builder.create();

        person_imgtxt_update_pwd=  findViewById(R.id.person_imgtxt_update_pwd);
        person_btn_update_pwd=  findViewById(R.id.person_btn_update_pwd);
        person_edit_newpass_pwd=findViewById(R.id.person_edit_newpass_pwd);
        person_edit_newpass2_pwd=findViewById(R.id.person_edit_newpass2_pwd);

        person_edit_newpass_pwd.addTextChangedListener(this);
        person_edit_newpass2_pwd.addTextChangedListener(this);


        person_btn_update_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(person_edit_newpass_pwd.getText().toString().equals(person_edit_newpass2_pwd.getText().toString()))
                {
                    if(person_edit_newpass_pwd.getText().toString().equals(sp.getPWD()))
                    {
                        dialog.show();
                        txt=(TextView) view.findViewById(R.id.ad_txt_erro2);
                        txt.setText("新密码和原密码相同，请重新输入。");
                    }
                    //在这里做修改密码的操作
                    else{
                        List<Map<String, String>> data = new ArrayList<>();
                        Map<String, String> map = new HashMap<>();
                        op.insert("update user set passwd=? where id=?", new String[]{person_edit_newpass_pwd.getText().toString(), sp.getID()});
                        //判断是否修改成功
                        data = op.select("select count(1) num from user where passwd=? and id=?", new String[]{person_edit_newpass_pwd.getText().toString(), sp.getID()});
                        if (data.size() != 0) {
                            map = data.get(0);
                            if (map.get("num").toString().equals("1")) {
                                state=1;
                                sp.setPWD("");
                            } else {
                                state=0;
                            }
                        }
                        handler.sendEmptyMessage(0x001);
                    }
                }
                else{
                    dialog.show();
                    txt=view.findViewById(R.id.ad_txt_erro2);
                    txt.setText("两次输入的密码不相同，请重新输入。");
                }

            }
        });

        person_imgtxt_update_pwd.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        view.findViewById(R.id.ad_btn_erro_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==1){
                    Intent intent=new Intent(PersonPWD.this,Login.class);
                    startActivity(intent);
                    finish();
                    AppManager.getAppManager().finishActivity();
                }
                dialog.dismiss();
            }
        });


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.person_passwd;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        person_btn_update_pwd.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(person_edit_newpass_pwd.getText().toString().length()!=0 && person_edit_newpass2_pwd.getText().toString().length()!=0){
            person_btn_update_pwd.setEnabled(true);
        }

    }

}

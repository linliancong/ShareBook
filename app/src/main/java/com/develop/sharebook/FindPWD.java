package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.develop.util.Code;
import com.develop.util.EmailUtil.MailSendInfo;
import com.develop.util.EmailUtil.MailSenderUtils;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/8.
 */

public class FindPWD extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private EditText email;
    private EditText pwd1;
    private EditText pwd2;
    private EditText code;
    private Button find;
    private ImgTxtLayout back;
    private Button sendCode;

    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private Context context;

    //生成的验证码
    private String realCode;

    private String msg="";


    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    Toast.makeText(context,"邮件已发送！请注意查收",Toast.LENGTH_SHORT).show();
                    break;
                case 0x002:
                    Toast.makeText(context,"邮件发送失败！请检查您的邮箱是否正确",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        context=FindPWD.this;
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);
        email=findViewById(R.id.find_email);
        pwd1=findViewById(R.id.find_pwd1);
        pwd2=findViewById(R.id.find_pwd2);
        code=findViewById(R.id.find_code);
        find=findViewById(R.id.find_btn);
        back=findViewById(R.id.find_back);
        sendCode=findViewById(R.id.find_send);
        //创建验证码
        Code.getInstance().createBitmap();
        realCode=Code.getInstance().getCode().toLowerCase();

        find.setOnClickListener(this);
        sendCode.setOnClickListener(this);
        email.addTextChangedListener(this);
        pwd1.addTextChangedListener(this);
        pwd2.addTextChangedListener(this);
        code.addTextChangedListener(this);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.findpwd;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_btn:
                if(!sp.getIsNetworkConnect())
                {
                    Toast.makeText(context,"网络不可用，请检查连接",Toast.LENGTH_SHORT).show();
                }else {
                    //判断邮箱
                    List<Map<String, String>> data = new ArrayList<>();
                    Map<String, String> map = new HashMap<>();
                    data = op.select("select count(1) num from user where email=?", new String[]{email.getText().toString()});
                    if (data.size() != 0) {
                        map = data.get(0);
                        if (map.get("num").toString().equals("1")) {
                            //判断验证码
                            if (realCode.equals(code.getText().toString())) {
                                //判断密码
                                if (pwd1.getText().toString().equals(pwd2.getText().toString())) {
                                    op.insert("update user set passwd=? where email=?", new String[]{pwd1.getText().toString(), email.getText().toString()});
                                    //判断是否修改成功
                                    data = new ArrayList<>();
                                    map = new HashMap<>();
                                    data = op.select("select count(1) num from user where email=? and passwd=?", new String[]{email.getText().toString(), pwd1.getText().toString()});
                                    if (data.size() != 0) {
                                        map = data.get(0);
                                        if (map.get("num").toString().equals("1")) {
                                            Toast.makeText(context, "修改成功！返回重新登录", Toast.LENGTH_SHORT).show();
                                            /*Intent intent = new Intent(context, Login.class);
                                            startActivity(intent);*/
                                            finish();
                                        } else {
                                            Toast.makeText(context, "修改失败！请稍后重试", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "修改失败！请稍后重试", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(context, "两次输入的密码不匹配，请重新输入", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "验证码错误，请重新输入", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "邮箱不存在，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case R.id.find_send:
                //判断邮箱
                List<Map<String, String>> data = new ArrayList<>();
                Map<String, String> map = new HashMap<>();
                data = op.select("select count(1) num from user where email=?", new String[]{email.getText().toString()});
                if (data.size() != 0) {
                    map = data.get(0);
                    if (map.get("num").toString().equals("1")) {
                        msg = "ShareBook服务中心：\n        您正在找回密码，本次的验证码是【" + realCode + "】。" +
                                "\n        请确认是本人操作！如果不是您的操作请忽略本邮件！";
                        ArrayList<String> emails = new ArrayList<>();
                        emails.add(email.getText().toString());
                        sendEmail(emails);
                    }
                    else {
                        Toast.makeText(context, "邮箱不存在，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(context, "邮箱不存在，请重新输入", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        find.setEnabled(false);
        sendCode.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(pwd1.getText().length()>0&&pwd2.getText().length()>0&&email.getText().length()>0&&code.getText().length()>0){
            find.setEnabled(true);
        }
        if(email.getText().length()>0){
            sendCode.setEnabled(true);
        }

    }

    public void sendEmail(final ArrayList<String> email){
        //设置发送邮件的相关信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                MailSendInfo mailinfo=new MailSendInfo();
                mailinfo.setMailServerHost("smtp.qq.com");
                mailinfo.setMailServerPort("587");
                mailinfo.setValidate(true);
                mailinfo.setUserName("1070335034@qq.com");
                mailinfo.setPassword("bglmvthbwgorbbbi");
                mailinfo.setFromAddress("1070335034@qq.com");
                //接收者
                mailinfo.setToAddress(email);
                //标题
                mailinfo.setSubject("ShareBook服务中心");
                mailinfo.setContent(msg);

                //发送邮件
                MailSenderUtils sms=new MailSenderUtils();
                boolean isSuccess=sms.sendTextMail(mailinfo);
                if(isSuccess){
                    handler.sendEmptyMessage(0x001);
                }else {
                    handler.sendEmptyMessage(0x002);
                }

            }
        }).start();
    }
}

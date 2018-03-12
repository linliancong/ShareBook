package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/8.
 */

public class Login extends StatusBarUtil implements View.OnClickListener,TextWatcher{

    private Button login;
    private Button v_login;
    private Button signin;
    private Button losepwd;
    private MaterialEditText user;
    private MaterialEditText passwd;

    private SqlOperator op;
    private Context context;
    private SharedPreferenceUtils sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化控件
        init();

        String name=sp.getUserName();

        //如果程序是第一次运行则初始化数据
        if(sp.getIsFirst()) {
            op.insert("insert into user(username,passwd,name,email) values(?,?,?,?)", new String[]{"test", "1234", "test", "1070335034@qq.com"});
        }else {
            if(sp.getUserName().length()>0){
                user.setText(sp.getUserName());
                passwd.setText(sp.getPWD());
            }
        }

    }

    private void init() {
        context=Login.this;
        op=new SqlOperator(context);
        sp=new SharedPreferenceUtils(context);

        login = findViewById(R.id.btn_login);
        v_login=findViewById(R.id.visitor_login);
        signin= findViewById(R.id.signin);
        losepwd= findViewById(R.id.losepwd);
        user= findViewById(R.id.editText_username);
        passwd= findViewById(R.id.editText_password);

        login.setOnClickListener(this);
        v_login.setOnClickListener(this);
        signin.setOnClickListener(this);
        losepwd.setOnClickListener(this);

        user.addTextChangedListener(this);
        passwd.addTextChangedListener(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.login;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //登录
            case R.id.btn_login:
                if(!sp.getIsNetworkConnect())
                {
                    Toast.makeText(Login.this,"网络不可用，请检查连接",Toast.LENGTH_SHORT).show();
                }else {
                    List<Map<String, String>> data = new ArrayList<>();
                    Map<String, String> map = new HashMap<>();
                    data = op.select("select * from user where username=? and passwd=?", new String[]{user.getText().toString(), passwd.getText().toString()});
                    if (data.size()!=0) {
                        map = data.get(0);
                        if (map.get("userName").toString().equals(user.getText().toString())) {
                            //验证成功
                            Toast.makeText(context, "验证成功！", Toast.LENGTH_SHORT).show();
                            sp.setIsFirst(false);
                            sp.setID(map.get("id"));
                            sp.setUserName(map.get("userName"));
                            sp.setPWD(map.get("passwd"));
                            sp.setName(map.get("name"));
                            sp.setRemark(map.get("remark"));
                            //设置登录标记
                            sp.setIsVisitor(false);
                            Intent it1 = new Intent(context, HomePage.class);
                            startActivity(it1);
                            finish();

                        } else {
                            //验证失败
                            Toast.makeText(context, "用户名或密码有误，请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        //验证失败
                        Toast.makeText(context, "用户名或密码有误，请重新输入！", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            //游客登录
            case R.id.visitor_login:
                //设置登录标记
                sp.setIsVisitor(true);
                Intent it2=new Intent(context,HomePage.class);
                startActivity(it2);
                finish();
                break;
            //注册
            case R.id.signin:
                Intent it3=new Intent(context,Register.class);
                startActivity(it3);
                finish();
                break;
            //找回密码
            case R.id.losepwd:
                Intent it4=new Intent(context,FindPWD.class);
                startActivity(it4);
                finish();
                break;
        }

    }

    /**
     *
    * 这三个方法都是处理文本框输入改变后的操作
    *
    * */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        login.setEnabled(false);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(user.getText().toString().length()!=0&&passwd.getText().toString().length()!=0)
        {
            login.setEnabled(true);
        }
    }
}

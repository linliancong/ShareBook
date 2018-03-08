package com.develop.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/11/24.
 * 报存常用信息工具类
 */

public class SharedPreferenceUtils {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtils(Context context, String file){
        sp=context.getSharedPreferences(file,context.MODE_PRIVATE);
        editor=sp.edit();
    }

    /**
    *
    * 1、用户信息
    *
    * */

    //用户ID
    public void setID(String ID){
        editor.putString("ID",ID);
        editor.commit();
    }

    public String getID()
    {
        return sp.getString("ID","");
    }

    //用户账户
    public void setUserName(String UserName){
        editor.putString("UserName",UserName);
        editor.commit();
    }

    public String getUserName()
    {
        return sp.getString("UserName","");
    }

    //用户名
    public void setName(String Name){
        editor.putString("Name",Name);
        editor.commit();
    }

    public String getName()
    {
        return sp.getString("Name","");
    }

    //用户密码
    public void setPWD(String pwd){
        editor.putString("PWD",pwd);
        editor.commit();
    }

    public String getPWD()
    {
        return sp.getString("PWD","");
    }

    /**
    *
    * 2、登录信息
    *
    * */

    // 是否在后台运行标记
    public void setIsStart(boolean isStart) {
        editor.putBoolean("isStart", isStart);
        editor.commit();
    }

    public boolean getIsStart() {
        return sp.getBoolean("isStart", false);
    }

    // 是否第一次运行本应用
    public void setIsFirst(boolean isFirst) {
        editor.putBoolean("isFirst", isFirst);
        editor.commit();
    }

    public boolean getIsFirst() {
        return sp.getBoolean("isFirst", true);
    }
    // 是否第一次检查更新
    public void setIsFirstUpdate(boolean isFirst) {
        editor.putBoolean("isFirst", isFirst);
        editor.commit();
    }

    public boolean getIsFirstUpdate() {
        return sp.getBoolean("isFirst", true);
    }

    //网络是否连接
    public void setIsNetworkConnect(boolean state){
        editor.putBoolean("IsNetworkConnect",state);
        editor.commit();
    }

    public boolean getIsNetworkConnect(){
        return sp.getBoolean("IsNetworkConnect",false);
    }


}

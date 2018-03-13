package com.develop.util.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/3/8
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    public DBOpenHelper(Context context){
        super(context,"ShareB.db",null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * 表名：user （用户表）
         * 以下为各个字段
         * id：用户编号
         * userName：用户名
         * passwd：密码
         * name：姓名
         * sex：性别
         * IDCard：身份证
         * address：地址
         * phone：手机
         * email：邮箱
         * QQ：qq号
         * pic：头像
         * remark：备注
        * */
        db.execSQL("create table if not exists user"+
                "(id integer primary key autoincrement,"+
                "userName varchar(50),"+
                "passwd varchar(50),"+
                "name varchar(50),"+
                "sex varchar(50),"+
                "IDCard varchar(50),"+
                "address varchar(50),"+
                "phone varchar(50),"+
                "email varchar(50),"+
                "QQ varchar(50),"+
                "pic varchar(50),"+
                "remark varchar(50))");

        /**
         * 表名：bookInfo（图书信息表）
         * 以下为各个字段
         * id：图书编号
         * ISBN: ISBN
         * libraryID：书库编号
         * bookName：图书名
         * author：作者
         * tag：类别
         * CLC：中图法
         * queryName：索书名
         * imgPath：图片路径
         * price：价格
         * num：图书数量
         * summary：简介
         * remark：备注
         * */
        db.execSQL("create table if not exists bookInfo"+
                "(id integer primary key autoincrement,"+
                "ISBN varchar(50),"+
                "libraryID integer,"+
                "bookName varchar(50),"+
                "author varchar(50),"+
                "tag varchar(100),"+
                "CLC varchar(50),"+
                "queryName varchar(50),"+
                "imgPath varchar(100),"+
                "price varchar(50),"+
                "num varchar(50),"+
                "summary varchar(1000),"+
                "remark varchar(500))");

        /**
         * 表名：bookBorrow（图书借阅信息）
         * 以下为各个字段
         * id：借阅编号
         * bookID：图书编号
         * userID：用户编号
         * state：借阅状态（1、预约中，2、借阅中）
         * date：借阅时间
         * remark：备注
         * */
        db.execSQL("create table if not exists bookBorrow"+
                "(id integer primary key autoincrement,"+
                "bookID integer,"+
                "userID integer,"+
                "state integer,"+
                "date varchar(50),"+
                "remark varchar(50))");

        /**
         * 表名：library（书库表）
         * 以下为各个字段
         * id：书库编号
         * userID：用户编号
         * name：书库名
         * remark：备注
         * */
        db.execSQL("create table if not exists library"+
                "(id integer primary key autoincrement,"+
                "userID integer,"+
                "name varchar(50),"+
                "remark varchar(50))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //当版本号发生改变时调用该方法,这里删除数据表,在实际业务中一般是要进行数据备份的
        db.execSQL("drop table user");
        db.execSQL("drop table bookInfo");
        db.execSQL("drop table bookBorrow");
        onCreate(db);
    }
}

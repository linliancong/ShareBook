package com.develop.util.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2017/3/8.
 * 该类是一个业务bean类,完成数据库的相关操作
 */
public class SqlOperator {
    //声明数据库管理器
    private DBOpenHelper openHelper;

    //在构造方法中根据上下文对象实例化数据库管理器
    public SqlOperator(Context context) {
        openHelper = new DBOpenHelper(context);
    }

    /**
     * 获得指定URI的每条线程已经下载的文件长度
     * @param sql 查询语句
     * @param strs 条件
     * @return
     * */
    public List<Map<String,String>> select(String sql, String[] strs)
    {
        //获得可读数据库句柄,通常内部实现返回的其实都是可写的数据库句柄
        SQLiteDatabase db = openHelper.getReadableDatabase();
        //查询数据,返回的Cursor指向第一条记录之前
        Cursor cursor = db.rawQuery(sql,strs);
        //所有数据集合
        List<Map<String,String>> datas=new ArrayList<>();

        //开始遍历数据
        while(cursor.moveToNext())
        {
            //一条数据的集合
            Map<String, String> data = new HashMap<>();
            for (int i=0;i<cursor.getColumnCount();i++){
                String coulumnName=cursor.getColumnName(i);
                String value=cursor.getString(cursor.getColumnIndex(coulumnName));
                data.put(coulumnName,value);
            }
            datas.add(data);
        }
        cursor.close();//关闭cursor,释放资源;
        db.close();
        return datas;
    }

    /**
     * 插入数据
     * @param sql 操作语句
     * @param strs 条件
     */
    public void insert(String sql, String[] strs)
    {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        //开启事务,因为此处需要插入多条数据
        db.beginTransaction();
        try{
            //参数依次是：表名，强行插入null值得数据列的列名，一行记录的数据
            //ContentValues values = new ContentValues();
            //values.put("name", "呵呵~");
            //db.execSQL("table", null, values);
            db.execSQL(sql, strs);
            //设置一个事务成功的标志,如果成功就提交事务,如果没调用该方法的话那么事务回滚
            //就是上面的数据库操作撤销
            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            //结束一个事务
            db.endTransaction();
        }
        db.close();
    }

    /**
     * 更新指定数据
     * @param sql 操作语句
     * @param strs 条件
     *
     */
    public void update(String sql, String[] strs)
    {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        //参数依次是表名，修改后的值，where条件，以及约束，如果不指定三四两个参数，会更改所有行
        //ContentValues values = new ContentValues();
        //values.put("name", "呵呵~");
        //db.update("table", values, "id = ?", strs);
        db.execSQL(sql, strs);
        db.close();
    }


    /**
     * 删除指定数据
     * @param sql 操作语句
     * @param strs 条件
     */
    public void delete(String sql, String[] strs)
    {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        //参数依次是表名，以及where条件与约束
        //ContentValues values = new ContentValues();
        //values.put("name", "呵呵~");
        //db.delete("table", "id = ?", strs);
        db.execSQL(sql, strs);
        db.close();
    }

}

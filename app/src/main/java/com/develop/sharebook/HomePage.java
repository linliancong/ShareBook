package com.develop.sharebook;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.develop.util.StatusBarUtil;
import com.nineoldandroids.view.ViewHelper;

import java.util.zip.Inflater;

/**
 * Created by Administrator on 2018/3/8.
 */

public class HomePage extends StatusBarUtil implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TextView user;
    private ImageView img;

    private Context context;
    private double mTime=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();


    }

    private void init() {
        context=HomePage.this;
        //创建toolbar
        toolbar=findViewById(R.id.hp_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //获得抽屉布局
        drawer=findViewById(R.id.hp_drawer);
        //在布局文件中声明了DrawerLayout后，即可滑出布局了
        //ActionBarDrawerToggle的作用是在toolbar上创建一个点击弹出drawer的按钮而已
        toggle=new ActionBarDrawerToggle(this,drawer,toolbar,0,0);
        drawer.addDrawerListener(toggle);
        //显示按钮
        toggle.syncState();

        navigationView=findViewById(R.id.hp_navview);
        navigationView.setNavigationItemSelectedListener(this);

        //获取NavigationView 的headerLayout
        View inflater=navigationView.inflateHeaderView(R.layout.hp_left);

        //设置头像和名字
        user=inflater.findViewById(R.id.left_name);
        img=inflater.findViewById(R.id.left_img);
        //user.setText("Test");
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.homepage;
    }

    //侧边菜单的点击事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.dynamic:
                Toast.makeText(context,"动态~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.message:
                Toast.makeText(context,"消息~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.library:
                Toast.makeText(context,"书库~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.stat:
                Toast.makeText(context,"图书统计~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                Toast.makeText(context,"关于我们~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.exit:
                Toast.makeText(context,"退出登录~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.left_img:
                Toast.makeText(context,"头像~",Toast.LENGTH_SHORT).show();
                break;
            case R.id.left_name:
                Toast.makeText(context,"姓名~",Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            if(System.currentTimeMillis()-mTime>2000)
            {
                Toast.makeText(getApplicationContext(),"再按一次退出",Toast.LENGTH_SHORT).show();
                mTime=System.currentTimeMillis();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    /*//菜单按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hp_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }*/
}

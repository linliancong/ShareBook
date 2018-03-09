package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.develop.util.MyFragmentPagerAdapter;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2018/3/8.
 */

public class HomePage extends StatusBarUtil implements NavigationView.OnNavigationItemSelectedListener,ViewPager.OnPageChangeListener,View.OnClickListener{

    public static final int PAG_ONE=0;
    public static final int PAG_TWO=1;
    public static final int PAG_THREE=2;
    public static final int PAG_FOUR=3;
    public static final int PAG_FIVE=4;
    public static final int PAG_SIX=5;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private TextView user;
    private ImageView img;
    private LinearLayout person;

    private Context context;
    private double mTime=0;
    private SharedPreferenceUtils sp;


    private ViewPager vpager;
    private TextView title;
    private ImageView scan;
    private MyFragmentPagerAdapter mAdapter=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();


    }

    private void init() {
        context=HomePage.this;
        sp=new SharedPreferenceUtils(context);
        //设置页面变化
        vpager=findViewById(R.id.hp_vpager);
        title=findViewById(R.id.hp_title);
        scan=findViewById(R.id.hp_scan);
        mAdapter=new MyFragmentPagerAdapter(getSupportFragmentManager(),context);
        vpager.setAdapter(mAdapter);
        vpager.addOnPageChangeListener(this);
        //默认显示动态页面
        vpager.setCurrentItem(PAG_TWO);
        title.setText("动态");
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

        scan.setOnClickListener(this);

        //获取NavigationView 的headerLayout
        View inflater=navigationView.inflateHeaderView(R.layout.hp_left);
        user = inflater.findViewById(R.id.left_name);
        img = inflater.findViewById(R.id.left_img);
        person=inflater.findViewById(R.id.person);

        //如果是游客登录则显示默认头像和名字
        if(!sp.getIsVisitor()) {
            //设置头像和名字
            user.setText(sp.getName());
            /*img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image*//*");
                    startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                }
            });*/
            person.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到个人信息页面
                    vpager.setCurrentItem(PAG_ONE);
                    title.setText("个人资料");
                    //收起侧边栏
                    drawer.closeDrawer(GravityCompat.START);
                }
            });
            readImage();
        }else {
            user.setText("游客~");
        }
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
                //动态~
                /*Intent it1=new Intent(context,Login.class);
                startActivity(it1);*/
                vpager.setCurrentItem(PAG_TWO);
                title.setText("动态");
                break;
            case R.id.message:
                //消息~
                /*Intent it2=new Intent(context,Login.class);
                startActivity(it2);*/
                vpager.setCurrentItem(PAG_THREE);
                title.setText("消息");
                break;
            case R.id.library:
                //书库~
                /*Intent it3=new Intent(context,Login.class);
                startActivity(it3);*/
                vpager.setCurrentItem(PAG_FOUR);
                title.setText("书库");
                break;
            case R.id.stat:
                //图书统计~
                /*Intent it4=new Intent(context,Login.class);
                startActivity(it4);*/
                vpager.setCurrentItem(PAG_FIVE);
                title.setText("图书统计");
                break;
            case R.id.about:
                //关于我们~
                /*Intent it5=new Intent(context,Login.class);
                startActivity(it5);*/
                vpager.setCurrentItem(PAG_SIX);
                title.setText("关于我们");
                break;
            case R.id.exit:
                //退出登录~
                Intent it6=new Intent(context,Login.class);
                startActivity(it6);
                finish();
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

    /**
     * 读取图片
     * */
    private boolean readImage() {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            filesDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            filesDir = context.getFilesDir();
        }
        File file = new File(filesDir,sp.getID()+".png");
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            img.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //state状态有3个，0：什么都没做，1：正在滑动，2：滑动完毕
        // 由于ViewPager 放在 RadioButton 后，所以RadioButton 的点击事件会失效。

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hp_scan:
                //这里是扫码之后的操作
                Toast.makeText(getApplicationContext(),"扫一扫",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

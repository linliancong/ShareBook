package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.develop.entity.BookInfo;
import com.develop.util.AppManager;
import com.develop.util.CheckPermissionsActivity;
import com.develop.util.MyFragmentPagerAdapter;
import com.develop.util.MyViewPager;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;
import com.develop.zxing.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/8.
 */

public class HomePage extends CheckPermissionsActivity implements NavigationView.OnNavigationItemSelectedListener,
        ViewPager.OnPageChangeListener,View.OnClickListener,PersonInfo.ShowAct{

    public final static int SCANNING_REQUEST_CODE = 1;
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
    private TextView remark;
    private ImageView img;
    private RelativeLayout person;

    private Context context;
    private double mTime=0;
    private SharedPreferenceUtils sp;
    private SqlOperator op;


    private MyViewPager vpager;
    private TextView title;
    private ImageView scan;
    private MyFragmentPagerAdapter mAdapter=null;

    private BookInfo bookInfo;
    private GetBookInfo getBookInfo;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    user.setText(sp.getName());
                    remark.setText(sp.getRemark());
                    readImage();
                    break;
                case 0x002:
                    //扫描插入数据
                    Intent it=new Intent(context,BookInfoView.class);
                    Bundle bd=new Bundle();
                    ArrayList<BookInfo> book=new ArrayList<>();
                    book.add(bookInfo);
                    bd.putSerializable("book",book);
                    bd.putInt("tag",1);
                    it.putExtras(bd);
                    startActivity(it);
                    Toast.makeText(getApplicationContext(),"获取书本数据成功",Toast.LENGTH_SHORT).show();
                    break;
                case 0x003:
                    //手动插入数据
                    Intent it2=new Intent(context,BookInfoView.class);
                    Bundle bd2=new Bundle();
                    ArrayList<BookInfo> book2=new ArrayList<>();
                    book2.add(bookInfo);
                    bd2.putSerializable("book",book2);
                    bd2.putInt("tag",1);
                    it2.putExtras(bd2);
                    startActivity(it2);
                    Toast.makeText(getApplicationContext(),"获取书本数据失败，请手动输入",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        //页面管理
        AppManager.getAppManager().addActivity(HomePage.this);

        init();

        initStatusBar();

        //测试用
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getBookInfo = new GetBookInfo(context);
                    bookInfo = getBookInfo.fetchBookInfoByXML("97871111280691");//"9787111128069"
                    if(bookInfo==null){
                        bookInfo=new BookInfo();
                    }
                    bookInfo.setIsbn13("97871111280691");

                } catch (Exception e) {
                }
            }
        }).start();*/



    }

    private void init() {
        context=HomePage.this;
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);
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
        remark = inflater.findViewById(R.id.left_remark);
        img = inflater.findViewById(R.id.left_img);
        person=inflater.findViewById(R.id.person);

        bookInfo=new BookInfo();

        //如果是游客登录则显示默认头像和名字
        if(!sp.getIsVisitor()) {
            //设置头像和名字
            user.setText(sp.getName());
            remark.setText(sp.getRemark());

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
            remark.setText("您还没有登录~");
        }

        //检查是否更新了头像、姓名、个性签名
        new Thread(){
            @Override
            public void run() {
                while (true)
                {
                    if(sp.getIsUpdate()) {
                        sp.setIsUpdate(false);
                        handler.sendEmptyMessage(0x001);
                    }
                }
            }
        }.start();
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
                if(!sp.getIsVisitor()) {
                /*Intent it2=new Intent(context,Login.class);
                startActivity(it2);*/
                    vpager.setCurrentItem(PAG_THREE);
                    title.setText("消息");
                }else {
                    Toast.makeText(getApplicationContext(),"你还没注册，请先注册",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.library:
                //书库~
                if(!sp.getIsVisitor()) {
                /*Intent it3=new Intent(context,Login.class);
                startActivity(it3);*/
                    vpager.setCurrentItem(PAG_FOUR);
                    title.setText("书库");
                }
                else {
                    Toast.makeText(getApplicationContext(),"你还没注册，请先注册",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stat:
                //图书统计~
                if(!sp.getIsVisitor()) {
                /*Intent it4=new Intent(context,Login.class);
                startActivity(it4);*/
                    vpager.setCurrentItem(PAG_FIVE);
                    title.setText("图书统计");
                }else {
                    Toast.makeText(getApplicationContext(), "你还没注册，请先注册", Toast.LENGTH_SHORT).show();
                }
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
                //Toast.makeText(getApplicationContext(),"扫一扫",Toast.LENGTH_SHORT).show();
                if(!sp.getIsVisitor()){
                Intent intent=new Intent(context, CaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,SCANNING_REQUEST_CODE);
                }else {
                    Toast.makeText(getApplicationContext(),"你还没注册，请先注册",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCANNING_REQUEST_CODE:
                if(resultCode==RESULT_OK){
                    final Bundle bd=data.getExtras();
                    Handler hd=new Handler(Looper.getMainLooper());
                    hd.post(new Runnable() {
                        @Override
                        public void run() {
                            //这里获取到扫描的数据
                            //String path="https://api.douban.com/v2/book/isbn/"+bd.getString("result");
                            //bookInfo=GetBookInfo.loading(path);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                        try {
                                            getBookInfo = new GetBookInfo(context);
                                            bookInfo = getBookInfo.fetchBookInfoByJSON(bd.getString("result"));//"9787111128069"
                                            if(bookInfo==null){
                                                bookInfo=new BookInfo();
                                            }
                                            bookInfo.setIsbn13(bd.getString("result"));

                                        } catch (Exception e) {
                                        }
                                }
                            }).start();
                            //Toast.makeText(getApplicationContext(),bd.getString("result"),Toast.LENGTH_SHORT).show();
                            //Log.i("aaaa:",bookInfo.getTitle());

                        }
                    });
                }
                break;
        }
    }

    @Override
    public void callBack(int result) {
        switch (result){
            case 0x003:
                Intent it1=new Intent(context,PersonPWD.class);
                startActivity(it1);
                break;
        }
    }


    private void initStatusBar() {
        Window win = getWindow();
        ViewGroup contentFrameLayout = findViewById(Window.ID_ANDROID_CONTENT);
        View parentView = contentFrameLayout.getChildAt(0);
        //KITKAT也能满足，只是SYSTEM_UI_FLAG_LIGHT_STATUS_BAR（状态栏字体颜色反转）只有在6.0才有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parentView.setFitsSystemWindows(true);
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(Color.WHITE);// SDK21
        }
    }


    // 通过豆瓣获取图书信息
    public class GetBookInfo {


        public static final String ISBN_URL = "https://api.douban.com/v2/book/isbn/:"; // 返回来的是JSON的编码信息
        public static final int RETURN_BOOKINFO_STATUS = 200;	//返回图书信息
        public static final int BOOK_NOT_FOUND_STATUS = 404;	//图书不存在
        private Context context;

        private File file;


        public GetBookInfo(Context context){
            this.context=context;
        }
        /**
         * 从根据isbn号从豆瓣获取数据
         *
         * @param
         * @return
         * @throws IOException
         */
        public BookInfo fetchBookInfoByJSON(String isbnNo) throws IOException {
            String requestUrl = ISBN_URL + isbnNo;
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            if(conn.getResponseCode() == RETURN_BOOKINFO_STATUS) {
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                return readBookInfo(sb.toString());
            }
            if(conn.getResponseCode() == BOOK_NOT_FOUND_STATUS) {
                handler.sendEmptyMessage(0x003);
            }

            return null;
        }

        // 读取获得的数据
        public  BookInfo readBookInfo(String jsonStr) {
            JSONObject jsonObject;
            BookInfo bookInfo = new BookInfo();

            try {
                jsonObject = new JSONObject(jsonStr);
                bookInfo.setTitle(jsonObject.getString("title"));
                bookInfo.setAuthor(parseJSONArraytoString(jsonObject.getJSONArray("author")));
                bookInfo.setTags(parseJSONArraytoString2(jsonObject.getJSONArray("tags")));
                bookInfo.setPublisher(jsonObject.getString("publisher"));

                DownloadBitmap(jsonObject.getString("image"));
                saveImage(DownloadBitmap(jsonObject.getString("image")),jsonObject.getString("isbn13"));
                bookInfo.setImagePath(file.toString());

                bookInfo.setPrice(jsonObject.getString("price"));
                bookInfo.setSummary(jsonObject.getString("summary"));
                //bookInfo.setIsbn13(jsonObject.getString("isbn13"));
            } catch (Exception e) {
                handler.sendEmptyMessage(0x003);
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0x002);
            return bookInfo;
        }

        /**
         * 下载图片
         */
        public Bitmap DownloadBitmap(String bitmapUrl) throws Exception{
            Bitmap bitmap = null;
            BufferedInputStream bis = null;

            URL url = new URL(bitmapUrl);
            URLConnection conn = url.openConnection();
            bis = new BufferedInputStream(conn.getInputStream());
            bitmap = BitmapFactory.decodeStream(bis);

            if (bis != null)
                bis.close();

            return bitmap;
        }

        public String parseJSONArraytoString(JSONArray array) throws Exception{
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < array.length(); i++) {
                str = str.append(array.getString(i)).append(";");
            }
            return str.toString();
        }

        public String parseJSONArraytoString2(JSONArray array) throws Exception {
            StringBuffer str = new StringBuffer();

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = (JSONObject) array.get(i);
                /*for (int j = 0; j < jsonObject.length(); j++) {*/
                    str = str.append(jsonObject.getString("name")).append(";");
                //}
            }
            return str.toString();
        }

        /**
         * 保存图片
         */
        private void saveImage(Bitmap bitmap,String isbnNo) throws Exception{
            File filesDir;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
                //路径1：storage/sdcard/Android/data/包名/files
                filesDir = context.getExternalFilesDir("");
            }else{//手机内部存储
                //路径2：data/data/包名/files
                filesDir = context.getFilesDir();
            }
            FileOutputStream fos = null;
            file = new File(filesDir,isbnNo+".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos);
            if(fos != null){
                fos.close();
            }
        }

    }

}

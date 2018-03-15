package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.entity.BookInfo;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import org.w3c.dom.Text;

import java.io.File;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/14.
 */

public class DynamicInfo extends StatusBarUtil implements View.OnClickListener {

    private ImgTxtLayout back;
    private Button more;
    private ImageView img;
    private TextView name;
    private TextView author;
    private TextView publisher;
    private TextView price;
    private TextView isbn;
    private TextView summary;
    private TextView info_state;
    private Intent intent;

    private RelativeLayout visible;
    private TextView save;
    private TextView sub;

    private Context context;
    private ArrayList<BookInfo> book;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private int state=0;
    private String libs;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        getData();

    }

    private void init() {
        context=getApplicationContext();
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);
        back=findViewById(R.id.dynamic_imgtxt_back);
        more=findViewById(R.id.dynamic_info_more);
        img=findViewById(R.id.dynamic_info_img);
        name=findViewById(R.id.dynamic_info_name);
        author=findViewById(R.id.dynamic_info_author);
        publisher=findViewById(R.id.dynamic_info_publisher);
        price=findViewById(R.id.dynamic_info_price);
        isbn=findViewById(R.id.dynamic_info_isbn);
        summary=findViewById(R.id.dynamic_info_summary);
        info_state=findViewById(R.id.dynamic_info_state);

        visible=findViewById(R.id.dynamic_info_ly_more);
        save=findViewById(R.id.dynamic_info_save);
        sub=findViewById(R.id.dynamic_info_sub);

        visible.setOnClickListener(this);
        more.setOnClickListener(this);
        save.setOnClickListener(this);
        sub.setOnClickListener(this);


        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("com.develop.sharebook.MYBROADCAST2"));
                finish();
            }
        });
    }

    private void getData() {
        intent=getIntent();
        Bundle bd=intent.getExtras();
        book=new ArrayList<>();
        book= (ArrayList<BookInfo>) bd.getSerializable("book");
        img.setImageBitmap(readImage(book.get(0).getImagePath()));
        name.setText(book.get(0).getTitle());
        author.setText(book.get(0).getAuthor());
        publisher.setText(book.get(0).getPublisher());
        price.setText(book.get(0).getPrice());
        isbn.setText(book.get(0).getIsbn13());
        summary.setText(book.get(0).getSummary());

        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        //判断图书状态，1：已预约，2：借阅中
        data = op.select("select * from bookBorrow where userID=? and ISBN=?", new String[]{sp.getID(),book.get(0).getIsbn13()});
        if (data.size() != 0) {
            info_state.setVisibility(View.VISIBLE);
            map = data.get(0);
            if(map.get("state").equals("1")){
                //已预约
                state=1;
            }
            if(map.get("state").equals("2")){
                //借阅中
                state=2;
            }
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dynamic_info;
    }

    /**
     * 读取图片
     * */
    private Bitmap readImage(String path) {
        File file = new File(path);
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            //img.setImageBitmap(bitmap);
            return bitmap;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dynamic_info_more:
                visible.setVisibility(View.VISIBLE);
                break;
            case R.id.dynamic_info_save:
                visible.setVisibility(View.GONE);
                if(!sp.getIsVisitor()) {
                    Intent it = new Intent(context, BookInfoView.class);
                    Bundle bd = new Bundle();
                    bd.putSerializable("book", book);
                    bd.putInt("tag", 1);
                    it.putExtras(bd);
                    startActivity(it);
                }else {
                    Toast.makeText(getApplicationContext(),"你还没注册，请先注册",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.dynamic_info_sub:
                visible.setVisibility(View.GONE);
                if(!sp.getIsVisitor()) {
                    if (state == 0) {
                        //获取书库信息
                        List<Map<String, String>> data = new ArrayList<>();
                        data = op.select("select * from library where userID=?", new String[]{sp.getID()});
                        if (data.size() != 0) {
                            libs = data.get(0).get("id");
                            update();
                        } else {
                            Toast.makeText(context, "书库不存在，请先创建书库", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "图书已经预约，无需重复操作", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"你还没注册，请先注册",Toast.LENGTH_SHORT).show();
                }
            case R.id.dynamic_info_ly_more:
                visible.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 保存图书
     * */
    private void update() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();

        Date newTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(newTime);
        int tag=1;
        //判断图书是否存在，不存在：插入
        data = op.select("select * from bookInfo where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() == 0) {
            op.insert("insert into bookInfo(ISBN,libraryID,bookName,author,publisher,tag,imgPath,price,summary) values(?,?,?,?,?,?,?,?,?)",
                    new String[]{book.get(0).getIsbn13(), libs, book.get(0).getTitle(), book.get(0).getAuthor(), book.get(0).getPublisher(),
                            "编程", book.get(0).getImagePath(), book.get(0).getPrice(), book.get(0).getSummary()});
            //判断是否插入成功
            data = op.select("select count(1) num from bookInfo where ISBN=?", new String[]{book.get(0).getIsbn13()});
            if (data.size() != 0) {
                map = data.get(0);
                if (map.get("num").toString().equals("1")) {
                    op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                            new String[]{sp.getID(), "保存书本","您已经成功保存了《"+book.get(0).getTitle()+"》。", "1",date});
                    tag=1;
                } else {
                    op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                            new String[]{sp.getID(), "保存书本","您对于《"+book.get(0).getTitle()+"》保存失败！", "1",date});
                    tag=0;
                }
            }
        }

        if(tag==1) {
            op.insert("insert into bookBorrow(userID,ISBN,state,date) values(?,?,?,?)", new String[]{sp.getID(), book.get(0).getIsbn13(), "1",date});
            data = op.select("select count(1) num from bookBorrow where ISBN=?", new String[]{book.get(0).getIsbn13()});
            if (data.size() != 0) {
                map = data.get(0);
                if (map.get("num").toString().equals("1")) {
                    op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                            new String[]{sp.getID(), "预约书本","您已经成功预约了《"+book.get(0).getTitle()+"》。", "1",date});
                    Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show();
                    info_state.setVisibility(View.VISIBLE);
                }
            } else {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "预约书本","您预约的《"+book.get(0).getTitle()+"》，预约失败！", "1",date});
                Toast.makeText(context, "预约失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }else {
            op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                    new String[]{sp.getID(), "预约书本","您预约的《"+book.get(0).getTitle()+"》预约失败！该书本不存在。", "1",date});
            Toast.makeText(context, "预约失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }
}

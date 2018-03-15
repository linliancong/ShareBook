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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/14.
 */

public class StateAll extends StatusBarUtil implements View.OnClickListener {

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
    private TextView delete;
    private TextView cancel;
    private TextView update;
    private TextView sub;
    private TextView share;

    private Context context;
    private ArrayList<BookInfo> book;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private int state=0;
    private int tag=0;
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
        back=findViewById(R.id.state_all_imgtxt_back);
        more=findViewById(R.id.state_all_info_more);
        img=findViewById(R.id.state_all_info_img);
        name=findViewById(R.id.state_all_info_name);
        author=findViewById(R.id.state_all_info_author);
        publisher=findViewById(R.id.state_all_info_publisher);
        price=findViewById(R.id.state_all_info_price);
        isbn=findViewById(R.id.state_all_info_isbn);
        summary=findViewById(R.id.state_all_info_summary);
        info_state=findViewById(R.id.state_all_info_state);

        visible=findViewById(R.id.state_all_info_ly_more);
        delete =findViewById(R.id.state_all_info_save);
        sub =findViewById(R.id.state_all_info_sub);
        cancel=findViewById(R.id.state_all_info_cancel);
        update=findViewById(R.id.state_all_info_update);
        share=findViewById(R.id.state_all_info_share);

        visible.setOnClickListener(this);
        more.setOnClickListener(this);
        delete.setOnClickListener(this);
        cancel.setOnClickListener(this);
        update.setOnClickListener(this);
        sub.setOnClickListener(this);
        share.setOnClickListener(this);


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
        if(readImage(book.get(0).getImagePath())!=null) {
            img.setImageBitmap(readImage(book.get(0).getImagePath()));
        }
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
                info_state.setText("已预约");
            }
            if(map.get("state").equals("2")){
                //借阅中
                state=2;
                info_state.setText("借阅中");
            }
        }

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.state_all;
    }

    /**
     * 读取图片
     * */
    private Bitmap readImage(String path) {
        if(path!=null) {
            File file = new File(path);
            if (file.exists()) {
                //存储--->内存
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                //img.setImageBitmap(bitmap);
                return bitmap;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.state_all_info_more:
                visible.setVisibility(View.VISIBLE);
                break;
            case R.id.state_all_info_save:
                visible.setVisibility(View.GONE);
                delete();
                if (state!=0) {
                    deleteSub();
                }
                if (tag==1){
                    finish();
                }
                break;
            case R.id.state_all_info_cancel:
                visible.setVisibility(View.GONE);
                if (state!=0) {
                    deleteSub();
                }else {
                    Toast.makeText(context, "该书本没有预约", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.state_all_info_sub:
                visible.setVisibility(View.GONE);
                if (state==0) {
                    sub();
                }else {
                    Toast.makeText(context, "图书已经预约，无需重复操作", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.state_all_info_update:
                visible.setVisibility(View.GONE);
                Intent it=new Intent(context,BookInfoView.class);
                Bundle bd=new Bundle();
                bd.putSerializable("book",book);
                bd.putInt("tag",2);
                it.putExtras(bd);
                startActivityForResult(it,0x001);
                break;
            case R.id.state_all_info_ly_more:
                visible.setVisibility(View.GONE);
                break;
            case R.id.state_all_info_share:
                Toast.makeText(context,"分享到朋友圈成功！",Toast.LENGTH_SHORT).show();
                visible.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 删除图书
     * */
    private void delete() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();

        Date newTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(newTime);
        op.insert("delete from bookInfo where ISBN=?", new String[]{book.get(0).getIsbn13()});
        //判断是否删除成功
        data = op.select("select count(1) num from bookInfo where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() != 0) {
            map = data.get(0);
            if (map.get("num").toString().equals("0")) {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "删除书本","您已经成功删除了《"+book.get(0).getTitle()+"》。", "1",date});
                Toast.makeText(context, "删除书本成功", Toast.LENGTH_SHORT).show();
                tag=1;
            } else {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "删除书本","您对于《"+book.get(0).getTitle()+"》删除失败！", "1",date});
                Toast.makeText(context, "删除书本失败", Toast.LENGTH_SHORT).show();
                tag=0;
            }
        }

    }

    /**
     * 删除预约
     * */
    private void deleteSub() {
        List<Map<String, String>> data;
        Map<String, String> map;

        Date newTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(newTime);

        op.insert("delete from bookBorrow where ISBN=?", new String[]{ book.get(0).getIsbn13()});
        data = op.select("select count(1) num from bookBorrow where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() != 0) {
            map = data.get(0);
            if (map.get("num").toString().equals("0")) {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "删除预约","您已经成功删除了《"+book.get(0).getTitle()+"》的预约。", "1",date});
                Toast.makeText(context, "删除预约成功", Toast.LENGTH_SHORT).show();
                state=0;
                info_state.setVisibility(View.GONE);
            }
        } else {
            op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                    new String[]{sp.getID(), "删除预约","您预约的《"+book.get(0).getTitle()+"》，删除预约失败！", "1",date});
            Toast.makeText(context, "删除预约失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 预约
     * */
    public void sub(){
        List<Map<String, String>> data;
        Map<String, String> map;

        Date newTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(newTime);
        op.insert("insert into bookBorrow(userID,ISBN,state,date) values(?,?,?,?)", new String[]{sp.getID(), book.get(0).getIsbn13(), "1",date});
        data = op.select("select count(1) num from bookBorrow where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() != 0) {
            map = data.get(0);
            if (map.get("num").toString().equals("1")) {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "预约书本","您已经成功预约了《"+book.get(0).getTitle()+"》。", "1",date});
                Toast.makeText(context, "预约成功", Toast.LENGTH_SHORT).show();
                state=1;
                info_state.setVisibility(View.VISIBLE);
            }
        } else {
            op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                    new String[]{sp.getID(), "预约书本","您预约的《"+book.get(0).getTitle()+"》，预约失败！", "1",date});
            Toast.makeText(context, "预约失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0x001){
            getData2();
        }
    }

    private void getData2() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        data = op.select("select * from bookInfo where ISBN=?", new String[]{isbn.getText().toString()});
        if (data.size() != 0) {
            map = data.get(0);
            img.setImageBitmap(readImage(map.get("imgPath")));
            name.setText(map.get("bookName"));
            author.setText(map.get("author"));
            publisher.setText(map.get("publisher"));
            price.setText(map.get("price"));
            isbn.setText(map.get("ISBN"));
            summary.setText(map.get("summary"));
        }

    }
}

package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private TextView est;

    private RelativeLayout visible;
    private TextView save;
    private TextView sub;
    private TextView esti;

    private Context context;
    private ArrayList<BookInfo> book;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private int state=0;
    private String libs;

    //弹窗所需的控件
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    getData();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        getData();

    }

    private void init() {
        context=DynamicInfo.this;
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
        est=findViewById(R.id.dynamic_info_est);

        visible=findViewById(R.id.dynamic_info_ly_more);
        save=findViewById(R.id.dynamic_info_save);
        sub=findViewById(R.id.dynamic_info_sub);
        esti=findViewById(R.id.dynamic_info_esti);

        visible.setOnClickListener(this);
        more.setOnClickListener(this);
        save.setOnClickListener(this);
        sub.setOnClickListener(this);
        esti.setOnClickListener(this);


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
        //获取书本评价
        data = op.select("select * from estimate where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() != 0) {
            StringBuffer sb = new StringBuffer();
            for (int i=0;i<data.size();i++) {
                map = data.get(i);
                sb.append(map.get("userName")+":"+map.get("estimate")+"\n");
            }
            est.setText(sb.toString());
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
            case R.id.dynamic_info_esti:
                visible.setVisibility(View.GONE);
                View ad_view2= getAlert(R.layout.ad_input_est);
                final EditText editText= (EditText) ad_view2.findViewById(R.id.ad_edit_pass);
                ad_view2.findViewById(R.id.ad_btn_pass_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //handler.sendEmptyMessage(0x0001);
                        alert.dismiss();
                    }
                });
                ad_view2.findViewById(R.id.ad_btn_pass_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //handler.sendEmptyMessage(0x0002);
                        if(!editText.getText().toString().equals("")){
                            //handler.sendEmptyMessage(0x003);
                            est(editText.getText().toString());
                            alert.dismiss();
                        }
                        else {
                            alert.dismiss();
                            View view=getAlert(R.layout.ad_pass_erro);
                            TextView txt= (TextView) view.findViewById(R.id.ad_txt_erro2);
                            //String name=editText.getText().toString();
                            if(editText.getText().toString().equals("")){
                                txt.setText("评价不能为空。");
                            }
                            view.findViewById(R.id.ad_btn_erro_confirm).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alert.dismiss();
                                }
                            });
                        }
                    }
                });
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
        data = op.select("select * from bookInfo where ISBN=? and libraryID=?", new String[]{book.get(0).getIsbn13(),libs});

        if (data.size() == 0) {
            op.insert("insert into bookInfo(ISBN,libraryID,bookName,author,publisher,tag,imgPath,price,summary) values(?,?,?,?,?,?,?,?,?)",
                    new String[]{book.get(0).getIsbn13(), libs, book.get(0).getTitle(), book.get(0).getAuthor(), book.get(0).getPublisher(),
                            "编程", book.get(0).getImagePath(), book.get(0).getPrice(), book.get(0).getSummary()});
            //判断是否插入成功
            data = op.select("select count(1) num from bookInfo where ISBN=? and libraryID=?", new String[]{book.get(0).getIsbn13(),libs});
            if (data.size() != 0) {
                map = data.get(0);
                if (!map.get("num").toString().equals("0")) {
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
            data = op.select("select count(1) num from bookBorrow where ISBN=? and userID=?", new String[]{book.get(0).getIsbn13(),sp.getID()});
            if (data.size() != 0) {
                map = data.get(0);
                if (map.get("num").toString().equals("1")) {
                    op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                            new String[]{sp.getID(), "预约书本","您已经成功预约了《"+book.get(0).getTitle()+"》。", "1",date});
                    if(book.get(0).getUserID()!=null) {
                        op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                                new String[]{book.get(0).getUserID(), "预约书本", "您的书《" + book.get(0).getTitle() + "》被【" + sp.getUserName() + "】预约了。", "1", date});
                    }
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

    /**
     * 评价
     * */
    public void est(String str){
        List<Map<String, String>> data;
        Map<String, String> map;

        Date newTime=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(newTime);
        op.insert("insert into estimate(userName,ISBN,estimate) values(?,?,?)", new String[]{sp.getUserName(), book.get(0).getIsbn13(),str});
        data = op.select("select count(1) num from estimate where ISBN=?", new String[]{book.get(0).getIsbn13()});
        if (data.size() != 0) {
            map = data.get(0);
            if (!map.get("num").toString().equals("0")) {
                op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                        new String[]{sp.getID(), "评价书本","您已经成功评价了《"+book.get(0).getTitle()+"》。", "1",date});
                Toast.makeText(context, "评价成功", Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessage(0x001);
            }
        } else {
            op.insert("insert into message(userID,title,content,state,date) values(?,?,?,?,?)",
                    new String[]{sp.getID(), "评价书本","您评价的《"+book.get(0).getTitle()+"》，评价失败！", "1",date});
            Toast.makeText(context, "评价失败，请稍后重试", Toast.LENGTH_SHORT).show();
        }
    }

    //定义弹窗方法
    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(context);
        //完成相关设置
        inflater=LayoutInflater.from(context);
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        alert=builder.create();
        alert.show();
        return ad_view;
    }
}

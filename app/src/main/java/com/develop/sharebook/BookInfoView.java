package com.develop.sharebook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.entity.BookInfo;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */

public class BookInfoView extends StatusBarUtil implements View.OnClickListener{

    private Button insert;
    private Button select;
    private ImgTxtLayout back;
    private Spinner lib;
    private Spinner tag;
    private EditText name;
    private EditText author;
    private EditText ISBN;
    private EditText price;
    private ImageView img;
    private EditText remark;

    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private Context context;

    private ArrayAdapter<String> adapter=null;
    private ArrayAdapter<String> adapter2=null;

    private String tags=null;
    private String libs=null;
    private String summary=null;
    private String imgPath=null;
    private List<String> aLib=new ArrayList<>();

    private static final int REQUEST_CODE_PICK_IMAGE=1;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA=2;
    private static final int CODE_RESULT_REQUEST=3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.bookinfo);

        init();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bookinfo;
    }

    public void init(){
        context=getApplicationContext();
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        lib=findViewById(R.id.book_lib);
        tag=findViewById(R.id.book_tag);
        name=findViewById(R.id.book_name);
        author=findViewById(R.id.book_author);
        ISBN=findViewById(R.id.book_isbn);
        price=findViewById(R.id.book_price);
        img=findViewById(R.id.book_img);
        remark=findViewById(R.id.book_remark);
        insert=findViewById(R.id.book_btn_new);
        select=findViewById(R.id.book_img_btn);
        back=findViewById(R.id.book_imgtxt_new);

        insert.setOnClickListener(this);
        select.setOnClickListener(this);
        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        setAdapter();


        getData();


    }

    private void setAdapter() {
        adapter=new ArrayAdapter<String>(this, R.layout.simple_spinner_item,R.id.tv_spinner,aLib);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        updateLib();

        adapter2=new ArrayAdapter<String>(this,R.layout.simple_spinner_item,R.id.tv_spinner);
        adapter2.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapter2.add("编程");
        adapter2.add("文学");
        adapter2.add("艺术");
        adapter2.add("军事");
        adapter2.add("航空");
        adapter2.add("经济");
        adapter2.add("政治");
        adapter2.add("生物");
        adapter2.add("探索");
        adapter2.add("小说");
        adapter2.add("其他");

        lib.setAdapter(adapter);
        tag.setAdapter(adapter2);

        lib.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adp= (ArrayAdapter<String>) parent.getAdapter();
                libs=adp.getItem(position);

                List<Map<String, String>> data = new ArrayList<>();
                data = op.select("select id from library where name=?", new String[]{libs});

                libs=data.get(0).get("id");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adp= (ArrayAdapter<String>) parent.getAdapter();
                tags=adp.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateLib() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        //aLib=new ArrayList<>();
        data = op.select("select id,name from library where id=1 or userId=?", new String[]{sp.getID()});
        if (data.size()!=0) {
            for (int i = 0; i < data.size(); i++) {
                map = data.get(i);
                aLib.add(map.get("name"));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void getData() {
        Intent intent=getIntent();
        Bundle bd=intent.getExtras();
        ArrayList<BookInfo> book= (ArrayList<BookInfo>) bd.getSerializable("book");

        if (book.get(0).getTitle()!=null){
            name.setText(book.get(0).getTitle());
            author.setText(book.get(0).getAuthor());
            ISBN.setText(book.get(0).getIsbn13());
            price.setText(book.get(0).getPrice());
            summary =book.get(0).getSummary();
            imgPath=book.get(0).getImagePath();
            if(imgPath!=null){
                readImage();
            }
        }else
        {
            ISBN.setText(book.get(0).getIsbn13());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.book_btn_new:
                update();
                break;
            case R.id.book_img_btn:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode==RESULT_OK &&data!=null) {
            if (data != null) {
                //cropPhoto(data.getData());
                setImageToHeadView(data);
            }
        }
        else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            Uri uri = data.getData();
            //to do find the path of pic
        }else if(requestCode==CODE_RESULT_REQUEST)
        {
            if (data!=null) {
                setImageToHeadView(data);
            }
        }
    }

    public void setImageToHeadView(Intent intent){
        //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        try {
            Bitmap photo= MediaStore.Images.Media.getBitmap(resolver, intent.getData());
            img.setImageBitmap(photo);
            saveImage(photo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片
     */
    private void saveImage(Bitmap bitmap) {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            filesDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            filesDir = context.getFilesDir();
        }
        FileOutputStream fos = null;
        try {
            File file = new File(filesDir,ISBN.getText().toString()+".png");
            imgPath=file.toString();
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(fos != null){
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
        File file = new File(imgPath);
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            img.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    /**
     * 保存图书
     * */
    private void update() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        op.insert("insert into bookInfo(ISBN,libraryID,bookName,author,tag,imgPath,price,summary,remark) values(?,?,?,?,?,?,?,?,?)",
                new String[]{ISBN.getText().toString(),libs,name.getText().toString(),author.getText().toString(),tags,imgPath,price.getText().toString(),summary,remark.getText().toString()});
        //判断是否插入成功
        data = op.select("select count(1) num from bookInfo where ISBN=?", new String[]{ISBN.getText().toString()});
        if (data.size() != 0) {
            map = data.get(0);
            if (map.get("num").toString().equals("1")) {
                Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
                finish();
            }else {
                Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show();
            }
        }
    }


}

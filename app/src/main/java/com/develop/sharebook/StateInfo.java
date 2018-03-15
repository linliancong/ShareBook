package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.develop.entity.BookInfo;
import com.develop.util.AdapterUtil;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/15.
 */

public class StateInfo extends StatusBarUtil {

    private ImgTxtLayout back;
    private int tag=0;
    private Context context;
    private SharedPreferenceUtils sp;
    private SqlOperator op;

    private Spinner select;
    private ListView list;
    private ArrayAdapter<String> adapter=null;
    private List<String> mSelect=new ArrayList<>();
    private int page;

    private BookInfo bookInfo;
    private ArrayList<BookInfo> bookInfos=new ArrayList<>();
    private AdapterUtil<BookInfo> adapterUtil;

    public static int positions=0;

    private TextView info;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        setAdapterS();
    }

    private void setAdapter() {
        adapterUtil=new AdapterUtil<BookInfo>(bookInfos, R.layout.dynamic_item){
            @Override
            public void bindView(ViewHolder holder, BookInfo obj) {
                if(readImage(obj.getImagePath())!=null) {
                    holder.setImageBitmap(R.id.dynamic_img, readImage(obj.getImagePath()));
                }
                holder.setText(R.id.dynamic_name,obj.getTitle());
                holder.setText(R.id.dynamic_author,obj.getAuthor());
                holder.setText(R.id.dynamic_summary,obj.getSummary());
            }
        };

        list.setAdapter(adapterUtil);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it=new Intent(context,StateAll.class);
                Bundle bd=new Bundle();
                BookInfo book1=bookInfos.get(position);
                ArrayList<BookInfo> bookInfos=new ArrayList<>();
                bookInfos.add(book1);
                bd.putSerializable("book",bookInfos);
                it.putExtras(bd);
                startActivityForResult(it,0x001);
            }

        });
    }

    private void setAdapterS() {
        adapter=new ArrayAdapter<String>(this, R.layout.simple_spinner_item,R.id.tv_spinner,mSelect);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);

        if(tag==1) {
            updateLib();
        }else
        {
            updateTag();
        }

        select.setAdapter(adapter);

        select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                positions=position;
                getData2(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void getData2(int position) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        bookInfos=new ArrayList<>();

        if(tag==2) {
            data = op.select("select * from bookInfo a,library b where a.tag=? and a.libraryID=b.id and b.userId=?", new String[]{mSelect.get(position), sp.getID()});
        }else
        {
            data = op.select("select * from bookInfo a,library b where b.name=? and a.libraryID=b.id and b.userId=?", new String[]{mSelect.get(position), sp.getID()});
        }
        if (data.size()!=0) {
            for (int i = 0; i < data.size(); i++) {
                map = data.get(i);
                bookInfo=new BookInfo();
                bookInfo.setTitle(map.get("bookName"));
                bookInfo.setAuthor(map.get("author"));
                bookInfo.setSummary(map.get("summary"));
                bookInfo.setPublisher(map.get("publisher"));
                bookInfo.setPrice(map.get("price"));
                bookInfo.setIsbn13(map.get("ISBN"));
                bookInfo.setImagePath(map.get("imgPath"));
                bookInfo.setTags(map.get("tag"));
                bookInfo.setLibraryName(map.get("name"));

                bookInfos.add(bookInfo);
                list.setVisibility(View.VISIBLE);
                info.setVisibility(View.GONE);
            }
        }else {
            list.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
            //Toast.makeText(context,"没有书本信息",Toast.LENGTH_SHORT).show();
        }
        setAdapter();
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

    private void init() {
        back=findViewById(R.id.state_imgtxt_back);
        context=getApplicationContext();
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        list=findViewById(R.id.state_info_list);
        select=findViewById(R.id.state_info_select);
        info=findViewById(R.id.state_info_info);

        Intent intent=getIntent();
        tag=intent.getIntExtra("tag",0);
        if (tag==1){
            back.setText("书库查看");
        }else if(tag==2){
            back.setText("标签查看");
        }


        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.state_info;
    }

    private void updateLib() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        //mSelect=new ArrayList<>();
        data = op.select("select id,name from library where userId=?", new String[]{sp.getID()});
        if (data.size()!=0) {
            for (int i = 0; i < data.size(); i++) {
                map = data.get(i);
                mSelect.add(map.get("name"));
                list.setVisibility(View.VISIBLE);
                info.setVisibility(View.GONE);
            }
            //adapter.notifyDataSetChanged();
        }
        else
        {
            list.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
        }
    }

    private void updateTag() {
        //mSelect=new ArrayList<>();
        mSelect.add("编程");
        mSelect.add("文学");
        mSelect.add("艺术");
        mSelect.add("军事");
        mSelect.add("航空");
        mSelect.add("经济");
        mSelect.add("政治");
        mSelect.add("生物");
        mSelect.add("探索");
        mSelect.add("小说");
        mSelect.add("其他");
        //adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0x001){
            getData2(positions);
        }
    }
}

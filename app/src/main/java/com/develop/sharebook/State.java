package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.develop.entity.BookInfo;
import com.develop.entity.Messages;
import com.develop.util.AdapterUtil;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/9.
 */

public class State extends Fragment implements RadioGroup.OnCheckedChangeListener,View.OnClickListener{

    private Context context;
    private View view;

    private SharedPreferenceUtils sp;
    private SqlOperator op;

    //类型查询
    private RadioGroup rg;
    private RadioButton all;
    private RadioButton condition;
    private int type =1;

    //一个是显示书库的列表，一个是显示分类的选项
    private ListView list;
    private LinearLayout classify;

    private RelativeLayout lib;
    private RelativeLayout tag;

    private AdapterUtil<BookInfo> adapter;
    private BookInfo bookInfo;
    private ArrayList<BookInfo> bookInfos=new ArrayList<>();

    private TextView info;

    public State(){}
    @SuppressLint("ValidFragment")
    public State(Context context){
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.state, container, false);

            init();

        }
        if(type==1) {
            getData();
        }
        return view;
    }

    private void init() {
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        //类型查询
        rg=view.findViewById(R.id.state_rg_type);
        all =view.findViewById(R.id.state_all);
        condition =view.findViewById(R.id.state_condition);
        rg.setOnCheckedChangeListener(this);
        all.setChecked(true);
        info=view.findViewById(R.id.state_info);

        list=view.findViewById(R.id.state_list);
        classify=view.findViewById(R.id.state_classify);
        lib=view.findViewById(R.id.state_lib);
        tag=view.findViewById(R.id.state_tag);
        lib.setOnClickListener(this);
        tag.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.state_all:
                setChecked();
                all.setChecked(true);
                list.setVisibility(View.VISIBLE);
                classify.setVisibility(View.GONE);
                info.setVisibility(View.GONE);
                type =1;
                getData();
                break;
            case R.id.state_condition:
                setChecked();
                condition.setChecked(true);
                list.setVisibility(View.GONE);
                classify.setVisibility(View.VISIBLE);
                info.setVisibility(View.GONE);
                type =2;
                break;
        }
    }

    public void setChecked(){
        all.setChecked(false);
        condition.setChecked(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.state_lib:
                Intent intent=new Intent(context,StateInfo.class);
                intent.putExtra("tag",1);
                startActivity(intent);
                break;
            case R.id.state_tag:
                Intent intent2=new Intent(context,StateInfo.class);
                intent2.putExtra("tag",2);
                startActivity(intent2);
                break;
        }
    }

    private void getData() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        bookInfos=new ArrayList<>();
        data = op.select("select * from bookInfo a,library b where a.libraryID=b.id and b.userID=?", new String[]{sp.getID()});
        if (data.size() != 0) {
            for (int i=0;i<data.size();i++) {
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
        }else
        {
            if (type==1) {
                list.setVisibility(View.GONE);
                info.setVisibility(View.VISIBLE);
            }
        }
        setAdapter();
    }

    private void setAdapter() {
        adapter=new AdapterUtil<BookInfo>(bookInfos, R.layout.dynamic_item){
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

        list.setAdapter(adapter);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0x001){
            getData();
        }
    }
}

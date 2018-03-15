package com.develop.sharebook;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.develop.entity.LiabaryInfo;
import com.develop.util.AdapterUtil;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/13.
 */

public class LiabaryShow extends StatusBarUtil {

    private AdapterUtil adapter;
    private ArrayList<LiabaryInfo> lib=new ArrayList<>();
    private ImgTxtLayout back;
    private ListView list;

    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private Context context;
    private TextView info;

    @Override
    protected int getLayoutResId() {
        return R.layout.liabary_show;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

    }

    private void init() {
        context=getApplicationContext();

        back=findViewById(R.id.liabary_imgtxt_back);
        list=findViewById(R.id.library_list);
        info=findViewById(R.id.library_info);
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取数据
        getData();

        //绑定数据
        setAdapter();
    }

    private void getData() {
        List<Map<String, String>> data = new ArrayList<>();
        //查书库
        data = op.select("select * from library where userID=?", new String[]{sp.getID()});
        if (data.size() != 0) {
            for (int i=0;i<data.size();i++) {
                List<Map<String, String>> data2 = new ArrayList<>();
                //根据书库查书本数量
                data2 = op.select("select count(*) num from bookInfo where libraryID=?", new String[]{data.get(i).get("id").toString()});
                LiabaryInfo liabaryInfo=new LiabaryInfo(data.get(i).get("name"),data2.get(0).get("num"));

                lib.add(liabaryInfo);
                list.setVisibility(View.VISIBLE);
                info.setVisibility(View.GONE);
            }
        }else
        {
            list.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
        }
    }

    private void setAdapter() {
        adapter=new AdapterUtil<LiabaryInfo>(lib, R.layout.liabary_show_item){
            @Override
            public void bindView(ViewHolder holder, LiabaryInfo obj) {
                holder.setText(R.id.library_item_name,obj.getName());
                holder.setText(R.id.library_item_count,obj.getCount());
            }
        };

        list.setAdapter(adapter);
    }
}

package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.entity.LiabaryInfo;
import com.develop.util.AdapterUtil;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/9.
 */

public class Liarary extends Fragment implements View.OnClickListener{

    private Context context;
    private View view;

    private RelativeLayout show;
    private RelativeLayout build;

    //弹窗所需的控件
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private SharedPreferenceUtils sp;
    private SqlOperator op;

    public Liarary(){}
    @SuppressLint("ValidFragment")
    public Liarary(Context context){
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.liabary, container, false);
            show=view.findViewById(R.id.library_ly_show);
            build=view.findViewById(R.id.library_ly_new);
            show.setOnClickListener(this);
            build.setOnClickListener(this);
            sp=new SharedPreferenceUtils(context);
            op=new SqlOperator(context);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.library_ly_show:
                Intent intent=new Intent(context,LiabaryShow.class);
                startActivity(intent);
                break;
            case R.id.library_ly_new:
                Date newTime=new Date();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                final String date=sdf.format(newTime);

                View view2= getAlert(R.layout.ad_input);
                final TextView text=view2.findViewById(R.id.ad_txt);
                final EditText editText= view2.findViewById(R.id.ad_edit);
                text.setText("请输入新建的书库名");
                view2.findViewById(R.id.ad_btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                    }
                });
                view2.findViewById(R.id.ad_btn_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        View view1=getAlert(R.layout.ad_pass_erro);
                        final TextView txt1= view1.findViewById(R.id.ad_txt_erro2);
                        view1.findViewById(R.id.ad_btn_erro_confirm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alert.dismiss();
                            }
                        });
                        List<Map<String, String>> data = new ArrayList<>();
                        Map<String, String> map = new HashMap<>();
                        if(editText.getText().length()!=0) {
                            data = op.select("select count(1) num from library where userID=? and name=?", new String[]{sp.getID(), editText.getText().toString()});
                            if (data.size() != 0) {
                                map = data.get(0);
                                if (!map.get("num").toString().equals("0")) {
                                    op.insert("insert into message1(userID,title,content,state,date) values(?,?,?,?,?)",
                                            new String[]{sp.getID(), "新建书库","您创建书库【"+editText.getText().toString()+"】失败！书库已存在。", "1",date});
                                    txt1.setText("书库已存在，请重试");
                                } else {
                                    op.insert("insert into library(userID,name) values(?,?)", new String[]{sp.getID(), editText.getText().toString()});
                                    //判断是否修改成功
                                    data = op.select("select count(1) num from library where userID=? and name=?", new String[]{sp.getID(), editText.getText().toString()});
                                    if (data.size() != 0) {
                                        map = data.get(0);
                                        if (map.get("num").toString().equals("1")) {
                                            op.insert("insert into message1(userID,title,content,state,date) values(?,?,?,?,?)",
                                                    new String[]{sp.getID(), "新建书库","您创建书库【"+editText.getText().toString()+"】成功！", "1",date});
                                            txt1.setText("新建成功");
                                        } else {
                                            op.insert("insert into message1(userID,title,content,state,date) values(?,?,?,?,?)",
                                                    new String[]{sp.getID(), "新建书库","您创建书库【"+editText.getText().toString()+"】失败！", "1",date});
                                            txt1.setText("新建失败");
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            if(editText.getText().toString().equals("")){
                                txt1.setText("书库名称不能为空");
                            }
                        }
                    }
                });
                break;
        }
    }

    //定义弹窗方法
    public View getAlert(int mLayout){
        View ad_view;
        //初始化Builder
        builder=new AlertDialog.Builder(context);
        //完成相关设置
        inflater=getActivity().getLayoutInflater();
        ad_view=inflater.inflate(mLayout,null,false);
        builder.setView(ad_view);
        builder.setCancelable(true);
        alert=builder.create();
        alert.show();
        return ad_view;
    }
}

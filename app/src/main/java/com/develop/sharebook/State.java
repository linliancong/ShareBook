package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;

/**
 * Created by Administrator on 2018/3/9.
 */

public class State extends Fragment implements RadioGroup.OnCheckedChangeListener{

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

            sp=new SharedPreferenceUtils(context);
            op=new SqlOperator(context);

            //类型查询
            rg=view.findViewById(R.id.state_rg_type);
            all =view.findViewById(R.id.state_all);
            condition =view.findViewById(R.id.state_condition);
            rg.setOnCheckedChangeListener(this);
            all.setChecked(true);
        }
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.state_all:
                setChecked();
                all.setChecked(true);
                type =1;
                break;
            case R.id.state_condition:
                setChecked();
                condition.setChecked(true);
                type =2;
                break;
        }
    }

    public void setChecked(){
        all.setChecked(false);
        condition.setChecked(false);
    }
}

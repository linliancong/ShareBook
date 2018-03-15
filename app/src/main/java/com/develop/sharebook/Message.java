package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/9.
 */

public class Message extends Fragment implements View.OnClickListener{

    private Context context;
    private View view;

    private SharedPreferenceUtils sp;
    private SqlOperator op;

    private RelativeLayout wd;
    private RelativeLayout yd;
    private int count=0;
    private TextView num;

    public static boolean state=false;
    private MyBroadcast broad;
    List<Map<String, String>> data = new ArrayList<>();
    Map<String, String> map = new HashMap<>();

    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            if(msg.what==0x001){
                getData();
            }
        }
    };

    public Message(){}
    @SuppressLint("ValidFragment")
    public Message(Context context){
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.message, container, false);

            sp=new SharedPreferenceUtils(context);
            op=new SqlOperator(context);

            wd=view.findViewById(R.id.message_wd);
            yd=view.findViewById(R.id.message_yd);
            num=view.findViewById(R.id.message_num);

            wd.setOnClickListener(this);
            yd.setOnClickListener(this);

            //广播更新
            broad=new Message.MyBroadcast();
            IntentFilter filter=new IntentFilter();
            filter.addAction("com.develop.sharebook.MYBROADCAST2");
            getActivity().registerReceiver(broad,filter);


            new Thread(){
                @Override
                public void run() {
                    while (true)
                    {
                        if(state) {
                            state=false;
                            handler.sendEmptyMessage(0x001);
                        }
                    }
                }
            }.start();


        }

        getData();

        return view;
    }

    private void getData() {
        List<Map<String, String>> data;
        Map<String, String> map;
        data = op.select("select count(*) num from message where userID=? and state=1", new String[]{sp.getID()});
        if (data.size() != 0) {
            map = data.get(0);
            if (!map.get("num").equals("0")) {
                count = new Integer(data.get(0).get("num"));
                num.setVisibility(View.VISIBLE);
                num.setText(count + "");
            } else {
                num.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.message_wd:
                Intent intent=new Intent(context,MessageInfo.class);
                intent.putExtra("tag",1);
                startActivity(intent);
                break;
            case R.id.message_yd:
                Intent intent2=new Intent(context,MessageInfo.class);
                intent2.putExtra("tag",2);
                startActivity(intent2);
                break;
        }
    }


    public static class MyBroadcast extends BroadcastReceiver {
        public final String board="com.develop.sharebook.MYBROADCAST2";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(board)){
                state=true;
                //Toast.makeText(context,"ces",Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(broad!=null){
            getActivity().unregisterReceiver(broad);
        }
    }
}

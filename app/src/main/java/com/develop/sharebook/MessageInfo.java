package com.develop.sharebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.develop.entity.Messages;
import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/3/15.
 */

public class MessageInfo extends StatusBarUtil {

    private ImgTxtLayout back;
    private int tag=0;
    private Context context;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private ListView list;
    private Messages msg;
    private ArrayList<Messages> msgs=new ArrayList<>();

    private MyAdapter adapter;
    private List<Map<String,String>> mapList=new ArrayList<Map<String,String>>();
    private LayoutInflater inflater;

    private TextView message_info;
    private Intent intent;
    private int count=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();


        inflater=getLayoutInflater();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> adp= (ArrayAdapter<String>) parent.getAdapter();
                if (tag==1) {
                    Messages message= msgs.get(position/2);
                    op.insert("update message set state=2 where id=?", new String[]{message.getId()+""});
                    getData();
                }
            }
        });

        getData();


    }

    private void getData() {
        msgs=new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        data = op.select("select * from message where userID=? and state=?", new String[]{sp.getID(),tag+""});
        if (data.size() != 0) {
            for (int i=0;i<data.size();i++) {
                map = data.get(i);
                msg=new Messages();
                msg.setContent(map.get("content"));
                msg.setTitle(map.get("title"));
                msg.setDate(map.get("date"));
                msg.setId(new Integer(map.get("id")));
                msgs.add(msg);
                count=i+1;
            }
            list.setVisibility(View.VISIBLE);
            message_info.setVisibility(View.GONE);
        }else {
            count=0;
            list.setVisibility(View.GONE);
            message_info.setVisibility(View.VISIBLE);
        }
        showQuestion();
    }

    private void init() {
        back=findViewById(R.id.message_imgtxt_back);
        list=findViewById(R.id.message_info_list);
        context=getApplicationContext();
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);

        message_info=findViewById(R.id.message_info);

        intent=getIntent();
        tag=intent.getIntExtra("tag",0);
        if (tag==1){
            back.setText("未读消息");
        }else if(tag==2){
            back.setText("已读消息");
        }

        back.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("com.develop.sharebook.MYBROADCAST2"));
                finish();
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.message_info;
    }

    private void showQuestion() {
        Map<String,String> map;
        mapList=new ArrayList<>();
        adapter=new MyAdapter(this,mapList);
        list.setAdapter(adapter);
        String str;
        String date;
        String time;


        for(int i=0;i<msgs.size();i++)
        {
            str=msgs.get(i).getDate();
            Date oldTime;
            Date newTime=new Date();
            SimpleDateFormat sdf=new SimpleDateFormat("MM月dd日");
            SimpleDateFormat sdf2=new SimpleDateFormat("HH:mm");
            SimpleDateFormat sdf3=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            SimpleDateFormat sdf4=new SimpleDateFormat("yyyy年MM月dd日");
            SimpleDateFormat sdf5=new SimpleDateFormat("yyyy");
            try {
                oldTime=sdf3.parse(str);
                String str1=sdf5.format(oldTime);
                String str2=sdf5.format(newTime);
                if(str1.equals(str2)) {
                    date = sdf.format(oldTime);
                }
                else {
                    date=sdf4.format(oldTime);
                }
                time=sdf2.format(oldTime);
                if(getDateDiff(oldTime)==0){
                    time="昨天"+"\t"+time;
                }
                else if(getDateDiff(oldTime)==2)
                {
                    time=date+"\t"+time;
                }
                map=new HashMap<String, String>();
                map.put("type","0");
                map.put("time",time);
                mapList.add(map);
                map=new HashMap<String, String>();
                map.put("type","1");
                map.put("title",msgs.get(i).getTitle());
                map.put("time",date);
                map.put("text",msgs.get(i).getContent());
                mapList.add(map);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
        list.setSelection(list.getCount()-1);
    }

    /**
     *@return 0:昨天 1：今天 2：之前
     * */
    public int getDateDiff(Date oldDate){
        long d=1000*24*60*60;
        /*long h=1000*60*60;
        long m=1000*60;*/
        //当前时间毫秒数
        long current=System.currentTimeMillis();
        //今天零点零分零秒的毫秒数
        long zero=current/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset();
        //总共相差的毫秒数
        //long diff=newDate.getTime()-oldDate.getTime();
        /*//相差的天数、小时数、分钟数
        long day=diff/d;
        long hour=diff%d/h;
        long min=diff%d%h/m;
        long hours=diff/h;*/
        //今天相差的毫秒数
        //long diff2=newDate.getTime()-zero;


        if(oldDate.getTime()>zero){
            return 1;
        }
        else {
            //昨天的23:59:59
            long dif=zero-d-1;
            if(oldDate.getTime()<dif)
            {
                return 2;
            }
            else {
                return 0;
            }
        }

    }

    public class MyAdapter extends ArrayAdapter {
        private List<Map<String,String>> list;
        private Context context;
        private int type;
        private ListView listView;

        public MyAdapter(Context context,List<Map<String,String>> list){
            super(context,0);
            this.context=context;
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            Map<String,String> map=(Map<String,String>)list.get(position);
            if(map.get("type").equals("0")){
                if(convertView==null){
                    convertView=inflater.inflate(R.layout.message_info_time,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.time= (TextView) convertView.findViewById(R.id.message_item_time);
                    convertView.setTag(viewHolder);
                }
                else{
                    viewHolder= (ViewHolder) convertView.getTag();
                }
                //防止滑动过快convertView返回的是上一个
                if(viewHolder.time==null){
                    convertView=inflater.inflate(R.layout.message_info_time,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.time= (TextView) convertView.findViewById(R.id.message_item_time);
                    convertView.setTag(viewHolder);
                }
                viewHolder.time.setText(map.get("time"));
            }
            else
            {
                if(convertView==null){
                    convertView=inflater.inflate(R.layout.message_info_item,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.text= (TextView) convertView.findViewById(R.id.message_item_txt);
                    viewHolder.txt_time= (TextView) convertView.findViewById(R.id.message_item_txt2);
                    viewHolder.title= (TextView) convertView.findViewById(R.id.message_item_txt1);
                    convertView.setTag(viewHolder);
                }
                else{
                    viewHolder= (ViewHolder) convertView.getTag();
                }
                //防止滑动过快convertView返回的是上一个
                if(viewHolder.text==null || viewHolder.txt_time==null ||viewHolder.title==null){
                    convertView=inflater.inflate(R.layout.message_info_item,parent,false);
                    viewHolder=new ViewHolder();
                    viewHolder.text= (TextView) convertView.findViewById(R.id.message_item_txt);
                    viewHolder.txt_time= (TextView) convertView.findViewById(R.id.message_item_txt2);
                    viewHolder.title= (TextView) convertView.findViewById(R.id.message_item_txt1);
                    convertView.setTag(viewHolder);
                }
                viewHolder.title.setText(map.get("title"));
                viewHolder.text.setText(map.get("text"));
                viewHolder.txt_time.setText(map.get("time"));
            }
            Log.i("现在的索引是","-----------"+position+"-----------------");
            return convertView;
        }

        public final class ViewHolder{
            TextView time;
            TextView text;
            TextView txt_time;
            TextView title;
        }
    }
}

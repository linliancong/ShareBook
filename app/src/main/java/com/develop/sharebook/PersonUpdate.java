package com.develop.sharebook;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.bigkoo.pickerview.OptionsPickerView;
import com.develop.entity.AddressCh;
import com.develop.util.ImgTxtLayout;
import com.develop.util.LoadingAddressChUtils;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/7.
 */

public class PersonUpdate extends StatusBarUtil implements TextWatcher,View.OnClickListener {

    private ImgTxtLayout person_imgtxt_update;
    private Button person_btn_update;
    private EditText person_txt_update;

    private String content;
    private String value;

    private String mProvinceName;
    private String mCityName;
    private String mCountName;

    private Intent it;
    //private MeSy meSy;
    private int tag=-1;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private Context context;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x001:
                    Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
                    sendBroadcast(new Intent("com.zxhl.gpsking.MYBROADCAST"));
                    break;
                case 0x002:
                    Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.me_info);
        //AppManager.getAppManager().addActivity(MeSyUpdate.this);
        init();


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.person_info;
    }

    public void init() {
        context=PersonUpdate.this;
        sp=new SharedPreferenceUtils(context);
        op=new SqlOperator(context);
        person_imgtxt_update = findViewById(R.id.person_imgtxt_update);
        person_btn_update = findViewById(R.id.person_btn_update);
        person_txt_update = findViewById(R.id.person_txt_update);

        it = getIntent();
        Bundle bd = it.getExtras();
        content = bd.getString("STR");
        value = bd.getString("VALUE");
        person_txt_update.setText(content);
        person_txt_update.setSelection(content.length());

        person_txt_update.addTextChangedListener(this);
        person_btn_update.setOnClickListener(this);
        person_imgtxt_update.setOnClickListener(new ImgTxtLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switch (value) {
            case "name":
                person_imgtxt_update.setText("姓名");
                break;
            case "userName":
                person_imgtxt_update.setText("账号");
                break;
            case "email":
                person_imgtxt_update.setText("邮箱");
                break;
            case "remark":
                person_imgtxt_update.setText("个性签名");
                break;
            case "IDCard":
                person_imgtxt_update.setText("身份证");
                break;
            case "phone":
                person_imgtxt_update.setText("手机");
                break;
            case "QQ":
                person_imgtxt_update.setText("QQ");
                break;
            case "address":
                person_imgtxt_update.setText("地址");
                selectAddress(2);
                break;
            default:
                break;

        }
    }

    @Override
    public void onClick(View v) {
        update(value);
        finish();
    }

    private void update(String sql) {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        op.insert("update user set "+sql+"=? where id=?", new String[]{person_txt_update.getText().toString(),sp.getID()});
        //判断是否修改成功
        data = op.select("select count(1) num from user where "+sql+"=? and id=?", new String[]{person_txt_update.getText().toString(),sp.getID()});
        if (data.size() != 0) {
            map = data.get(0);
            if (map.get("num").toString().equals("1")) {
                handler.sendEmptyMessage(0x001);
            }else {
                handler.sendEmptyMessage(0x002);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        person_btn_update.setEnabled(false);

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (person_txt_update.getText().toString().length() != 0) {
            person_btn_update.setEnabled(true);
        }

    }

    public void selectAddress(final int tag) {
        final AddressCh addressCh= LoadingAddressChUtils.loading(getApplicationContext());
        OptionsPickerView option = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                mProvinceName = addressCh.getProvince().get(options1);
                mCityName = addressCh.getCity().get(options1).get(options2);
                mCountName = addressCh.getCounty().get(options1).get(options2).get(options3);
                if(tag==1) {
                    strReplace();
                    person_txt_update.setText(mProvinceName + "-" + mCountName);
                    person_txt_update.setSelection(person_txt_update.getText().toString().length());
                }
                else{
                    person_txt_update.setText(mProvinceName + "\t" + mCityName + "\t" +mCountName+"\t");
                    person_txt_update.setSelection(person_txt_update.getText().toString().length());
                }

            }
        }).build();
        option.setPicker(addressCh.getProvince(), addressCh.getCity(),addressCh.getCounty());
        option.show();

    }

    private void strReplace() {
        //因为中文空格比英文空格多一个，所以使用的时候需要多打一个空格才可以去除
        //mCountName=mCountName.replaceAll("  ","");
        // \s 可以匹配空格、制表符、换页符等空白字符的其中任意一个
        mCountName=mCountName.replaceAll("\\s*","");
        if(!mProvinceName.contains("特别行政区")) {
            if (mCountName.contains("县") && !mCountName.contains("自治县") && mCountName.length()>2) {
                mCountName = mCountName.replaceAll("县", "");
            }
            if (mCountName.contains("区") && mCountName.length()>2) {
                mCountName = mCountName.replaceAll("区", "");
            }
        }
        else
        {
            mProvinceName=mProvinceName.replaceAll("特别行政区","");
            mCountName = mProvinceName;
        }
        if(mProvinceName.contains("省"))
        {
            mProvinceName=mProvinceName.replaceAll("省","");
        }
        if(mProvinceName.contains("壮族自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("壮族自治区","");
        }
        if(mProvinceName.contains("维吾尔自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("维吾尔自治区","");
        }
        if(mProvinceName.contains("回族自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("回族自治区","");
        }
        if(mProvinceName.contains("自治区"))
        {
            mProvinceName=mProvinceName.replaceAll("自治区","");
        }
    }

}

package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ResourceBusyException;
import android.net.Uri;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.develop.util.ImgTxtLayout;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.database.SqlOperator;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2018/3/9.
 */

public class PersonInfo extends Fragment implements View.OnClickListener{

    private static final int REQUEST_CODE_PICK_IMAGE=1;
    private static final int REQUEST_CODE_CAPTURE_CAMEIA=2;
    private static final int CODE_RESULT_REQUEST=3;

    private Context context;
    private View view;
    private SharedPreferenceUtils sp;
    private SqlOperator op;
    private List<Map<String, String>> data = new ArrayList<>();
    private Map<String, String> map = new HashMap<>();

    private MyBroadcast broad;
    public static boolean state2=false;


    private RelativeLayout tx;
    private RelativeLayout xm;
    private RelativeLayout zh;
    private RelativeLayout yx;
    private RelativeLayout gxqm;
    private RelativeLayout sfz;
    private RelativeLayout sj;
    private RelativeLayout qq;
    private RelativeLayout dz;
    private RelativeLayout pwd;

    private ImageView img;
    private TextView tx_t;
    private TextView tx2_t;

    private ImgTxtLayout xm_i;
    private ImgTxtLayout zh_i;
    private ImgTxtLayout yx_i;
    private ImgTxtLayout gxqm_i;
    private ImgTxtLayout sfz_i;
    private ImgTxtLayout sj_i;
    private ImgTxtLayout qq_i;
    private ImgTxtLayout dz_i;

    //弹窗所需的控件
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    private ShowAct showAct;
    private int tag=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x001:
                    getUserInfo();
                    sp.setIsUpdate(true);
                    break;
                case 0x002:
                    sp.setIsUpdate(true);
                    break;
                case 0x003:
                    showAct = (ShowAct) getActivity();
                    showAct.callBack(0x003);
                    break;
            }
        }
    };


    public PersonInfo(){}
    @SuppressLint("ValidFragment")
    public PersonInfo(Context context){
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.personinfo, container, false);
            sp=new SharedPreferenceUtils(context);
            op=new SqlOperator(context);
            init();
            readImage();
            //广播更新
            broad=new MyBroadcast();
            IntentFilter filter=new IntentFilter();
            filter.addAction("com.zxhl.gpsking.MYBROADCAST");
            getActivity().registerReceiver(broad,filter);

            if(!sp.getIsVisitor()) {
                data = op.select("select * from user where id=?", new String[]{sp.getID()});
                if (data.size() != 0) {
                    map = data.get(0);
                }
            }
            new Thread(){
                @Override
                public void run() {
                    while (true)
                    {
                        if(state2) {
                            state2=false;
                            handler.sendEmptyMessage(0x001);
                        }
                    }
                }
            }.start();
        }
        getUserInfo();
        return view;
    }

    public void init(){
        tx=view.findViewById(R.id.person_ly_tx);
        xm=view.findViewById(R.id.person_ly_xm);
        zh=view.findViewById(R.id.person_ly_zh);
        yx=view.findViewById(R.id.person_ly_yx);
        gxqm=view.findViewById(R.id.person_ly_gxqm);
        sfz=view.findViewById(R.id.person_ly_sfz);
        sj=view.findViewById(R.id.person_ly_sj);
        qq=view.findViewById(R.id.person_ly_qq);
        dz=view.findViewById(R.id.person_ly_dz);
        pwd=view.findViewById(R.id.person_ly_pwd);

        img=view.findViewById(R.id.person_img_tx);
        tx_t=view.findViewById(R.id.person_txt_tx);
        tx2_t=view.findViewById(R.id.person_txt_tx2);

        xm_i=view.findViewById(R.id.person_imgtxt_xm);
        zh_i=view.findViewById(R.id.person_imgtxt_zh);
        yx_i=view.findViewById(R.id.person_imgtxt_yx);
        gxqm_i=view.findViewById(R.id.person_imgtxt_gxqm);
        sfz_i=view.findViewById(R.id.person_imgtxt_sfz);
        sj_i=view.findViewById(R.id.person_imgtxt_sj);
        qq_i=view.findViewById(R.id.person_imgtxt_qq);
        dz_i=view.findViewById(R.id.person_imgtxt_dz);

        tx.setOnClickListener(this);
		xm.setOnClickListener(this);
		//zh.setOnClickListener(this);
		yx.setOnClickListener(this);
		gxqm.setOnClickListener(this);
		sfz.setOnClickListener(this);
		sj.setOnClickListener(this);
		qq.setOnClickListener(this);
		dz.setOnClickListener(this);
		pwd.setOnClickListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode==RESULT_OK &&data!=null) {
            if (data != null) {
                cropPhoto(data.getData());
            }
        }
        else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
            Uri uri = data.getData();
            //to do find the path of pic
        }else if(requestCode==CODE_RESULT_REQUEST)
        {
            if (data!=null) {
                setImageToHeadView(data);
                handler.sendEmptyMessage(0x002);
            }
        }
    }

    /**
     *裁剪图片
     * */
    public void cropPhoto(Uri uri){
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");

        //设置裁剪
        intent.putExtra("crop","true");

        // aspectX , aspectY :宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX , outputY : 裁剪图片宽高
        intent.putExtra("outputX", img.getWidth());
        intent.putExtra("outputY", img.getHeight());
        intent.putExtra("return-data", true);

        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

    public void setImageToHeadView(Intent intent){
        Bundle bd=intent.getExtras();
        if(bd!=null){
            Bitmap photo=bd.getParcelable("data");
            img.setImageBitmap(toOvalBitmap(photo,200));
            saveImage(toOvalBitmap(photo,200));

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
            File file = new File(filesDir,sp.getID()+".png");
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
        File file = new File(filesDir,sp.getID()+".png");
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            img.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    /**
     * 设置图片圆形显示
     * */
    public static Bitmap toOvalBitmap(Bitmap bitmap, float pix) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getHeight(),
                    bitmap.getWidth(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getWidth());
            RectF rectF = new RectF(rect);
            float roundPx = pix;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            int color = 0xff424242;
            paint.setColor(color);
            canvas.drawOval(rectF, paint);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.person_ly_tx:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                break;
            case R.id.person_ly_xm:
                Intent it_xm=new Intent();
                it_xm.setClass(context,PersonUpdate.class);
                Bundle bd_xm=new Bundle();
                bd_xm.putString("STR",map.get("name"));
                bd_xm.putString("VALUE","name");
                it_xm.putExtras(bd_xm);
                startActivity(it_xm);
                break;
            case R.id.person_ly_zh:
                Intent it_zh=new Intent();
                it_zh.setClass(context,PersonUpdate.class);
                Bundle bd_zh=new Bundle();
                bd_zh.putString("STR",map.get("userName"));
                bd_zh.putString("VALUE","userName");
                it_zh.putExtras(bd_zh);
                startActivity(it_zh);
                break;
            case R.id.person_ly_yx:
                Intent it_yx=new Intent();
                it_yx.setClass(context,PersonUpdate.class);
                Bundle bd_yx=new Bundle();
                bd_yx.putString("STR",map.get("email"));
                bd_yx.putString("VALUE","email");
                it_yx.putExtras(bd_yx);
                startActivity(it_yx);
                break;
            case R.id.person_ly_gxqm:
                Intent it_gxqm=new Intent();
                it_gxqm.setClass(context,PersonUpdate.class);
                Bundle bd_gxqm=new Bundle();
                bd_gxqm.putString("STR",map.get("remark"));
                bd_gxqm.putString("VALUE","remark");
                it_gxqm.putExtras(bd_gxqm);
                startActivity(it_gxqm);
                break;
            case R.id.person_ly_sfz:
                Intent it_sfz=new Intent();
                it_sfz.setClass(context,PersonUpdate.class);
                Bundle bd_sfz=new Bundle();
                bd_sfz.putString("STR",map.get("IDCard"));
                bd_sfz.putString("VALUE","IDCard");
                it_sfz.putExtras(bd_sfz);
                startActivity(it_sfz);
                break;
            case R.id.person_ly_sj:
                Intent it_sj=new Intent();
                it_sj.setClass(context,PersonUpdate.class);
                Bundle bd_sj=new Bundle();
                bd_sj.putString("STR",map.get("phone"));
                bd_sj.putString("VALUE","phone");
                it_sj.putExtras(bd_sj);
                startActivity(it_sj);
                break;
            case R.id.person_ly_qq:
                Intent it_qq=new Intent();
                it_qq.setClass(context,PersonUpdate.class);
                Bundle bd_qq=new Bundle();
                bd_qq.putString("STR",map.get("QQ"));
                bd_qq.putString("VALUE","QQ");
                it_qq.putExtras(bd_qq);
                startActivity(it_qq);
                break;
            case R.id.person_ly_dz:
                Intent it_dz=new Intent();
                it_dz.setClass(context,PersonUpdate.class);
                Bundle bd_dz=new Bundle();
                bd_dz.putString("STR",map.get("address"));
                bd_dz.putString("VALUE","address");
                it_dz.putExtras(bd_dz);
                startActivity(it_dz);
                break;
            case R.id.person_ly_pwd:
                View ad_view2= getAlert(R.layout.ad_input_pass);
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
                        SharedPreferenceUtils sp=new SharedPreferenceUtils(context);
                        if(sp.getPWD().equals(editText.getText().toString())){
                            handler.sendEmptyMessage(0x003);
                            alert.dismiss();
                        }
                        else {
                            alert.dismiss();
                            View view=getAlert(R.layout.ad_pass_erro);
                            TextView txt= (TextView) view.findViewById(R.id.ad_txt_erro2);
                            //String name=editText.getText().toString();
                            if(editText.getText().toString().equals("")){
                                txt.setText("原密码不能为空。");
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

    public void getUserInfo(){
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        data = op.select("select * from user where id=?", new String[]{sp.getID()});
        if (data.size()!=0) {
            map = data.get(0);
            tx_t.setText(map.get("name"));
            tx2_t.setText(map.get("userName"));
            xm_i.setText(map.get("name"));
            zh_i.setText(map.get("userName"));
            yx_i.setText(map.get("email"));
            gxqm_i.setText(map.get("remark"));
            sfz_i.setText(map.get("IDCard"));
            sj_i.setText(map.get("phone"));
            qq_i.setText(map.get("QQ"));
            dz_i.setText(map.get("address"));

            sp.setName(map.get("name"));
            sp.setRemark(map.get("remark"));
        }
    }

    public static class MyBroadcast extends BroadcastReceiver {
        public final String board="com.zxhl.gpsking.MYBROADCAST";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(board)){
                state2=true;
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

    //定义接口
    public interface ShowAct{
        public void callBack(int result);
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

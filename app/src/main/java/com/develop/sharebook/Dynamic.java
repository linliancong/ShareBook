package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.develop.entity.BookInfo;
import com.develop.entity.LiabaryInfo;
import com.develop.util.AdapterUtil;
import com.develop.util.SharedPreferenceUtils;
import com.develop.util.StatusBarUtil;
import com.develop.util.database.SqlOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018/3/9.
 */

public class Dynamic extends Fragment{

    private AdapterUtil adapter;
    private SharedPreferenceUtils sp;
    private SqlOperator op;

    private Context context;
    private View view;

    private BookInfo book;
    private ArrayList<BookInfo> books;
    private GetBookInfo getBookInfo;
    private ListView list;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 0x001:
                    break;
                case 0x002:
                    //扫描插入数据
                    /*Intent it=new Intent(context,BookInfoView.class);
                    Bundle bd=new Bundle();
                    ArrayList<BookInfo> book1=new ArrayList<>();
                    book1.add(book);
                    bd.putSerializable("book",book1);
                    it.putExtras(bd);
                    startActivity(it);*/
                    setAdapter();
                    Toast.makeText(context,"获取推荐数据成功",Toast.LENGTH_SHORT).show();
                    break;
                case 0x003:
                    //手动插入数据
                    /*Intent it2=new Intent(context,BookInfoView.class);
                    Bundle bd2=new Bundle();
                    ArrayList<BookInfo> book2=new ArrayList<>();
                    book2.add(book);
                    bd2.putSerializable("book",book2);
                    it2.putExtras(bd2);
                    startActivity(it2);*/
                    Toast.makeText(context,"获取推荐数据失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public Dynamic(){}
    @SuppressLint("ValidFragment")
    public Dynamic(Context context){
        this.context=context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.dynamic, container, false);
            sp=new SharedPreferenceUtils(context);
            op=new SqlOperator(context);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getBookInfo = new GetBookInfo(context);
                        books = getBookInfo.fetchBookInfoByJSON();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                }).start();

        }
        return view;
    }

    private void setAdapter() {
        adapter=new AdapterUtil<BookInfo>(books, R.layout.liabary_show_item){
            @Override
            public void bindView(ViewHolder holder, BookInfo obj) {
                /*holder.setText(R.id.library_item_name,obj.getName());
                holder.setText(R.id.library_item_count,obj.getCount());*/
            }
        };

        list.setAdapter(adapter);
    }

    // 通过豆瓣获取图书信息
    public class GetBookInfo {


        public static final String ISBN_URL = "https://api.douban.com/v2/book/search?tag=新书推荐"; // 返回来的是JSON的编码信息
        public static final int RETURN_BOOKINFO_STATUS = 200;	//返回图书信息
        public static final int BOOK_NOT_FOUND_STATUS = 404;	//图书不存在
        private Context context;

        private File file;


        public GetBookInfo(Context context){
            this.context=context;
        }
        /**
         * 从根据isbn号从豆瓣获取数据
         *
         * @param
         * @return
         * @throws IOException
         */
        public ArrayList<BookInfo> fetchBookInfoByJSON() throws IOException {
            String requestUrl = ISBN_URL;
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();
            if(conn.getResponseCode() == RETURN_BOOKINFO_STATUS) {
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                return readBookInfo(sb.toString());
            }
            if(conn.getResponseCode() == BOOK_NOT_FOUND_STATUS) {
                handler.sendEmptyMessage(0x003);
            }

            return null;
        }

        // 读取获得的数据
        public  ArrayList<BookInfo> readBookInfo(String jsonStr) {
            JSONObject jsonObject;
            ArrayList<BookInfo> bookInfos=new ArrayList<>();
            BookInfo bookInfo = new BookInfo();

            try {
                JSONObject jsonObject1 = new JSONObject(jsonStr);
                JSONArray jsonArray=jsonObject1.getJSONArray("books");
                for (int i=0;i<jsonArray.length();i++) {
                    jsonObject=jsonArray.getJSONObject(i);
                    bookInfo.setTitle(jsonObject.getString("title"));
                    bookInfo.setAuthor(parseJSONArraytoString(jsonObject.getJSONArray("author")));
                    bookInfo.setTags(parseJSONArraytoString2(jsonObject.getJSONArray("tags")));
                    bookInfo.setPublisher(jsonObject.getString("publisher"));

                    DownloadBitmap(jsonObject.getString("image"));
                    saveImage(DownloadBitmap(jsonObject.getString("image")), jsonObject.getString("isbn13"));
                    bookInfo.setImagePath(file.toString());

                    bookInfo.setPrice(jsonObject.getString("price"));
                    bookInfo.setSummary(jsonObject.getString("summary"));
                    bookInfo.setIsbn13(jsonObject.getString("isbn13"));
                    bookInfos.add(bookInfo);
                }
            } catch (Exception e) {
                handler.sendEmptyMessage(0x003);
                e.printStackTrace();
            }
            handler.sendEmptyMessage(0x002);
            return bookInfos;
        }

        /**
         * 下载图片
         */
        public Bitmap DownloadBitmap(String bitmapUrl) throws Exception{
            Bitmap bitmap = null;
            BufferedInputStream bis = null;

            URL url = new URL(bitmapUrl);
            URLConnection conn = url.openConnection();
            bis = new BufferedInputStream(conn.getInputStream());
            bitmap = BitmapFactory.decodeStream(bis);

            if (bis != null)
                bis.close();

            return bitmap;
        }

        public String parseJSONArraytoString(JSONArray array) throws Exception{
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < array.length(); i++) {
                str = str.append(array.getString(i)).append(";");
            }
            return str.toString();
        }

        public String parseJSONArraytoString2(JSONArray array) throws Exception {
            StringBuffer str = new StringBuffer();

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = (JSONObject) array.get(i);
                /*for (int j = 0; j < jsonObject.length(); j++) {*/
                str = str.append(jsonObject.getString("name")).append(";");
                //}
            }
            return str.toString();
        }

        /**
         * 保存图片
         */
        private void saveImage(Bitmap bitmap,String isbnNo) throws Exception{
            File filesDir;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
                //路径1：storage/sdcard/Android/data/包名/files
                filesDir = context.getExternalFilesDir("");
            }else{//手机内部存储
                //路径2：data/data/包名/files
                filesDir = context.getFilesDir();
            }
            FileOutputStream fos = null;
            file = new File(filesDir,isbnNo+".png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos);
            if(fos != null){
                fos.close();
            }
        }

    }
}

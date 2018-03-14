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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
                    setAdapter();
                    //Toast.makeText(context,"获取推荐数据成功",Toast.LENGTH_SHORT).show();
                    break;
                case 0x003:
                    Toast.makeText(context,"没有新动态",Toast.LENGTH_SHORT).show();
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
            list=view.findViewById(R.id.dynamic_list);
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
        adapter=new AdapterUtil<BookInfo>(books, R.layout.dynamic_item){
            @Override
            public void bindView(ViewHolder holder, BookInfo obj) {
                holder.setImageBitmap(R.id.dynamic_img,readImage(obj.getImagePath()));
                holder.setText(R.id.dynamic_name,obj.getTitle());
                holder.setText(R.id.dynamic_author,obj.getAuthor());
                holder.setText(R.id.dynamic_summary,obj.getSummary());
            }
        };

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it=new Intent(context,DynamicInfo.class);
                Bundle bd=new Bundle();
                BookInfo book1=books.get(position);
                ArrayList<BookInfo> bookInfos=new ArrayList<>();
                bookInfos.add(book1);
                bd.putSerializable("book",bookInfos);
                it.putExtras(bd);
                startActivity(it);
            }

        });
    }

    /**
     * 读取图片
     * */
    private Bitmap readImage(String path) {
        File file = new File(path);
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            //img.setImageBitmap(bitmap);
            return bitmap;
        }
        return null;
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

            try {
                JSONObject jsonObject1 = new JSONObject(jsonStr);
                JSONArray jsonArray=jsonObject1.getJSONArray("books");
                for (int i=0;i<jsonArray.length();i++) {
                    jsonObject=jsonArray.getJSONObject(i);
                    BookInfo bookInfo = new BookInfo();
                    bookInfo.setTitle(jsonObject.getString("title"));
                    bookInfo.setAuthor(parseJSONArraytoString(jsonObject.getJSONArray("author")));
                    bookInfo.setTags(parseJSONArraytoString2(jsonObject.getJSONArray("tags")));
                    bookInfo.setPublisher(jsonObject.getString("publisher"));

                    //bookInfo.setImage(DownloadBitmap(jsonObject.getString("image")));
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

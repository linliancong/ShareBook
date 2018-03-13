package com.develop.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.develop.entity.BookInfo;
import org.json.JSONArray;
import org.json.JSONException;
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


/**
 * Created by Administrator on 2018/3/12.
 */

public class GetBookInfo {

    public static final String TAG = GetBookInfo.class.getSimpleName();
    // 通过豆瓣获取图书信息
    public static final String ISBN_URL = "https://api.douban.com/v2/book/isbn/"; // 返回来的是JSON的编码信息
    public static final int RETURN_BOOKINFO_STATUS = 200;	//返回图书信息
    public static final int BOOK_NOT_FOUND_STATUS = 404;	//图书不存在
    public static int Result=0;
    private Context context;


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
    public BookInfo fetchBookInfoByXML(String isbnNo) throws IOException {
        String requestUrl = ISBN_URL + isbnNo;
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

        return null;
    }

    // 读取获得的数据
    public  BookInfo readBookInfo(String jsonStr) {
        JSONObject jsonObject;
        String nodeInfo = null;
        BookInfo bookInfo = new BookInfo();

        try {
            jsonObject = new JSONObject(jsonStr);
            bookInfo.setTitle(jsonObject.getString("title"));
            bookInfo.setAuthor(parseJSONArraytoString(jsonObject.getJSONArray("author")));
            bookInfo.setTags(parseJSONArraytoString2(jsonObject.getJSONArray("tags")));
            bookInfo.setPublisher(jsonObject.getString("publisher"));
            DownloadBitmap(jsonObject.getString("image"));
            saveImage(DownloadBitmap(jsonObject.getString("image")),jsonObject.getString("isbn13"));
            bookInfo.setPrice(jsonObject.getString("price"));
            bookInfo.setSummary(jsonObject.getString("summary"));
            bookInfo.setIsbn13(jsonObject.getString("isbn13"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Result=1;
        return bookInfo;
    }

    /**
     * 下载图片
     */
    public Bitmap DownloadBitmap(String bitmapUrl) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;

        try {
            URL url = new URL(bitmapUrl);
            URLConnection conn = url.openConnection();
            bis = new BufferedInputStream(conn.getInputStream());
            bitmap = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    public String parseJSONArraytoString(JSONArray array) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < array.length(); i++) {
            try {
                str = str.append(array.getString(i)).append(";");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str.toString();
    }

    public String parseJSONArraytoString2(JSONArray array) {
        StringBuffer str = new StringBuffer();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = (JSONObject) array.get(i);
                for (int j = 0; j < jsonObject.length(); j++) {
                    str = str.append(jsonObject.getString("name")).append(";");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    /**
     * 保存图片
     */
    private void saveImage(Bitmap bitmap,String isbnNo) {
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
            File file = new File(filesDir,isbnNo+".png");
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
    /*private boolean readImage() {
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){//判断sd卡是否挂载
            //路径1：storage/sdcard/Android/data/包名/files
            filesDir = context.getExternalFilesDir("");
        }else{//手机内部存储
            //路径2：data/data/包名/files
            filesDir = context.getFilesDir();
        }
        File file = new File(filesDir,isbnNo+".png");
        if(file.exists()){
            //存储--->内存
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            //me_img_tx.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }*/
}

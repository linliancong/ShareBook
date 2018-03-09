package com.develop.sharebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2018/3/9.
 */

public class Message extends Fragment{

    private Context context;
    private View view;

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
        }
        return view;
    }
}

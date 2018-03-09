package com.develop.util;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.develop.sharebook.About;
import com.develop.sharebook.Dynamic;
import com.develop.sharebook.HomePage;
import com.develop.sharebook.Liarary;
import com.develop.sharebook.Message;
import com.develop.sharebook.PersonInfo;
import com.develop.sharebook.State;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGER_COUNT=6;
    private Context context;

    private PersonInfo personInfo=null;
    private Dynamic dynamic=null;
    private Message message=null;
    private Liarary liarary=null;
    private State state=null;
    private About about=null;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context=context;

        personInfo=new PersonInfo(context);
        dynamic=new Dynamic(context);
        message=new Message(context);
        liarary=new Liarary(context);
        state=new State(context);
        about=new About(context);

    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position)
        {
            case HomePage.PAG_ONE:
                fragment=personInfo;
                break;
            case HomePage.PAG_TWO:
                fragment=dynamic;
                break;
            case HomePage.PAG_THREE:
                fragment=message;
                break;
            case HomePage.PAG_FOUR:
                fragment=liarary;
                break;
            case HomePage.PAG_FIVE:
                fragment=state;
                break;
            case HomePage.PAG_SIX:
                fragment=about;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}

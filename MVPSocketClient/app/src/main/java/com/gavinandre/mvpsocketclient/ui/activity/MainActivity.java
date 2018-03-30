package com.gavinandre.mvpsocketclient.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.gavinandre.mvpsocketclient.R;
import com.gavinandre.mvpsocketclient.mvp.base.AbstractMvpActivity;
import com.gavinandre.mvpsocketclient.mvp.presenter.MainPresenter;
import com.gavinandre.mvpsocketclient.mvp.view.IMainView;
import com.gavinandre.mvpsocketclient.ui.fragment.BaseFragment;
import com.gavinandre.mvpsocketclient.ui.fragment.TestFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by gavinandre on 18-1-8.
 */
public class MainActivity extends AbstractMvpActivity<IMainView, MainPresenter> implements
        IMainView, BaseFragment.IMainCallBack {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationBar mBottomNavigationBar;

    private TestFragment fragment1;
    private TestFragment fragment2;
    private TestFragment fragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
        initView();
    }

    private void initView() {
        fragment1 = TestFragment.newInstance("123");
        fragment2 = TestFragment.newInstance("321");
        fragment3 = TestFragment.newInstance("123321");
        mBottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.SimpleOnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                setScrollableText(position);
            }
        });
        mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.mipmap.ic_location_on_white_24dp, "Nearby").setActiveColorResource(R.color.orange))
                .addItem(new BottomNavigationItem(R.mipmap.ic_find_replace_white_24dp, "Find").setActiveColorResource(R.color.teal))
                .addItem(new BottomNavigationItem(R.mipmap.ic_favorite_white_24dp, "Categories").setActiveColorResource(R.color.blue))
                .initialise();
        setScrollableText(0);
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showData(String s) {
        Log.i(TAG, "showData: " + s);
        //runOnUiThread(() -> Toast.makeText(this, s, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showMessage(String msg) {
        Log.i(TAG, "showMessage: " + msg);
        //runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void dismiss() {
    }

    @Override
    public <T> void sendData(T data) {
        getPresenter().sendData(data);
    }

    private void setScrollableText(int position) {
        switch (position) {
            case 0:
                getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_frag_container, fragment1).commitAllowingStateLoss();
                break;
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_frag_container, fragment2).commitAllowingStateLoss();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_frag_container, fragment3).commitAllowingStateLoss();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.home_activity_frag_container, fragment1).commitAllowingStateLoss();
                break;
        }
    }
}

package com.gavinandre.mvpsocketclient.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.gavinandre.mvpsocketclient.R;
import com.gavinandre.mvpsocketclient.bean.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by gavinandre on 18-3-18.
 */

public class ChildActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        EventBus.getDefault().post(new MessageEvent("ChildActivity"));
    }
}

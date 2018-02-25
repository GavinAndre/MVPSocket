package com.gavinandre.mvpsocketclient.ui.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by gavinandre on 18-2-21.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Activity 供Fragment回调的接口
     */
    public interface IMainCallBack {
        <T> void sendData(T data);
    }
}

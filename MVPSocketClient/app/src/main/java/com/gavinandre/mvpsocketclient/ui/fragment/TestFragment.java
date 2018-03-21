package com.gavinandre.mvpsocketclient.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gavinandre.mvpsocketclient.R;
import com.gavinandre.mvpsocketclient.mvp.base.AbstractMvpFragment;
import com.gavinandre.mvpsocketclient.mvp.presenter.TestPresenter;
import com.gavinandre.mvpsocketclient.mvp.view.ITestView;
import com.gavinandre.mvpsocketclient.ui.activity.ChildActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by gavinandre on 18-2-21.
 */
public class TestFragment extends AbstractMvpFragment<ITestView, TestPresenter> implements
        ITestView {

    private static final String TAG = TestFragment.class.getSimpleName();
    private static final String KEY_MESSAGE = "message";
    Unbinder unbinder;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    private String msg;
    //private IMainCallBack activityCallBack;

    public static TestFragment newInstance(String message) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    //@Override
    //public void onAttach(Context context) {
    //    super.onAttach(context);
    //    try {
    //        activityCallBack = (IMainCallBack) context;
    //    } catch (ClassCastException e) {
    //        throw new ClassCastException(context.toString()
    //                + " must implement activityCallBack");
    //    }
    //}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            msg = (String) getArguments().getSerializable(KEY_MESSAGE);
        }
        Log.i(TAG, "onCreate: " + msg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        Log.i(TAG, "onCreateView: " + msg);
        return view;
    }

    @NonNull
    @Override
    public TestPresenter createPresenter() {
        return new TestPresenter(this);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismiss() {

    }

    @Override
    public void showData(String s) {
        Toast.makeText(getContext(), msg + "," + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(String s) {
        Toast.makeText(getContext(), msg + "," + s, Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        String text = mBtnSend.getText() + msg;
        mBtnSend.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: " + msg);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: " + msg);
    }

    @OnClick({R.id.btn_send, R.id.btn_child_activity})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                //activityCallBack.sendData(msg);
                getPresenter().sendData(msg);
                break;
            case R.id.btn_child_activity:
                startActivity(new Intent(getContext(), ChildActivity.class));
            default:
                break;
        }
    }

}

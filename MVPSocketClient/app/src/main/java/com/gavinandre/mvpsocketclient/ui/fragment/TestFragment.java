package com.gavinandre.mvpsocketclient.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gavinandre.mvpsocketclient.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by gavinandre on 18-2-21.
 */
public class TestFragment extends BaseFragment {

    private static final String KEY_MESSAGE = "message";
    Unbinder unbinder;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    private String msg;
    private IMainCallBack activityCallBack;

    public static TestFragment newInstance(String message) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityCallBack = (IMainCallBack) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement activityCallBack");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            msg = (String) getArguments().getSerializable(KEY_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        String text = mBtnSend.getText() + msg;
        mBtnSend.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                activityCallBack.sendData(msg);
                break;
            default:
                break;
        }
    }

}

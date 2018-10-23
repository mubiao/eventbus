package com.eskyray.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eskyray.im.R;
import com.eskyray.im.server.utils.PreferencesUtils;
import com.eskyray.im.server.utils.SmackUtils;
import com.eskyray.im.ui.activity.AboutActivity;
import com.eskyray.im.ui.activity.LoginActivity;
import com.eskyray.im.ui.widget.CustomPopWindow;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 设置页
 */

public class SettingFragment extends Fragment {
    private Unbinder unbinder;
    private Context mContext;//上下文

    @BindView(R.id.rl_app_exit)
    RelativeLayout exit;//退出

    @BindView(R.id.name)
    TextView name;//昵称

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);
        initName();
        return view;
    }

    private void initName() {
        //设置昵称
        name.setText(PreferencesUtils.getInstance().getString("username"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.rl_about, R.id.rl_app_exit})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_about:
                Intent intent = new Intent(mContext, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_app_exit:
                View popview = LayoutInflater.from(mContext).inflate(R.layout.pop_exit, null);
                //创建并显示popWindow
                CustomPopWindow popWindow = new CustomPopWindow.PopupWindowBuilder(mContext)
                        .setView(popview)
                        .setAnimationStyle(android.R.style.Animation_InputMethod)
                        .create()
                        .showAtLocation(exit, Gravity.BOTTOM, 0, 0);
                initPop(popWindow, popview);
                break;
        }

    }

    public void initPop(final CustomPopWindow popWindow, View popview) {
        TextView exit = (TextView) popview.findViewById(R.id.exit);//退出
        TextView cancel = (TextView) popview.findViewById(R.id.cancel);//取消
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                /**
                 * 断开连接逻辑
                 */
                SmackUtils.getInstance().exitConnect();
                Intent intent = new Intent(mContext, LoginActivity.class);
                mContext.startActivity(intent);
                getActivity().finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popWindow.dissmiss();
            }
        });
    }
}

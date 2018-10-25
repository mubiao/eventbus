package com.eskyray.im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.eskyray.im.R;
import com.eskyray.im.manager.ConnectionManager;
import com.eskyray.im.server.utils.PreferencesUtil;
import com.eskyray.im.server.utils.ReFreshDataUtil;
import com.eskyray.im.server.utils.ToastUtil;
import com.eskyray.im.server.utils.XMPPUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateGroupActivity extends Activity {
    private Context myContext;

    @BindView(R.id.new_group_name)
    EditText groupName;
    @BindView(R.id.new_group_password)
    EditText password;

    private String name;
    private String pwd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        myContext = this;
    }

    private void createGroup() {
        final String username = PreferencesUtil.getSharedPreStr(myContext, "username");
        name = groupName.getText().toString().trim();
        pwd = password.getText().toString().trim();
        //判断账号密码是否非空
        if (TextUtils.isEmpty(name)) {
            ToastUtil.showShortToast(myContext, "群名为空");
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            ToastUtil.showShortToast(myContext, "密码为空");
            return;
        }

        if (XMPPUtil.createGroup(ConnectionManager.getConnection(), name, username, pwd, myContext)) {
            ToastUtil.showShortToast(myContext, "创建群成功");
            ReFreshDataUtil.reFreshGroupList(name);
            finish();
        } else
            ToastUtil.showShortToast(myContext, "群名已存在或其他问题导致创建群失败");
    }


    @OnClick({R.id.create_group_go_back, R.id.create_group})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_go_back:
                finish();
                break;
            case R.id.create_group:
                createGroup();
                break;
        }
    }


}

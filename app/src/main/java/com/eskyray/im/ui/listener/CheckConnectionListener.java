package com.eskyray.im.ui.listener;

import com.eskyray.im.server.XMPPService;
import com.eskyray.im.server.utils.ToastUtil;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

/**
 * Created by wtr on 2017/6/29.
 */

//主要为了处理账号在别处重复登录
public class CheckConnectionListener implements ConnectionListener {

    private XMPPService context;

    public CheckConnectionListener(XMPPService context){
        this.context=context;
    }

    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        if (e.getMessage().equals("stream:error (conflict)")) {
            ToastUtil.showLongToast(context, "您的账号在异地登录");
        }
    }

    @Override
    public void reconnectingIn(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reconnectionSuccessful() {
        // TODO Auto-generated method stub

    }

}


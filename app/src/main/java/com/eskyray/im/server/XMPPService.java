package com.eskyray.im.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.eskyray.im.Const;
import com.eskyray.im.application.MyApplication;
import com.eskyray.im.manager.ConnectionManager;
import com.eskyray.im.server.utils.PreferencesUtil;
import com.eskyray.im.server.utils.ReFreshDataUtil;
import com.eskyray.im.ui.beans.PeopleItem;
import com.eskyray.im.ui.listener.CheckConnectionListener;
import com.eskyray.im.ui.listener.FriendsPacketListener;
import com.eskyray.im.ui.listener.MyChatManagerListener;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by wtr on 2017/6/29.
 */

public class XMPPService extends Service {
    private ConnectionManager xmppConnectionManager;
    private XMPPConnection xmppConnection;
    private MyChatManagerListener myChatManagerListener;
    private CheckConnectionListener checkConnectionListener;
    private FriendsPacketListener friendsPacketListener;
    private String username;
    private String password;
    private Context myContext;

    private final IBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public XMPPService getService() {
            return XMPPService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myContext = this;
        initXMPPTask();
    }

    //开线程初始化XMPP和完成后台登录
    private void initXMPPTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initXMPP();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //初始化XMPP
    private void initXMPP() throws XMPPException {
        xmppConnection = ConnectionManager.getConnection();
        doLogin();
        //添加信息监听
        ChatManager chatManager = ChatManager.getInstanceFor(xmppConnection);
        myChatManagerListener = new MyChatManagerListener(this);
        chatManager.addChatListener(myChatManagerListener);
    }

    //登录
    private void doLogin() throws XMPPException {
        AbstractXMPPConnection connection = ConnectionManager.getConnection();//连接
        try {
            if (checkConnectionListener != null) {
                xmppConnection.removeConnectionListener(checkConnectionListener);
                checkConnectionListener = null;
            }
        } catch (Exception e) {

        }

        try {
            //如果登录成功
            if (xmppConnection.isAuthenticated()) {
                MyApplication.setXmppService(this);
                //设置连接
                MyApplication.xmppConnection = xmppConnection;
                //设置用户
                PeopleItem peopleItem = new PeopleItem();
                peopleItem.setName(username);
                MyApplication.setUser(peopleItem);

                //添加XMPP连接监听
                checkConnectionListener = new CheckConnectionListener(this);
                xmppConnection.addConnectionListener(checkConnectionListener);
                //注册好友状态监听
                friendsPacketListener = new FriendsPacketListener(this);
                xmppConnection.addPacketListener(friendsPacketListener, null);
                //更新数据
                ReFreshDataUtil.reFreshPeopleList();
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
        }


    }

    public XMPPService getThisService() {
        return this;
    }

    @Override
    public void onDestroy() {
        if (myChatManagerListener != null) {
            ChatManager.getInstanceFor(xmppConnection).removeChatListener(myChatManagerListener);
        }

        if (xmppConnection != null) {
            ConnectionManager.release();
            xmppConnection = null;
        }

        if (xmppConnectionManager != null) {
            xmppConnectionManager = null;
        }

        super.onDestroy();
    }


}

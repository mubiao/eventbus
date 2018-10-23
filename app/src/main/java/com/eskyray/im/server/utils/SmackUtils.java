package com.eskyray.im.server.utils;

import android.util.Log;

import com.eskyray.im.Const;
import com.eskyray.im.application.MyApplication;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 封装 Smack 常用方法
 */
public class SmackUtils {

    private static SmackUtils smackUtils;

    public static SmackUtils getInstance() {
        if (smackUtils == null) {
            smackUtils = new SmackUtils();
        }
        return smackUtils;
    }

    /**
     * 建立连接
     */
    public void getXMPPConnection() {
        if (MyApplication.xmppConnection == null || !MyApplication.xmppConnection.isConnected()) {
            XMPPTCPConnectionConfiguration builder = XMPPTCPConnectionConfiguration.builder()
                    .setHost(Const.IM_HOST)//ip
                    .setPort(Const.IM_PORT)//端口
                    .setServiceName(Const.IM_SERVER)//此处填写openfire服务器名称
                    .setCompressionEnabled(false)//是否允许使用压缩
                    .setSendPresence(true)//是否发送Presece信息
                    .setDebuggerEnabled(true)//是否开启调试
                    .setResource("Android")//设置登陆设备标识
                    .setConnectTimeout(15 * 1000)//连接超时时间
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)//设置TLS安全模式时使用的连接
                    .build();
            // 是否使用SASL
            SASLAuthentication.blacklistSASLMechanism(SASLMechanism.DIGESTMD5);
            MyApplication.xmppConnection  = new XMPPTCPConnection(builder);
        }
    }

    /**
     * 检查连接
     */
    private void checkConnect() {
        if (MyApplication.xmppConnection == null) {//null
            getXMPPConnection();
        }
        if (!MyApplication.xmppConnection.isConnected()) {//没有连接到服务器
        }
    }

    /**
     * 断开连接
     */
    public void exitConnect() {
        if (MyApplication.xmppConnection != null && MyApplication.xmppConnection.isConnected()) {
            MyApplication.xmppConnection = null;
        }
    }


    /**
     * 检查登录
     */
    private void checkLogin() {
        if (!MyApplication.xmppConnection.isAuthenticated()) {//没有连接到服务器
        }
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     */
    public boolean register(String username, String password) {
        try {
            checkConnect();
            Map<String, String> map = new HashMap<String, String>();
            map.put("phone", "Android");
            AccountManager accountManager = AccountManager.getInstance(MyApplication.xmppConnection);
            //敏感操作跳过不安全的连接
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(username, password, map);
        } catch (SmackException | XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */

    public boolean login(String username, String password) {
            checkConnect();
            if (MyApplication.xmppConnection.isAuthenticated()) {//已经登录
                return true;
            } else {
            }

        return false;
    }


    /**
     * 发送消息
     *
     * @param message
     * @param to
     */
    public void sendMessage(String message, String to) {
        try {
            checkConnect();
            checkLogin();
            ChatManager mChatManager = ChatManager.getInstanceFor(MyApplication.xmppConnection);
            Chat mChat = mChatManager.createChat(to + "@" + Const.IM_HOST);
            mChat.sendMessage(message);
            mChat.close();
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加好友
     *
     * @param userName
     */
    public void addFriend(String userName) {
        try {
            checkConnect();
            checkLogin();
            Roster roster = Roster.getInstanceFor(MyApplication.xmppConnection);
            roster.createEntry(userName, userName, null);
        } catch (SmackException | XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除好友
     *
     * @param userJID
     * @return
     */
    public void deleteFriend(String userJID) {
        try {
            checkConnect();
            checkLogin();
            Roster roster = Roster.getInstanceFor(MyApplication.xmppConnection);
            roster.removeEntry(roster.getEntry(userJID));
        } catch (SmackException | XMPPException e) {
            e.printStackTrace();
        }
    }
}

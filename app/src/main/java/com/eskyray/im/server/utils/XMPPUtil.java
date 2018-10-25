package com.eskyray.im.server.utils;

import android.content.Context;

import com.eskyray.im.Const;
import com.eskyray.im.application.MyApplication;
import com.eskyray.im.ui.beans.GroupItem;
import com.eskyray.im.ui.beans.PeopleItem;
import com.eskyray.im.ui.listener.GroupMessageListener;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wtr on 2017/6/29.
 */

//Asmack相关工具
public class XMPPUtil {

    //注册
    public static int register(XMPPConnection xmppConnection, String username, String password) {
        Registration reg = new Registration();
        reg.setType(IQ.Type.set); //设置类型
        reg.setTo(xmppConnection.getServiceName()); //发送到哪
        StanzaFilter filter = new AndFilter();
        PacketCollector collector = xmppConnection.createPacketCollector(filter);   //创建包收集器，用来获取返回结果
        IQ result = (IQ) collector.nextResult(SmackConfiguration.getDefaultPacketReplyTimeout());  //获取返回结果
        // Stop queuing results停止请求results（是否成功的结果）
        collector.cancel();
        if (result == null) {
            return 0;   //服务器没结果
        } else if (result.getType() == IQ.Type.result) {
            return 1;  //注册成功
        } else {
            if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
                return 2;   //账号已存在
            } else {
                return 3;  //注册失败
            }
        }
    }

    //搜索用户
    public static List<PeopleItem> searchUsers(XMPPConnection mXMPPConnection, String userName) {
        List<PeopleItem> listUser = new ArrayList<PeopleItem>();
        try {
            UserSearchManager search = new UserSearchManager(mXMPPConnection);
            Form searchForm = search.getSearchForm("search." + mXMPPConnection.getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);   //把某个字段设成true就会在那个字段里搜索关键字，search字段设置要搜索的关键字，什么不输入不会返回。
            answerForm.setAnswer("search", userName);
            ReportedData data = search.getSearchResults(answerForm, "search." + mXMPPConnection.getServiceName());
            Iterator<ReportedData.Row> it = data.getRows().iterator();
            ReportedData.Row row = null;
            while (it.hasNext()) {
                row = it.next();
                PeopleItem peopleItem = new PeopleItem();
                peopleItem.setName(row.getValues("Username").toString());
                listUser.add(peopleItem);
            }
        } catch (Exception e) {

        }
        return listUser;
    }

    //发送好友申请
    public static void applyForFriend(XMPPConnection xmppConnection, String username, String message) throws SmackException.NotConnectedException {
        Presence subscription = new Presence(Presence.Type.subscribe);
        subscription.setTo(username + "@" + xmppConnection.getServiceName());  //接收方
        subscription.setFrom(xmppConnection.getUser());  //发送方
        subscription.setStatus(message);  //消息
        xmppConnection.sendStanza(subscription);   //发送
    }

    //发送同意好友申请
    public static void agreeApplyForFriend(XMPPConnection xmppConnection, String username) throws SmackException.NotConnectedException {
        //发送同意添加
        Presence subscription = new Presence(Presence.Type.subscribed);
        subscription.setTo(username + "@" + xmppConnection.getServiceName());
        xmppConnection.sendPacket(subscription);
        //再发送到之前的发送方，让其同意
        applyForFriend(xmppConnection, username, Const.AGREE_FRIEND);
    }

    //返回所有用户信息
    public static List<RosterEntry> getAllEntries(XMPPConnection xmppConnection) {
        return null;
    }

    //判断是否为好友
    public static boolean isFriend(XMPPConnection xmppConnection, String username) {
        return false;
    }

    //发送消息
    public static void sendMessage(XMPPConnection xmppConnection, String message, String toUser) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            return;
        }
        ChatManager chatmanager = ChatManager.getInstanceFor(xmppConnection);
        Chat chat = chatmanager.createChat(toUser + "@" + xmppConnection.getServiceName(), null);
        if (chat != null) {
            try {
                chat.sendMessage(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    //创建群
    public static boolean createGroup(XMPPConnection xmppConnection, String groupName, String username,
                                      String password, Context context) {
        if (xmppConnection == null || !xmppConnection.isConnected()) {
            ToastUtil.showLongToast(context, "服务器未连接");
            return false;
        }

        boolean result = false;
        MultiUserChat multiUserChat;
        try {
            multiUserChat = MultiUserChatManager.getInstanceFor(xmppConnection).getMultiUserChat(groupName + "@conference." + xmppConnection.getServiceName());
            multiUserChat.create(username);       // 用户在用户群中的昵称
            Form form = multiUserChat.getConfigurationForm();   //获得聊天室的配置表单
            Form submitForm = form.createAnswerForm();          //根据原始表单创建一个要提交的新表单
            for (Iterator<?> fields = form.getFields().iterator(); fields.hasNext(); ) {
                FormField field = (FormField) fields.next();
                if (!FormField.Type.hidden.equals(field.getType())
                        && field.getVariable() != null) {
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            List<String> list = new ArrayList<String>();
            // 设置聊天室的新拥有者
            List<String> owners = new ArrayList<String>();
            owners.add(xmppConnection.getUser());// 用户JID
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            list.add("30");
            submitForm.setAnswer("muc#roomconfig_maxusers", list); // 最大用户
            submitForm.setAnswer("muc#roomconfig_persistentroom", true); // 房间永久
            submitForm.setAnswer("muc#roomconfig_membersonly", false); // 仅对成员开放
            submitForm.setAnswer("muc#roomconfig_allowinvites", true); // 允许邀请
            submitForm.setAnswer("muc#roomconfig_enablelogging", true); // 登陆房间对话
            if (password != null) {
                submitForm.setAnswer("muc#roomconfig_roomsecret", password);// 设置密码
                submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
                        true);// 进入房间，密码验证
            }
            submitForm.setAnswer("x-muc#roomconfig_reservednick", true); // 仅允许注册的用户登陆
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", false); // 允许修改昵称
            submitForm.setAnswer("x-muc#roomconfig_registration", false); // 允许用户注册房间
            multiUserChat.sendConfigurationForm(submitForm);
            multiUserChat.join(username, password);

            multiUserChat.addMessageListener(new GroupMessageListener(MyApplication.getXmppService()));
            List<MultiUserChat> multiUserChatList = MyApplication.getMultiUserChatList();
            multiUserChatList.add(multiUserChat);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //查找服务器上的和当前群名类似的群
    public static List<GroupItem> getGroups(XMPPConnection xmppConnection, String groupName) throws XMPPException {
        return null;
    }

    //加入群
    public static boolean enterGroup(XMPPConnection xmppConnection, String username, String groupName, String password) {

        return false;
    }

    public static void sendGroupMessage(XMPPConnection xmppConnection, String groupName, String message) {

    }
}

package com.eskyray.im.application;

import android.app.Application;
import android.content.Context;

import com.eskyray.im.server.XMPPService;
import com.eskyray.im.ui.beans.Conversation;
import com.eskyray.im.ui.beans.GroupItem;
import com.eskyray.im.ui.beans.PeopleItem;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    public static XMPPConnection xmppConnection;
    private List<Conversation> ConversationList = new ArrayList<Conversation>();
    private static MyApplication myApplicationContext;
    private static Context myAppContext = null;
    private List<PeopleItem> PeopleItemList = new ArrayList<PeopleItem>();
    private List<GroupItem> GroupItemList = new ArrayList<GroupItem>();
    private static PeopleItem theUser = new PeopleItem();
    private static XMPPService xmppService;
    private static List<MultiUserChat> multiUserChatList = new ArrayList<>();

    public static MyApplication getMyApplication() {
        return myApplicationContext;
    }

    public List<Conversation> getConversationList() {
        return ConversationList;
    }

    public List<PeopleItem> getPeopleItemList() {
        return PeopleItemList;
    }

    public List<GroupItem> getGroupItemList() {
        return GroupItemList;
    }

    public static PeopleItem getUser() {
        return theUser;
    }

    public static void setUser(PeopleItem people) {
        theUser = people;
    }

    public static void setXmppService(XMPPService service) {
        xmppService = service;
    }

    public static XMPPService getXmppService() {
        return xmppService;
    }

    public static List<MultiUserChat> getMultiUserChatList() {
        return multiUserChatList;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplicationContext = this;
        myAppContext = getApplicationContext();
    }

    public static Context getMyAppContext() {
        return myApplicationContext;
    }
}

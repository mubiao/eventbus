package com.eskyray.im.server.utils;

import com.eskyray.im.application.MyApplication;
import com.eskyray.im.ui.beans.GroupItem;
import com.eskyray.im.ui.beans.PeopleItem;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;

public class ReFreshDataUtil {
    public static void reFreshPeopleList(){
        List<PeopleItem> PeopleItemList = MyApplication.getMyApplication().getPeopleItemList();
        List<RosterEntry> rosterEntryList =  XMPPUtil.getAllEntries(MyApplication.xmppConnection);
        PeopleItemList.clear();
        for(RosterEntry r: rosterEntryList){
            PeopleItem peopleItem = new PeopleItem();
            peopleItem.setName(r.getUser().split("@")[0]);
            PeopleItemList.add(peopleItem);
        }
    }

    //新加群，更新群列表
    public static void reFreshGroupList(String name){
        List<GroupItem> groupItemList = MyApplication.getMyApplication().getGroupItemList();
        GroupItem groupItem = new GroupItem();
        groupItem.setName(name);
        groupItemList.add(groupItem);
    }
}

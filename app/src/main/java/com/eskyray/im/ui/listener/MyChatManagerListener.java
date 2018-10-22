package com.eskyray.im.ui.listener;

import com.eskyray.im.server.XMPPService;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;

/**
 * Created by wtr on 2017/7/1.
 */

public class MyChatManagerListener implements ChatManagerListener {
    XMPPService context;
    public MyChatManagerListener(XMPPService context){
        this.context = context;
    }
    @Override
    public void chatCreated(Chat chat, boolean arg1) {
        chat.addMessageListener(new MsgListener(context));
    }
}

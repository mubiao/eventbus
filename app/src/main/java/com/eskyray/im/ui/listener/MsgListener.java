package com.eskyray.im.ui.listener;

import android.content.Intent;
import android.text.TextUtils;

import com.eskyray.im.Const;
import com.eskyray.im.server.XMPPService;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by wtr on 2017/7/1.
 */

public class MsgListener implements ChatMessageListener {
    XMPPService context;
    public MsgListener(XMPPService context){
        this.context = context;
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        String msgBody = message.getBody();
        //如果消息为空
        if (TextUtils.isEmpty(msgBody))
            return;

        Intent intent=new Intent(Const.ACTION_NEW_MESSAGE);//发送广播到聊天界面
        intent.putExtra("message", msgBody);
        context.sendBroadcast(intent);
    }
}

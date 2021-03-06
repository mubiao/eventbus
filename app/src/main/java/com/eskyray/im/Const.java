package com.eskyray.im;

/**
 * Created by wtr on 2017/6/28.
 */

//所有常量
public class Const {
    //服务器地址、服务器名称、端口
    public static final String IM_HOST = "192.168.31.43";
    public static final String IM_SERVER = "lwb-pc";


    public static final int IM_PORT = 5222;
    public static final String XMPP_HOST = "192.168.31.43";    //OpenFire服务器IP地址
    public static final int XMPP_PORT = 5222;                     //端口号
    public static final String ACTION_IS_LOGIN_SUCCESS = "com.example.wtr.im.is_login_success"; //登录状态广播
    public static final String ACTION_ADD_FRIEND= "com.example.wtr.im.add_friend";   //添加好友广播
    public static final String ACTION_NEW_MESSAGE= "com.example.wtr.im.new_message";   //新消息广播
    public static final String AGREE_FRIEND= "已同意你的好友申请，点击同意完成好友添加";
    public static final String SPLIT="卍"; //消息内容分隔符
}

package com.eskyray.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eskyray.im.R;
import com.eskyray.im.manager.ConnectionManager;
import com.eskyray.im.server.pinin.PinyinComparator;
import com.eskyray.im.server.pinin.SideBar;
import com.eskyray.im.ui.activity.ChatActivity;
import com.eskyray.im.ui.activity.GroupListActivity;
import com.eskyray.im.ui.activity.NewFriendListActivity;
import com.eskyray.im.ui.activity.PublicServiceActivity;
import com.eskyray.im.ui.activity.UserDetailActivity;
import com.eskyray.im.ui.adapter.FriendsExpandableListAdapter;
import com.eskyray.im.ui.beans.FriendInfo;
import com.eskyray.im.ui.widget.SelectableRoundedImageView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * tab 2 通讯录的 Fragment
 * Created by Bob on 2015/1/25.
 */
public class ContactsFragment extends Fragment implements View.OnClickListener, ExpandableListView.OnChildClickListener {

    private SelectableRoundedImageView mSelectableRoundedImageView;
    private TextView mNameTextView;
    private TextView mNoFriends;
    private TextView mUnreadTextView;
    private View mHeadView;
    private EditText mSearchEditText;
    private ExpandableListView mListView;
    private PinyinComparator mPinyinComparator;
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    private TextView mDialogTextView;
    /**
     * 好友列表的 mFriendListAdapter
     */
    private FriendsExpandableListAdapter adapter;
    /**
     * 根据拼音来排列ListView里面的数据类
     */

    private String mId;
    private String mCacheName;

    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_address, container, false);
        initView(view);
        initData();
        updateUI();
        return view;
    }

    private void startFriendDetailsPage() {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
        startActivity(intent);
    }

    private void initView(View view) {
        mSearchEditText = (EditText) view.findViewById(R.id.search);
        mListView = view.findViewById(R.id.listview);
        mNoFriends = (TextView) view.findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) view.findViewById(R.id.sidrbar);
        mDialogTextView = (TextView) view.findViewById(R.id.group_dialog);
        mSidBar.setTextView(mDialogTextView);
        LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
        mHeadView = mLayoutInflater.inflate(R.layout.item_contact_list_header,
                                            null);
        mUnreadTextView = (TextView) mHeadView.findViewById(R.id.tv_unread);
        RelativeLayout newFriendsLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_newfriends);
        RelativeLayout groupLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_chatroom);
        RelativeLayout publicServiceLayout = (RelativeLayout) mHeadView.findViewById(R.id.publicservice);
        RelativeLayout selfLayout = (RelativeLayout) mHeadView.findViewById(R.id.contact_me_item);
        mSelectableRoundedImageView = (SelectableRoundedImageView) mHeadView.findViewById(R.id.contact_me_img);
        mNameTextView = (TextView) mHeadView.findViewById(R.id.contact_me_name);
        mListView.addHeaderView(mHeadView);
        mNoFriends.setVisibility(View.VISIBLE);

        selfLayout.setOnClickListener(this);
        groupLayout.setOnClickListener(this);
        newFriendsLayout.setOnClickListener(this);
        publicServiceLayout.setOnClickListener(this);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {

            }
        });
    }

    private void initData() {
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mDialogTextView != null) {
            mDialogTextView.setVisibility(View.INVISIBLE);
        }
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_newfriends:
                mUnreadTextView.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
                startActivityForResult(intent, 20);
                break;
            case R.id.re_chatroom:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.publicservice:
                Intent intentPublic = new Intent(getActivity(), PublicServiceActivity.class);
                startActivity(intentPublic);
                break;
            case R.id.contact_me_item:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateUI() {
        AbstractXMPPConnection connection = ConnectionManager.getConnection();
        // 获取联系人列表
        Roster roster = Roster.getInstanceFor(connection);
        adapter = new FriendsExpandableListAdapter(getContext(), roster.getGroups());
        mListView.setAdapter(adapter);
        mListView.setOnChildClickListener(this);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
        // 获取被选中好友
        FriendInfo info = (FriendInfo) adapter.getChild(groupPosition, childPosition);

        String chattoJid = info.getJid();

        // 跳转到聊天界面
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chattoJid", chattoJid);
        startActivity(intent);
        return false;
    }
}

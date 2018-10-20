package com.eskyray.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.eskyray.im.R;
import com.eskyray.im.server.pinin.PinyinComparator;
import com.eskyray.im.server.pinin.SideBar;
import com.eskyray.im.server.utils.NToast;
import com.eskyray.im.ui.beans.FriendInfo;
import com.eskyray.im.ui.widget.SelectableRoundedImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectFriendsActivity extends BaseActivity implements View.OnClickListener {

    private static final int ADD_GROUP_MEMBER = 21;
    private static final int DELETE_GROUP_MEMBER = 23;
    public static final String DISCUSSION_UPDATE = "DISCUSSION_UPDATE";
    /**
     * 好友列表的 ListView
     */
    private ListView mListView;
    /**
     * 发起讨论组的 adapter
     */
    private StartDiscussionAdapter adapter;
    /**
     * 中部展示的字母提示
     */
    public TextView dialog;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;
    private TextView mNoFriends;
    private List<FriendInfo> sourceDataList = new ArrayList<>();
    private List<FriendInfo> createGroupList;
    private LinearLayout mSelectedFriendsLinearLayout;
    private boolean isCrateGroup;
    private boolean isConversationActivityStartDiscussion;
    private boolean isConversationActivityStartPrivate;
    private String groupId;
    private String conversationStartId;
    private String conversationStartType = "null";
    private ArrayList<String> discListMember;
    private boolean isStartPrivateChat;
    private boolean isAddGroupMember;
    private boolean isDeleteGroupMember;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_disc);
        Button rightButton = getHeadRightButton();
        rightButton.setVisibility(View.GONE);
        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText("确定");
        mHeadRightText.setOnClickListener(this);
        mSelectedFriendsLinearLayout = (LinearLayout) findViewById(R.id.ll_selected_friends);
        isCrateGroup = getIntent().getBooleanExtra("createGroup", false);
        isConversationActivityStartDiscussion = getIntent().getBooleanExtra("CONVERSATION_DISCUSSION", false);
        isConversationActivityStartPrivate = getIntent().getBooleanExtra("CONVERSATION_PRIVATE", false);
        groupId = getIntent().getStringExtra("GroupId");
        isAddGroupMember = getIntent().getBooleanExtra("isAddGroupMember", false);
        isDeleteGroupMember = getIntent().getBooleanExtra("isDeleteGroupMember", false);
        if (isAddGroupMember || isDeleteGroupMember) {
            initGroupMemberList();
        }

        setTitle();
        initView();

        /**
         * 根据进行的操作初始化数据,添加删除群成员和获取好友信息是异步操作,所以做了很多额外的处理
         * 数据添加后还需要过滤已经是群成员,讨论组成员的用户
         * 最后设置adapter显示
         * 后两个操作全都根据异步操作推后
         */
        initData();
    }

    private void initGroupMemberList() {
    }

    private void setTitle() {
        if (isConversationActivityStartPrivate) {
            conversationStartType = "PRIVATE";
            conversationStartId = getIntent().getStringExtra("DEMO_FRIEND_TARGETID");
            setTitle("选择讨论组成员");
        } else if (isConversationActivityStartDiscussion) {
            conversationStartType = "DISCUSSION";
            conversationStartId = getIntent().getStringExtra("DEMO_FRIEND_TARGETID");
            discListMember = getIntent().getStringArrayListExtra("DISCUSSIONMEMBER");
            setTitle("选择讨论组成员");
        } else if (isDeleteGroupMember) {
            setTitle(getString(R.string.remove_group_member));
        } else if (isAddGroupMember) {
            setTitle(getString(R.string.add_group_member));
        } else if (isCrateGroup) {
            setTitle(getString(R.string.select_group_member));
        } else {
            setTitle(getString(R.string.select_contact));
            if (!getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                isStartPrivateChat = true;
            }
        }
    }

    private void initView() {
        //实例化汉字转拼音类
        pinyinComparator = PinyinComparator.getInstance();
        mListView = (ListView) findViewById(R.id.dis_friendlistview);
        mNoFriends = (TextView) findViewById(R.id.dis_show_no_friend);
        SideBar mSidBar = (SideBar) findViewById(R.id.dis_sidrbar);
        dialog = (TextView) findViewById(R.id.dis_dialog);
        mSidBar.setTextView(dialog);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });
        List<FriendInfo> list = new ArrayList<>();
        FriendInfo friendInfo1 = new FriendInfo();
        friendInfo1.setMood("12332");
        friendInfo1.setName("123424353");
        friendInfo1.setUsername("xffsfsf");
        list.add(friendInfo1);

        adapter = new StartDiscussionAdapter(mContext, list);
        mListView.setAdapter(adapter);
    }

    private void initData() {
        if (true) {
            /**
             * 以下3步是标准流程
             * 1.填充数据sourceDataList
             * 2.过滤数据,邀请新成员时需要过滤掉已经是成员的用户,但做删除操作时不需要这一步
             * 3.设置adapter显示
             */
            fillSourceDataList();
            updateAdapter();
        } else if (!isDeleteGroupMember && !isAddGroupMember) {
        }
    }

    private void fillSourceDataList() {
        mNoFriends.setVisibility(View.VISIBLE);
    }

    //讨论组群组邀请新成员时需要过滤掉已经是成员的用户

    private void updateAdapter() {
        List<FriendInfo> list = new ArrayList<>();
        FriendInfo friendInfo1 = new FriendInfo();
        friendInfo1.setMood("12332");
        friendInfo1.setName("123424353");
        friendInfo1.setUsername("xffsfsf");
        list.add(friendInfo1);
        adapter.setData(list);
        adapter.notifyDataSetChanged();
    }

    private void fillSourceDataListForDeleteGroupMember() {
        fillSourceDataList();
        updateAdapter();
    }


    //用于存储CheckBox选中状态
    public Map<Integer, Boolean> mCBFlag;

    public List<FriendInfo> adapterList;


    class StartDiscussionAdapter extends BaseAdapter implements SectionIndexer {

        private Context context;
        private ArrayList<CheckBox> checkBoxList = new ArrayList<>();

        public StartDiscussionAdapter(Context context, List<FriendInfo> list) {
            this.context = context;
            adapterList = list;
            mCBFlag = new HashMap<>();
            init();
        }

        public void setData(List friends) {
            adapterList = friends;
            init();
        }

        void init() {
            for (int i = 0; i < adapterList.size(); i++) {
                mCBFlag.put(i, false);
            }
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List list) {
            adapterList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder viewHolder;
            final FriendInfo friend = adapterList.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_start_discussion, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.dis_friendname);
                viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.dis_catalog);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.dis_frienduri);
                viewHolder.isSelect = (CheckBox) convertView.findViewById(R.id.dis_select);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(friend.getJid());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }

            if (isStartPrivateChat) {
                viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        if (cb != null) {
                            if (cb.isChecked()) {
                                for (CheckBox c : checkBoxList) {
                                    c.setChecked(false);
                                }
                                checkBoxList.clear();
                                checkBoxList.add(cb);
                            } else {
                                checkBoxList.clear();
                            }
                        }
                    }
                });
                viewHolder.isSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCBFlag.put(position, viewHolder.isSelect.isChecked());
                    }
                });
            } else {
                viewHolder.isSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCBFlag.put(position, viewHolder.isSelect.isChecked());
                        updateSelectedSizeView(mCBFlag);
                    }
                });
            }
            viewHolder.isSelect.setChecked(mCBFlag.get(position));

            if (TextUtils.isEmpty(adapterList.get(position).getUsername())) {
                viewHolder.tvTitle.setText(adapterList.get(position).getName());
            } else {
                viewHolder.tvTitle.setText(adapterList.get(position).getUsername());
            }
            return convertView;
        }

        private void updateSelectedSizeView(Map<Integer, Boolean> mCBFlag) {
            if (!isStartPrivateChat && mCBFlag != null) {
                int size = 0;
                for (int i = 0; i < mCBFlag.size(); i++) {
                    if (mCBFlag.get(i)) {
                        size++;
                    }
                }
                if (size == 0) {
                    mHeadRightText.setText("确定");
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                } else {
                    mHeadRightText.setText("确定(" + size + ")");
                    mSelectedFriendsLinearLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        @Override
        public int getPositionForSection(int sectionIndex) {

            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的Char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return 1;
        }


        final class ViewHolder {
            /**
             * 首字母
             */
            TextView tvLetter;
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userid
             */
//            TextView tvUserId;
            /**
             * 是否被选中的checkbox
             */
            CheckBox isSelect;
        }

    }


    private List<String> startDisList;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListView = null;
        adapter = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_right:
                if (mCBFlag != null) {
                    startDisList = new ArrayList<>();
                    List<String> disNameList = new ArrayList<>();
                    createGroupList = new ArrayList<>();
                    FriendInfo info = new FriendInfo();
                    info.setName("123132123");
                    startDisList.add("143123123");
                    disNameList.add("xsfsf");
                    createGroupList.add(info);
                }

                if (createGroupList.size() > 0) {
                    mHeadRightText.setClickable(true);
                    Intent intent = new Intent(SelectFriendsActivity.this, CreateGroupActivity.class);
                    intent.putExtra("GroupMember", (Serializable) createGroupList);
                    startActivity(intent);
                    finish();
                } else {
                    NToast.shortToast(mContext, "请至少邀请一位好友创建群组");
                    mHeadRightText.setClickable(true);
                }

                break;
        }
    }
}


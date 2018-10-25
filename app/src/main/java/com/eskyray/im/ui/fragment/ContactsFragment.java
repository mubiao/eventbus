package com.eskyray.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eskyray.im.R;
import com.eskyray.im.application.MyApplication;
import com.eskyray.im.manager.ConnectionManager;
import com.eskyray.im.server.pinin.PinyinComparator;
import com.eskyray.im.server.pinin.SideBar;
import com.eskyray.im.ui.activity.ChatActivity;
import com.eskyray.im.ui.activity.CreateGroupActivity;
import com.eskyray.im.ui.activity.FindNewFriendsActivity;
import com.eskyray.im.ui.activity.GroupListActivity;
import com.eskyray.im.ui.activity.NewFriendListActivity;
import com.eskyray.im.ui.activity.PublicServiceActivity;
import com.eskyray.im.ui.activity.ShowConversationActivity;
import com.eskyray.im.ui.activity.ShowPeopleActivity;
import com.eskyray.im.ui.activity.UserDetailActivity;
import com.eskyray.im.ui.adapter.FriendsExpandableListAdapter;
import com.eskyray.im.ui.beans.Conversation;
import com.eskyray.im.ui.beans.FriendInfo;
import com.eskyray.im.ui.beans.GroupItem;
import com.eskyray.im.ui.beans.PeopleItem;
import com.eskyray.im.ui.widget.SelectableRoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.roster.Roster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.webkit.WebSettings.PluginState.ON;

/**
 * tab 2 通讯录的 Fragment
 * Created by Bob on 2015/1/25.
 */
public class ContactsFragment extends Fragment{

    private SelectableRoundedImageView mSelectableRoundedImageView;
    private TextView mNameTextView;
    private TextView mNoFriends;
    private TextView mUnreadTextView;
    private View mHeadView;
    private EditText mSearchEditText;

    @BindView(R.id.simpleListView)
    ListView mListView;
    private PinyinComparator mPinyinComparator;
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    private TextView mDialogTextView;
    /**
     * 好友列表的 mFriendListAdapter
     */
    private ItemAdapter adapter;
    /**
     * 根据拼音来排列ListView里面的数据类
     */

    private String mId;
    private String mCacheName;

    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;

    private View view;
    private List<PeopleItem> peopleItemList;
    private List<GroupItem> groupItemList;

    private int PeopleNum = 0;
    private int GroupNum = 0;

    public static final int ON = 0;
    public static final int CLOSE = 1;

    private int PeopleStage = CLOSE;
    private int GroupStage = CLOSE;

    private DisplayImageOptions options;
    final private ImageLoader imageLoader = ImageLoader.getInstance();

    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_address, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView(view);
        initData();
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        peopleItemList = MyApplication.getMyApplication().getPeopleItemList();
        groupItemList = MyApplication.getMyApplication().getGroupItemList();

        mListView = (ListView) view.findViewById(R.id.simpleListView);

        adapter = new ItemAdapter();

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (position >= 4) {
                    if (position != (4 + PeopleNum)) {
                        if (position < 4 + PeopleNum) {
                            PeopleItem peopleItem = peopleItemList.get(position - 4);

                            Intent intent = new Intent(getActivity(), ShowPeopleActivity.class);
                            intent.putExtra("name", peopleItem.getName());
                            intent.putExtra("area", peopleItem.getArea());
                            intent.putExtra("sex", peopleItem.getSex());
                            intent.putExtra("age", peopleItem.getAge());
                            intent.putExtra("image", peopleItem.getImage());
                            intent.putExtra("produce", peopleItem.getIntroduce());
                            startActivity(intent);
                        } else {
                            GroupItem groupItem = groupItemList.get(position - 5 - PeopleNum);
                            List<Conversation> conversationList = MyApplication.getMyApplication().getConversationList();
                            for (int p = 0; p < conversationList.size(); p++) {
                                if (conversationList.get(p).getGroupConversation() &&
                                        conversationList.get(p).getGroupName().equals(groupItem.getName())) {
                                    Intent intent = new Intent(getActivity(), ShowConversationActivity.class);
                                    intent.putExtra("Name", conversationList.get(p).getName());
                                    intent.putExtra("Position", p);
                                    intent.putExtra("IsGroupConversation", true);
                                    intent.putExtra("GroupName", groupItem.getName());
                                    startActivity(intent);
                                    return;
                                }
                            }
                            Conversation conversation = new Conversation();
                            conversation.setGroupConversation(true);
                            conversation.setGroupName(groupItem.getName());
                            conversation.setName("");
                            conversationList.add(0, conversation);
                            Intent intent = new Intent(getActivity(), ShowConversationActivity.class);
                            intent.putExtra("Name", "");
                            intent.putExtra("Position", 0);
                            intent.putExtra("IsGroupConversation", true);
                            intent.putExtra("GroupName", groupItem.getName());
                            startActivity(intent);

                        }
                    }
                }

            }
        });
    }

    private void startFriendDetailsPage() {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
        startActivity(intent);
    }

    private void initView(View view) {
        mSearchEditText = (EditText) view.findViewById(R.id.search);
        mSidBar = (SideBar) view.findViewById(R.id.sidrbar);
        mDialogTextView = (TextView) view.findViewById(R.id.group_dialog);
        mSidBar.setTextView(mDialogTextView);
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
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                // 是否第一次显示
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    // 图片淡入效果
                    FadeInBitmapDisplayer.animate(imageView, 200);
                    displayedImages.add(imageUri);
                }
            }
        }
    }


    /**
     * 自定义列表项适配器
     */
    class ItemAdapter extends BaseAdapter {
        //图片第一次加载的监听器
        private ImageLoadingListener animateFirstListener =
                new AnimateFirstDisplayListener();

        final int NEWFRIENDS = 0;
        final int CREATE_GROUP = 1;
        final int LINKMAN = 2;
        final int FRIENDS = 3;
        final int GROUPS = 4;
        final int AFRIEND = 5;
        final int AGROUP = 6;

        public ItemAdapter() {
        }

        private class ViewHolder {
            public ImageView controlArrow;

            public TextView count;

            public ImageView portrait;
            public TextView name;
            public TextView age;
            public TextView info;
        }

        @Override
        public int getCount() {
            return (PeopleNum + GroupNum + 5);
        }

        @Override
        public int getViewTypeCount() {
            return 7;
        }

        @Override

        public int getItemViewType(int position) {
            if (position == 0) return NEWFRIENDS;
            else if (position == 1) return CREATE_GROUP;
            else if (position == 2) return LINKMAN;
            else if (position == 3) return FRIENDS;
            else if (position == (4 + PeopleNum)) return GROUPS;
            else if (position < (4 + PeopleNum)) return AFRIEND;
            else return AGROUP;
        }

        @Override
        public Object getItem(int position) {
            switch (getItemViewType(position)) {
                case NEWFRIENDS:
                    break;
                case CREATE_GROUP:
                    break;
                case LINKMAN:
                    break;
                case FRIENDS:
                    break;
                case GROUPS:
                    break;
                case AFRIEND:
                    return peopleItemList.get(position - 4);
                case AGROUP:
                    return groupItemList.get(position - 5 - PeopleNum);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ViewHolder holder;
            int type = getItemViewType(position);
            //通过convertView来判断是否已经加载过了，如果没有就加载
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case NEWFRIENDS:
                        v = LayoutInflater.from(getActivity()).inflate
                                (R.layout.linkman_new_friends_button, parent, false);
                        v.setTag(holder);
                        break;
                    case CREATE_GROUP:
                        v = LayoutInflater.from(getActivity()).inflate
                                (R.layout.linkman_create_group_button, parent, false);
                        break;
                    case LINKMAN:
                        v = LayoutInflater.from(getActivity()).inflate
                                (R.layout.linkman_linkman, parent, false);
                        v.setTag(holder);
                        break;
                    case FRIENDS:
                        v = LayoutInflater.from(getActivity()).inflate
                                (R.layout.linkman_friends_button, parent, false);
                        holder.controlArrow =
                                (ImageView) v.findViewById(R.id.linkman_friends_arrow);
                        holder.count =
                                (TextView) v.findViewById(R.id.linkman_friends_count);
                        v.setTag(holder);
                        break;
                    case GROUPS:
                        v = LayoutInflater.from(getActivity()).inflate
                                (R.layout.linkman_groups_button, parent, false);
                        holder.controlArrow =
                                (ImageView) v.findViewById(R.id.linkman_groups_arrow);
                        holder.count =
                                (TextView) v.findViewById(R.id.linkman_groups_count);
                        v.setTag(holder);
                        break;
                    case AFRIEND:
                        v = LayoutInflater.from(getActivity()).
                                inflate(R.layout.people_item, parent, false);
                        holder.portrait = (ImageView) v.findViewById(R.id.pepple_portrait);
                        holder.name = (TextView) v.findViewById(R.id.people_name);
                        holder.age = (TextView) v.findViewById(R.id.people_age);
                        holder.info = (TextView) v.findViewById(R.id.people_info);
                        v.setTag(holder);
                        break;
                    case AGROUP:
                        v = LayoutInflater.from(getActivity()).
                                inflate(R.layout.group_item, parent, false);
                        holder.portrait = (ImageView) v.findViewById(R.id.group_portrait);
                        holder.name = (TextView) v.findViewById(R.id.group_name);
                        holder.info = (TextView) v.findViewById(R.id.group_info);
                        v.setTag(holder);
                        break;
                }
            } else {
                holder = (ViewHolder) v.getTag();
            }
            switch (type) {
                case NEWFRIENDS:
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), FindNewFriendsActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;
                case CREATE_GROUP:
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
                            startActivity(intent);
                        }
                    });
                    break;
                case LINKMAN:
                    //do nothing
                    break;
                case FRIENDS:
                    holder.count.setText(peopleItemList.size() + "/" + peopleItemList.size());
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (PeopleStage) {
                                case ON:
                                    holder.controlArrow.
                                            setImageResource(R.drawable.arrow_right);
                                    closePeopleList();
                                    PeopleStage = CLOSE;
                                    break;
                                case CLOSE:
                                    holder.controlArrow.
                                            setImageResource(R.drawable.arrow_down);
                                    expandPeopleList();
                                    PeopleStage = ON;
                                    break;
                            }
                        }
                    });
                    break;
                case GROUPS:
                    holder.count.setText(groupItemList.size() + "/" + groupItemList.size());
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (GroupStage) {
                                case ON:
                                    holder.controlArrow.
                                            setImageResource(R.drawable.arrow_right);
                                    closeGroupList();
                                    GroupStage = CLOSE;
                                    break;
                                case CLOSE:
                                    holder.controlArrow.
                                            setImageResource(R.drawable.arrow_down);
                                    expandGroupList();
                                    GroupStage = ON;
                                    break;
                            }
                        }
                    });
                    break;
                case AFRIEND:
                    options = getPeopleOptions();
                    holder.name.setText(peopleItemList.get(position - 4).getName());
                    String sex = peopleItemList.get(position - 4).getSex();
                    if (("female").equals(sex))
                        holder.age.setText("♀" + peopleItemList.get(position - 4).getAge());
                    else
                        holder.age.setText("♂" + peopleItemList.get(position - 4).getAge());
                    holder.info.setText(peopleItemList.get(position - 4).getArea() + " "
                            + peopleItemList.get(position - 4).getIntroduce());
                    imageLoader.displayImage(peopleItemList.get(position - 4).getImage()
                            , holder.portrait, options, animateFirstListener);
                    break;
                case AGROUP:
                    options = getGroupOptions();
                    holder.name.setText(groupItemList.get(position - 5 -
                            PeopleNum).getName());
                    holder.info.setText(groupItemList.get(position - 5 -
                            PeopleNum).getArea() + " "
                            + groupItemList.get(position - 5 -
                            PeopleNum).getIntroduce());
                    imageLoader.displayImage(groupItemList.get(position - 5 -
                                    PeopleNum).getImage()
                            , holder.portrait, options, animateFirstListener);
                    break;
            }
            return v;
        }


    }

    private void expandGroupList() {
        GroupNum = groupItemList.size();
        adapter.notifyDataSetChanged();
    }

    private void closePeopleList() {
        PeopleNum = 0;
        adapter.notifyDataSetChanged();
    }

    private void closeGroupList() {
        GroupNum = 0;
        adapter.notifyDataSetChanged();
    }

    private void expandPeopleList() {
        PeopleNum = peopleItemList.size();
        adapter.notifyDataSetChanged();
    }

    private DisplayImageOptions getPeopleOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_portrait) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.default_portrait)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.default_portrait)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                .build();//构建完成
        return options;
    }

    private DisplayImageOptions getGroupOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.group_portrait) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.group_portrait)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.group_portrait)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                .build();//构建完成
        return options;
    }
}

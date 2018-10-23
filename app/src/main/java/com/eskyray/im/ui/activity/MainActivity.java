package com.eskyray.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eskyray.im.R;
import com.eskyray.im.server.utils.NLog;
import com.eskyray.im.server.utils.NToast;
import com.eskyray.im.ui.fragment.ContactsFragment;
import com.eskyray.im.ui.fragment.MessageFragment;
import com.eskyray.im.ui.fragment.SettingFragment;
import com.eskyray.im.ui.widget.DragPointView;
import com.eskyray.im.ui.widget.HomeWatcherReceiver;
import com.eskyray.im.ui.widget.MorePopWindow;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity {

    public static final int ON_MESSAGE = 0;
    public static final int ON_LINKMAN = 1;
    public static final int ON_USER = 2;

    private int STAGE = ON_MESSAGE;

    @BindView(R.id.tab_text_chats)
    TextView mTextChats;
    @BindView(R.id.tab_img_chats)
    ImageView mImageChats;
    @BindView(R.id.seal_num)
    DragPointView mUnreadNumView;

    @BindView(R.id.tab_text_contact)
    TextView mTextContact;
    @BindView(R.id.tab_img_contact)
    ImageView mImageContact;

    @BindView(R.id.tab_text_me)
    TextView mTextMe;
    @BindView(R.id.tab_img_me)
    ImageView mImageMe;

    @BindView(R.id.seal_more)
    ImageView moreImage;
    @BindView(R.id.mine_red)
    ImageView mMineRed;
    @BindView(R.id.ac_iv_search)
    ImageView mSearchImageView;
    /**
     * 会话列表的fragment
     */
    private boolean isDebug;
    private Context mContext;
    private MessageFragment messageFragment;//会话页
    private ContactsFragment contactsFragment;//联系人页
    private SettingFragment settingFragment;//设置页

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            NLog.d("MainActivity", "onCreate intent flag FLAG_ACTIVITY_BROUGHT_TO_FRONT");
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this;
        isDebug = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
        initBottom();
        changeSelectedTabState(ON_MESSAGE);
        registerHomeKeyReceiver(this);
    }

    private void initBottom() {
        mImageChats.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_chat));
        mImageContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_contacts));
        mImageMe.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_me));
        mTextChats.setTextColor(Color.parseColor("#abadbb"));
        mTextContact.setTextColor(Color.parseColor("#abadbb"));
        mTextMe.setTextColor(Color.parseColor("#abadbb"));
    }

    private void hideFrame(FragmentTransaction transaction) {
        if (messageFragment != null) {
            transaction.hide(messageFragment);
        }
        if (contactsFragment != null) {
            transaction.hide(contactsFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
    }

    private void changeSelectedTabState(int position) {
        if (position != STAGE) {
            switch (STAGE) {
                case ON_MESSAGE:
                    mTextChats.setTextColor(Color.parseColor("#abadbb"));
                    mImageChats.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_chat));
                    break;
                case ON_LINKMAN:
                    mTextContact.setTextColor(Color.parseColor("#abadbb"));
                    mImageContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_contacts));
                    break;
                case ON_USER:
                    mImageMe.setBackgroundResource(R.drawable.tab_me);
                    mTextMe.setTextColor(Color.parseColor("#abadbb"));
                    break;
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFrame(transaction);//隐藏碎片

        switch (position) {
            case ON_MESSAGE:
                mTextChats.setTextColor(Color.parseColor("#0099ff"));
                mImageChats.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_chat_hover));
                STAGE = ON_MESSAGE;
                if (firstClick == 0) {
                    firstClick = System.currentTimeMillis();
                } else {
                    secondClick = System.currentTimeMillis();
                }
                NLog.i("MainActivity", "time = " + (secondClick - firstClick));
                if (secondClick - firstClick > 0 && secondClick - firstClick <= 800) {
                    firstClick = 0;
                    secondClick = 0;
                } else if (firstClick != 0 && secondClick != 0) {
                    firstClick = 0;
                    secondClick = 0;
                }

                if (messageFragment == null) {
                    messageFragment = new MessageFragment();
                    transaction.add(R.id.container, messageFragment);
                } else {
                    transaction.show(messageFragment);
                }
                break;
            case ON_LINKMAN:
                mTextContact.setTextColor(Color.parseColor("#0099ff"));
                mImageContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_contacts_hover));
                STAGE = ON_LINKMAN;
                if (contactsFragment == null) {
                    contactsFragment = new ContactsFragment();
                    transaction.add(R.id.container, contactsFragment);
                } else {
                    transaction.show(contactsFragment);
                }
                break;
            case ON_USER:
                mTextMe.setTextColor(Color.parseColor("#0099ff"));
                mImageMe.setBackgroundResource(R.drawable.tab_me_hover);
                STAGE = ON_USER;
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.add(R.id.container, settingFragment);
                } else {
                    transaction.show(settingFragment);
                }
                mMineRed.setVisibility(View.GONE);
                break;
        }
        transaction.commit();
    }

    long firstClick = 0;
    long secondClick = 0;

    @OnClick({R.id.seal_chat, R.id.seal_contact_list, R.id.seal_me, R.id.seal_more, R.id.ac_iv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.seal_chat:
                changeSelectedTabState(ON_MESSAGE);
                break;
            case R.id.seal_contact_list:
                changeSelectedTabState(ON_LINKMAN);
                break;
            case R.id.seal_me:
                changeSelectedTabState(ON_USER);
                break;
            case R.id.seal_more:
                MorePopWindow morePopWindow = new MorePopWindow(MainActivity.this);
                morePopWindow.showPopupWindow(moreImage);
                break;
            case R.id.ac_iv_search:
                startActivity(new Intent(MainActivity.this, SealSearchActivity.class));
                break;
            case R.id.seal_num:
                mUnreadNumView.setVisibility(View.GONE);
                NToast.shortToast(mContext, getString(R.string.clear_success));
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void hintKbTwo() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus() && event.getAction() == MotionEvent.ACTION_UP) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private HomeWatcherReceiver mHomeKeyReceiver = null;

    //如果遇见 Android 7.0 系统切换到后台回来无效的情况 把下面注册广播相关代码注释或者删除即可解决。下面广播重写 home 键是为了解决三星 note3 按 home 键花屏的一个问题
    private void registerHomeKeyReceiver(Context context) {
//        if (mHomeKeyReceiver == null) {
//            mHomeKeyReceiver = new HomeWatcherReceiver();
//            final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            try {
//                context.registerReceiver(mHomeKeyReceiver, homeFilter);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
}


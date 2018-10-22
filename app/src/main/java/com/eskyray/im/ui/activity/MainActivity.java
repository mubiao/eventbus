package com.eskyray.im.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eskyray.im.R;
import com.eskyray.im.manager.BroadcastManager;
import com.eskyray.im.server.utils.NLog;
import com.eskyray.im.server.utils.NToast;
import com.eskyray.im.ui.fragment.ContactsFragment;
import com.eskyray.im.ui.fragment.MessageFragment;
import com.eskyray.im.ui.widget.DragPointView;
import com.eskyray.im.ui.widget.HomeWatcherReceiver;
import com.eskyray.im.ui.widget.MorePopWindow;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        ViewPager.OnPageChangeListener,
        View.OnClickListener,
        DragPointView.OnDragListencer{

    public static ViewPager mViewPager;
    private List<Fragment> mFragment = new ArrayList<>();
    private ImageView moreImage, mImageChats, mImageContact, mImageMe, mMineRed;
    private TextView mTextChats, mTextContact, mTextMe;
    private DragPointView mUnreadNumView;
    private ImageView mSearchImageView;
    /**
     * 会话列表的fragment
     */
    private boolean isDebug;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            NLog.d("MainActivity", "onCreate intent flag FLAG_ACTIVITY_BROUGHT_TO_FRONT");
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        mContext = this;
        isDebug = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
        initViews();
        changeTextViewColor();
        changeSelectedTabState(0);
        initMainViewPager();
        registerHomeKeyReceiver(this);
    }


    private void initViews() {
        RelativeLayout chatRLayout = (RelativeLayout) findViewById(R.id.seal_chat);
        RelativeLayout contactRLayout = (RelativeLayout) findViewById(R.id.seal_contact_list);
        RelativeLayout mineRLayout = (RelativeLayout) findViewById(R.id.seal_me);
        mImageChats = (ImageView) findViewById(R.id.tab_img_chats);
        mImageContact = (ImageView) findViewById(R.id.tab_img_contact);
        mImageMe = (ImageView) findViewById(R.id.tab_img_me);
        mTextChats = (TextView) findViewById(R.id.tab_text_chats);
        mTextContact = (TextView) findViewById(R.id.tab_text_contact);
        mTextMe = (TextView) findViewById(R.id.tab_text_me);
        mMineRed = (ImageView) findViewById(R.id.mine_red);
        moreImage = (ImageView) findViewById(R.id.seal_more);
        mSearchImageView = (ImageView) findViewById(R.id.ac_iv_search);

        chatRLayout.setOnClickListener(this);
        contactRLayout.setOnClickListener(this);
        mineRLayout.setOnClickListener(this);
        moreImage.setOnClickListener(this);
        mSearchImageView.setOnClickListener(this);
    }


    private void initMainViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);

        mUnreadNumView = (DragPointView) findViewById(R.id.seal_num);
        mUnreadNumView.setOnClickListener(this);
        mUnreadNumView.setDragListencer(this);

        mFragment.add(new MessageFragment());
        mFragment.add(new ContactsFragment());
//        mFragment.add(new MineFragment());
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragment.get(position);
            }

            @Override
            public int getCount() {
                return mFragment.size();
            }
        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnPageChangeListener(this);
        initData();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeTextViewColor();
        changeSelectedTabState(position);
    }

    private void changeTextViewColor() {
        mImageChats.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_chat));
        mImageContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_contacts));
        mImageMe.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_me));
        mTextChats.setTextColor(Color.parseColor("#abadbb"));
        mTextContact.setTextColor(Color.parseColor("#abadbb"));
        mTextMe.setTextColor(Color.parseColor("#abadbb"));
    }

    private void changeSelectedTabState(int position) {
        switch (position) {
            case 0:
                mTextChats.setTextColor(Color.parseColor("#0099ff"));
                mImageChats.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_chat_hover));
                break;
            case 1:
                mTextContact.setTextColor(Color.parseColor("#0099ff"));
                mImageContact.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_contacts_hover));
                break;
            case 2:
                mTextMe.setTextColor(Color.parseColor("#0099ff"));
                mImageMe.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_me_hover));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    long firstClick = 0;
    long secondClick = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.seal_chat:
                if (mViewPager.getCurrentItem() == 0) {
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
                }
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.seal_contact_list:
                mViewPager.setCurrentItem(1, false);
                break;
            case R.id.seal_me:
                mViewPager.setCurrentItem(3, false);
                mMineRed.setVisibility(View.GONE);
                break;
            case R.id.seal_more:
                MorePopWindow morePopWindow = new MorePopWindow(MainActivity.this);
                morePopWindow.showPopupWindow(moreImage);
                break;
            case R.id.ac_iv_search:
                startActivity(new Intent(MainActivity.this, SealSearchActivity.class));
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("systemconversation", false)) {
            mViewPager.setCurrentItem(0, false);
        }
    }

    protected void initData() {
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

    @Override
    public void onDragOut() {
        mUnreadNumView.setVisibility(View.GONE);
        NToast.shortToast(mContext, getString(R.string.clear_success));
    }
}


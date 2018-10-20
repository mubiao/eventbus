package com.eskyray.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.eskyray.im.R;
import com.eskyray.im.SealConst;
import com.eskyray.im.server.utils.NToast;
import com.eskyray.im.server.utils.PhotoUtils;
import com.eskyray.im.ui.beans.FriendInfo;
import com.eskyray.im.ui.emoticon.AndroidEmoji;
import com.eskyray.im.ui.widget.BottomMenuDialog;
import com.eskyray.im.ui.widget.ClearWriteEditText;
import com.eskyray.im.ui.widget.LoadDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity  extends BaseActivity implements View.OnClickListener {

        private static final int GET_QI_NIU_TOKEN = 131;
        private static final int CREATE_GROUP = 16;
        private static final int SET_GROUP_PORTRAIT_URI = 17;
        public static final String REFRESH_GROUP_UI = "REFRESH_GROUP_UI";
        private PhotoUtils photoUtils;
        private ImageView asyncImageView;
        private BottomMenuDialog dialog;
        private String mGroupName, mGroupId;
        private ClearWriteEditText mGroupNameEdit;
        private List<String> groupIds = new ArrayList<>();
        private Uri selectUri;
        private String imageUrl;

        @Override
        @SuppressWarnings("unchecked")
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_group);
            setTitle(R.string.rc_item_create_group);
            List<FriendInfo> memberList = (List<FriendInfo>) getIntent().getSerializableExtra("GroupMember");
            initView();
            setPortraitChangeListener();
            if (memberList != null && memberList.size() > 0) {
                groupIds.add(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""));
                for (FriendInfo f : memberList) {
                    groupIds.add(f.getJid());
                }
            }
        }

        private void setPortraitChangeListener() {
            photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
                @Override
                public void onPhotoResult(Uri uri) {
                    if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                        selectUri = uri;
                        LoadDialog.show(mContext);
//                        requestest(GET_QI_NIU_TOKEN);
                    }
                }

                @Override
                public void onPhotoCancel() {

                }
            });
        }

        private void initView() {
            asyncImageView = findViewById(R.id.img_Group_portrait);
            asyncImageView.setOnClickListener(this);
            Button mButton = (Button) findViewById(R.id.create_ok);
            mButton.setOnClickListener(this);
            mGroupNameEdit = (ClearWriteEditText) findViewById(R.id.create_groupname);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img_Group_portrait:
                    showPhotoDialog();
                    break;
                case R.id.create_ok:
                    mGroupName = mGroupNameEdit.getText().toString().trim();
                    if (TextUtils.isEmpty(mGroupName)) {
                        NToast.shortToast(mContext, getString(R.string.group_name_not_is_null));
                        break;
                    }
                    if (mGroupName.length() == 1) {
                        NToast.shortToast(mContext, getString(R.string.group_name_size_is_one));
                        return;
                    }
                    if (AndroidEmoji.isEmoji(mGroupName)) {
                        if (mGroupName.length() <= 2) {
                            NToast.shortToast(mContext, getString(R.string.group_name_size_is_one));
                            return;
                        }
                    }
                    if (groupIds.size() > 1) {
                        LoadDialog.show(mContext);
//                        request(CREATE_GROUP, true);
                    }

                    break;
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            hintKbTwo();
            finish();
            return super.onOptionsItemSelected(item);
        }


        /**
         * 弹出底部框
         */
        private void showPhotoDialog() {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            dialog = new BottomMenuDialog(mContext);
            dialog.setConfirmListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    photoUtils.takePicture(CreateGroupActivity.this);
                }
            });
            dialog.setMiddleListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    photoUtils.selectPicture(CreateGroupActivity.this);
                }
            });
            dialog.show();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PhotoUtils.INTENT_CROP:
                case PhotoUtils.INTENT_TAKE:
                case PhotoUtils.INTENT_SELECT:
                    photoUtils.onActivityResult(CreateGroupActivity.this, requestCode, resultCode, data);
                    break;
            }
        }

        private void hintKbTwo() {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && getCurrentFocus() != null) {
                if (getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    }


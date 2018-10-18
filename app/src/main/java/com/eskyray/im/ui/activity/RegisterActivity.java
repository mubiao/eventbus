package com.eskyray.im.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eskyray.im.R;
import com.eskyray.im.manager.ConnectionManager;
import com.eskyray.im.server.utils.AMUtils;
import com.eskyray.im.server.utils.DownTimer;
import com.eskyray.im.server.utils.DownTimerListener;
import com.eskyray.im.server.utils.NToast;
import com.eskyray.im.ui.widget.ClearWriteEditText;
import com.eskyray.im.ui.widget.LoadDialog;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

public class RegisterActivity extends BaseActivity implements View.OnClickListener,DownTimerListener {

        private static final int CHECK_PHONE = 1;
        private static final int SEND_CODE = 2;
        private static final int VERIFY_CODE = 3;
        private static final int REGISTER = 4;
        private static final int REGISTER_BACK = 1001;
        private ImageView mImgBackground;
        private ClearWriteEditText mPhoneEdit, mCodeEdit, mNickEdit, mPasswordEdit;
        private Button mGetCode, mConfirm;
        private String mPhone, mCode, mNickName, mPassword, mCodeToken;
        private boolean isRequestCode = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);
            setHeadVisibility(View.GONE);
            initView();
        }

        private void initView() {
            mPhoneEdit = (ClearWriteEditText) findViewById(R.id.reg_phone);
            mCodeEdit = (ClearWriteEditText) findViewById(R.id.reg_code);
            mNickEdit = (ClearWriteEditText) findViewById(R.id.reg_username);
            mPasswordEdit = (ClearWriteEditText) findViewById(R.id.reg_password);
            mGetCode = (Button) findViewById(R.id.reg_getcode);
            mConfirm = (Button) findViewById(R.id.reg_button);

            mGetCode.setOnClickListener(this);
            mGetCode.setClickable(false);
            mConfirm.setOnClickListener(this);

            TextView goLogin = (TextView) findViewById(R.id.reg_login);
            TextView goForget = (TextView) findViewById(R.id.reg_forget);
            goLogin.setOnClickListener(this);
            goForget.setOnClickListener(this);

            mImgBackground = (ImageView) findViewById(R.id.rg_img_backgroud);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.translate_anim);
                    mImgBackground.startAnimation(animation);
                }
            }, 200);

            addEditTextListener();

        }

        private void addEditTextListener() {
            mPhoneEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 11 && isBright) {
                        if (AMUtils.isMobile(s.toString().trim())) {
                            mPhone = s.toString().trim();
                            AMUtils.onInactive(mContext, mPhoneEdit);
                        } else {
                            Toast.makeText(mContext, R.string.Illegal_phone_number, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mGetCode.setClickable(false);
                        mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mCodeEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 6) {
                        AMUtils.onInactive(mContext, mCodeEdit);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mPasswordEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 5) {
                        mConfirm.setClickable(true);
                        mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
                    } else {
                        mConfirm.setClickable(false);
                        mConfirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reg_login:
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case R.id.reg_forget:
                    startActivity(new Intent(this, ForgetPasswordActivity.class));
                    break;
                case R.id.reg_getcode:
                    if (TextUtils.isEmpty(mPhoneEdit.getText().toString().trim())) {
                        NToast.longToast(mContext, R.string.phone_number_is_null);
                    } else {
                        isRequestCode = true;
                        DownTimer downTimer = new DownTimer();
                        downTimer.setListener(this);
                        downTimer.startDown(60 * 1000);
                    }
                    break;
                case R.id.reg_button:
                    mPhone = mPhoneEdit.getText().toString().trim();
                    mCode = mCodeEdit.getText().toString().trim();
                    mNickName = mNickEdit.getText().toString().trim();
                    mPassword = mPasswordEdit.getText().toString().trim();


                    if (TextUtils.isEmpty(mNickName)) {
                        NToast.shortToast(mContext, getString(R.string.name_is_null));
                        mNickEdit.setShakeAnimation();
                        return;
                    }
                    if (mNickName.contains(" ")) {
                        NToast.shortToast(mContext, getString(R.string.name_contain_spaces));
                        mNickEdit.setShakeAnimation();
                        return;
                    }

                    if (TextUtils.isEmpty(mPhone)) {
                        NToast.shortToast(mContext, getString(R.string.phone_number_is_null));
                        mPhoneEdit.setShakeAnimation();
                        return;
                    }
                    if (TextUtils.isEmpty(mCode)) {
                        NToast.shortToast(mContext, getString(R.string.code_is_null));
                        mCodeEdit.setShakeAnimation();
                        return;
                    }
                    if (TextUtils.isEmpty(mPassword)) {
                        NToast.shortToast(mContext, getString(R.string.password_is_null));
                        mPasswordEdit.setShakeAnimation();
                        return;
                    }
                    if (mPassword.contains(" ")) {
                        NToast.shortToast(mContext, getString(R.string.password_cannot_contain_spaces));
                        mPasswordEdit.setShakeAnimation();
                        return;
                    }

//                if (!isRequestCode) {
//                    NToast.shortToast(mContext, getString(R.string.not_send_code));
//                    return;
//                }

                    LoadDialog.show(mContext);
//                request(VERIFY_CODE, true);
                    new RegTask().execute();

                    break;
            }
        }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class RegTask extends AsyncTask<Void,Void,Boolean> {

            @Override
            protected Boolean doInBackground(Void... voids) {
                mPhone = mPhoneEdit.getText().toString().trim();
                mPassword = mPasswordEdit.getText().toString().trim();

                try {
                    AbstractXMPPConnection connection = ConnectionManager.getConnection();
                    boolean connected = connection.isConnected();

                    // 用户管理
                    AccountManager manager = AccountManager.getInstance(connection);

                    // 创建账户
                    manager.createAccount(mPhone, mPassword);

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    NToast.shortToast(mContext, R.string.register_success);
                    LoadDialog.dismiss(mContext);
                    Intent data = new Intent();
                    data.putExtra("phone", mPhone);
                    data.putExtra("password", mPassword);
                    data.putExtra("nickname", mNickName);
                    setResult(REGISTER_BACK, data);

                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

        boolean isBright = true;

        @Override
        public void onTick(long millisUntilFinished) {
            mGetCode.setText(String.valueOf(millisUntilFinished / 1000) + "s");
            mGetCode.setClickable(false);
            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_gray));
            isBright = false;
        }

        @Override
        public void onFinish() {
            mGetCode.setText(R.string.get_code);
            mGetCode.setClickable(true);
            mGetCode.setBackgroundDrawable(getResources().getDrawable(R.drawable.rs_select_btn_blue));
            isBright = true;
        }

    }


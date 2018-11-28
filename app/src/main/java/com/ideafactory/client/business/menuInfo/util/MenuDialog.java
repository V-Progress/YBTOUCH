package com.ideafactory.client.business.menuInfo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.business.weichat.views.WeiChatShowPager;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;
import java.util.Map;

public class MenuDialog {
    private static final String TAG = "MenuDialog";

    /**
     * 输入密码dialog
     */
    private static AlertDialog norAlertDialog;
    private static TextView hintsEditText;
    private static EditText norPwdEditText;

    public static void showNormalEntryDialog(final Context context, final Intent intent, final Button btn, final Button btn2,
                                             final String def) {
        norAlertDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_normal_dialog, null);
        TextView norTitleTextView = (TextView) view.findViewById(R.id.tv_pwd_nor_title);
        norPwdEditText = (EditText) view.findViewById(R.id.et_entry_nor_pwd);
        Button norCancelBtn = (Button) view.findViewById(R.id.btn_nor_pwd_cancel);
        Button norSureBtn = (Button) view.findViewById(R.id.btn_nor_pwd_sure);
        hintsEditText = (TextView) view.findViewById(R.id.tv_entry_nor_pwd_hints);

        norTitleTextView.setText(R.string.pwd_protect);
        hintsEditText.setText(R.string.input_pwd_to_next);

        norCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                norAlertDialog.dismiss();
            }
        });

        norSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enter_pwd = norPwdEditText.getText().toString().trim();
                if (TextUtils.isEmpty(enter_pwd)) {
                    hintsEditText.setText(R.string.pwd_is_null);
                    return;
                }
                String saved_pwd = SpUtils.getString(APP.getContext(), SpUtils.MENU_PWD, "");
                if (saved_pwd.equals(enter_pwd)) {//密码正确进行相关操作
//                    Toast.makeText(context, "密码正确", Toast.LENGTH_SHORT).show();
                    if (intent != null) {
                        context.startActivity(intent);
                    }
                    if (btn != null) {
                        MenuDialog.bindDecDialog(SwitchLayout.getActivity(), btn, null);
                    }
                    if (btn2 != null) {
                        MenuDialog.bindDecDialog(SwitchLayout.getActivity(), null, btn2);
                    }
                    if (def != null) {
                        if (def.equals("1")) {//快速发布
                            WeiChatShowPager.playDefaultLayout();
                        } else if (def.equals("2")) {//退出APP
                            BaseActivity.finishAll();
                        }
                    }
                    norAlertDialog.dismiss();
                } else {
                    hintsEditText.setText(R.string.pwd_is_error);
                }
            }
        });

        norAlertDialog.setView(view, 0, 0, 0, 0);
        norAlertDialog.show();
    }

    /**
     * 绑定设备对话框
     */
    private static AlertDialog bindDecDialog;
    private static EditText bindNumEditText, bindNameEditText;
    private static TextView bindHintsTextView;

    public static void bindDecDialog(final Context context, final Button btn, final Button btn2) {
        bindDecDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.menu_bind_dialog, null);
        TextView bindTitleTextView = (TextView) view.findViewById(R.id.tv_bind_title);
        bindNameEditText = (EditText) view.findViewById(R.id.et_bind_name);
        bindNumEditText = (EditText) view.findViewById(R.id.et_bind_num);
        Button bindCancelBtn = (Button) view.findViewById(R.id.btn_bind_cancel);
        Button bindSureBtn = (Button) view.findViewById(R.id.btn_bind_sure);
        bindHintsTextView = (TextView) view.findViewById(R.id.tv_bind_hints);

        bindNameEditText.addTextChangedListener(new EditChangedListener());
        bindNumEditText.addTextChangedListener(new EditChangedListener());

        bindTitleTextView.setText(R.string.bind_dev);

        bindCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindDecDialog.dismiss();
            }
        });
        bindSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceNo = HeartBeatClient.getDeviceNo();
                String userName = bindNameEditText.getText().toString();
                String userRand = bindNumEditText.getText().toString();

                if (!TextUtils.isEmpty(deviceNo)) {
                    if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userRand)) {
                        Map map = new HashMap();
                        map.put("deviceNo", deviceNo);
                        map.put("userName", userName);
                        map.put("userRand", userRand);
                        MyXutils.getInstance().post(ResourceUpdate.DEC_NUM, map, new MyXutils.XCallBack() {
                            @Override
                            public void onSuccess(String result) {
                                if (result.startsWith("\"")) {
                                    result = result.substring(1, result.length() - 1);
                                }
                                if (!result.equals("faile")) {
                                    String[] split = result.split("\"");
                                    String result1 = split[split.length - 2];
                                    switch (result1) {
                                        case "1":
                                            if (btn != null) {
                                                btn.setText(R.string.menu_bind_user);
                                                btn.setTextColor(Color.parseColor("#95e546"));
                                                btn.setBackgroundResource(R.drawable.is_service_btn);
                                            }
                                            if (btn2 != null) {
                                                btn2.setText(R.string.bind_user);
                                                btn2.setBackgroundResource(R.drawable.wei_chat_btn);
                                            }
                                            Toast.makeText(APP.getContext(), R.string.bind_dev_ok, Toast.LENGTH_SHORT).show();
                                            bindDecDialog.dismiss();
                                            break;
                                        case "2":
                                            bindHintsTextView.setText(R.string.bind_code_no);
                                            break;
                                        default:
                                            bindHintsTextView.setText(R.string.bind_code_transfer_error);
                                            break;
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable ex) {

                            }

                            @Override
                            public void onFinish() {

                            }
                        });
                    } else {
                        bindHintsTextView.setText(R.string.bind_code_is_null);
                    }
                } else {
                    bindHintsTextView.setText(R.string.bind_code_no_get);
                }
            }
        });

        bindDecDialog.setView(view, 0, 0, 0, 0);
        bindDecDialog.show();
    }

    private static class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (MenuInfoActivity.onReceivedEdChange != null) {
                MenuInfoActivity.onReceivedEdChange.OnEdreceived(s);
            }
        }
    }
}

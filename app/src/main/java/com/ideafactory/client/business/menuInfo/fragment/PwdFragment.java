package com.ideafactory.client.business.menuInfo.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.R;
import com.ideafactory.client.util.SpUtils;

public class PwdFragment extends Fragment {
    private Button setBtn, resetBtn;
    private TextView oneHintTextView, twoHintTextView;

    public PwdFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pwd, container, false);
        setBtn = (Button) rootView.findViewById(R.id.btn_pwd_setting);
        resetBtn = (Button) rootView.findViewById(R.id.btn_pwd_reset);
        oneHintTextView = (TextView) rootView.findViewById(R.id.tv_pwd_hint_one);
        twoHintTextView = (TextView) rootView.findViewById(R.id.tv_pwd_hint_two);

        twoHintTextView.setText(R.string.hint_pwd);
        setBtn.setOnClickListener(setListener);
        resetBtn.setOnClickListener(resetListener);

        if (isSetupPwd()) {
            setBtn.setVisibility(View.VISIBLE);
            oneHintTextView.setText(R.string.no_pwd);
        } else {
            resetBtn.setVisibility(View.VISIBLE);
            oneHintTextView.setText(R.string.yes_pwd);
        }

        return rootView;
    }

    View.OnClickListener setListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isSetupPwd()) {
                showFirstEntryDialog(PwdFragment.this.getActivity());
            }
        }
    };

    View.OnClickListener resetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SpUtils.saveString(PwdFragment.this.getActivity(), SpUtils.MENU_PWD, "");//清除密码
            oneHintTextView.setText(R.string.no_pwd);
            setBtn.setVisibility(View.VISIBLE);
            resetBtn.setVisibility(View.GONE);
            Toast.makeText(PwdFragment.this.getActivity(), R.string.clear_pwd_ok, Toast.LENGTH_SHORT).show();
        }
    };

    private AlertDialog pwdAlertDialog;
    private TextView titleTextView, hintsTextView;
    private EditText firEditText, firEntryEditText;
    private Button firSureBtn, firCancelBtn;

    public void showFirstEntryDialog(final Context context) {
        pwdAlertDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_fir_dialog, null);
        titleTextView = (TextView) view.findViewById(R.id.tv_pwd_title);
        firEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd);
        firEntryEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd_confirm);
        firCancelBtn = (Button) view.findViewById(R.id.btn_first_pwd_cancel);
        firSureBtn = (Button) view.findViewById(R.id.btn_first_pwd_sure);
        hintsTextView = (TextView) view.findViewById(R.id.tv_first_entry_pwd_hints);

        titleTextView.setText(R.string.pwd_max_length);
        firCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwdAlertDialog.dismiss();
            }
        });
        firSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = firEditText.getText().toString().trim();
                String pwd_confirm = firEntryEditText.getText().toString().trim();
                if (TextUtils.isEmpty(pwd) || TextUtils.isEmpty(pwd_confirm)) {
                    hintsTextView.setText(R.string.pwd_is_null);
                    return;
                }
                if (!pwd.equals(pwd_confirm)) {
                    hintsTextView.setText(R.string.pwd_two_diff);
                    return;
                }
                SpUtils.saveString(PwdFragment.this.getActivity(), SpUtils.MENU_PWD, pwd);
                pwdAlertDialog.dismiss();
                oneHintTextView.setText(R.string.have_pwd);
                setBtn.setVisibility(View.GONE);
                resetBtn.setVisibility(View.VISIBLE);

            }
        });
        pwdAlertDialog.setView(view, 0, 0, 0, 0);
        pwdAlertDialog.show();
    }

    /**
     * 判断用户是否设置密码
     */
    private boolean isSetupPwd() {
        String pwd = SpUtils.getString(PwdFragment.this.getActivity(), SpUtils.MENU_PWD, "");
        //密码为空 返回为true 用户设置两次密码
        return TextUtils.isEmpty(pwd);
    }
}

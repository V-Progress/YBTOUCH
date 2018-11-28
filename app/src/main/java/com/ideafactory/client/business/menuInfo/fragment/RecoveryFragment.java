package com.ideafactory.client.business.menuInfo.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.util.MyAdmin;

public class RecoveryFragment extends Fragment {
    private TextView tipTextView;
    private Button fireBtn;

    public RecoveryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recovery, container, false);
        tipTextView = (TextView) rootView.findViewById(R.id.tv_recovery_tip);
        fireBtn = (Button) rootView.findViewById(R.id.btn_recovery_fire);

        init();

        return rootView;
    }

    private void init() {
        //获取系统管理权限
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) RecoveryFragment.this.getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        //申请权限
        ComponentName componentName = new ComponentName(RecoveryFragment.this.getActivity(), MyAdmin.class);
        //判断该组件是否有系统管理员的权限
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);

        if (!isAdminActive) {
            tipTextView.setText(R.string.no_active);
            fireBtn.setText(R.string.sure_active);
            fireBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToRec();
                }
            });
        } else {
            tipTextView.setText(R.string.is_recovery_dec);
            fireBtn.setText(R.string.recovery_dec);
            fireBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRecovery(RecoveryFragment.this.getActivity());
                }
            });
        }
    }

    private void ToRec() {
        //获取系统管理权限
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) RecoveryFragment.this.getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        //申请权限
        ComponentName componentName = new ComponentName(RecoveryFragment.this.getActivity(), MyAdmin.class);
        //判断该组件是否有系统管理员的权限
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
        if (isAdminActive) {
            //恢复出厂设置（建议不要真机测试）
            devicePolicyManager.wipeData(0);
        } else {
            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给哪个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivity(intent);
        }
    }

    public static AlertDialog recoveryDialog;

    public void isRecovery(final Context context) {
        recoveryDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_recovery_dialog, null);
        Button cancelBtn = (Button) view.findViewById(R.id.btn_recovery_cancel);
        Button sureBtn = (Button) view.findViewById(R.id.btn_recovery_sure);

        recoveryDialog.setView(view, 0, 0, 0, 0);
        recoveryDialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoveryDialog.dismiss();
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToRec();
            }
        });
    }

}

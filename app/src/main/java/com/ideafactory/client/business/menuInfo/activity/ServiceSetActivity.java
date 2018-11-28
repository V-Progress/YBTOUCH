package com.ideafactory.client.business.menuInfo.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.R;
import com.ideafactory.client.business.menuInfo.bean.SwitchBtn;
import com.ideafactory.client.common.Constants;
import com.ideafactory.client.common.VersionUpdateConstants;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.NetWorkUtil;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.Signaturer;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.UpdateVersionControl;
import com.ideafactory.client.util.xutil.MyXutils;

import java.util.HashMap;

import static com.ideafactory.client.common.net.ResourceUpdate.VERSION_URL;

public class ServiceSetActivity extends BaseActivity {
    private TextView decTextView, decInTextView, machineTextView;
    private TextView firmVerTextView;
    private TextView innerCapTextView, outerCapTextView;
    private RadioGroup StorageRadioGroup;
    private LinearLayout outerLinearLayout;
    private SwitchBtn switchBtn;
    private static Button firmVerButton;
    private static Integer dType;
    private PercentRelativeLayout layout;
    private EditText ipEditText, portEditText;
    private Button okButton;
    private static String noActive, noAuthor, online, standlone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_set);

        initView();
        setView();
    }

    private void initView() {
        decTextView = (TextView) findViewById(R.id.tv_service_deviceNo);
        decInTextView = (TextView) findViewById(R.id.tv_service_deviceInNo);
        machineTextView = (TextView) findViewById(R.id.tv_service_machineNo);
        firmVerTextView = (TextView) findViewById(R.id.tv_service_firmVersion);
        firmVerButton = (Button) findViewById(R.id.btn_service_firmVer);
        innerCapTextView = (TextView) findViewById(R.id.tv_service_inner_capacity);
        outerCapTextView = (TextView) findViewById(R.id.tv_service_outer_capacity);
        StorageRadioGroup = (RadioGroup) findViewById(R.id.radio_service_storage_path);
        outerLinearLayout = (LinearLayout) findViewById(R.id.ll_position_outer);
        switchBtn = (SwitchBtn) findViewById(R.id.switchBtn_service);
        layout = (PercentRelativeLayout) findViewById(R.id.ll_service_set);
        ipEditText = (EditText) findViewById(R.id.et_service_ip);
        portEditText = (EditText) findViewById(R.id.et_service_port);
        okButton = (Button) findViewById(R.id.btn_service_ok);

        noActive = getResources().getString(R.string.dev_no_active);
        noAuthor = getResources().getString(R.string.dev_no_authorization);
        online = getResources().getString(R.string.online_edition);
        standlone = getResources().getString(R.string.dev_standalone);
    }

    private void setView() {
        decTextView.setText(getDeviceNum());
        decInTextView.setText(LayoutCache.getPwd());
        if (NetWorkUtil.isNetworkConnected(this)) {
            machineTextView.setText(HeartBeatClient.getDeviceNo());
        } else {
            machineTextView.setText("-1");
        }

        firmVerTextView.setText(VersionUpdateConstants.getServerType(this) + "-" + MainActivity.versionName);
        checkNewFirmVer();
        firmVerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetWorkUtil.isNetworkConnected(ServiceSetActivity.this)) {
                    UpdateVersionControl.getInstance().checkUpdate();
                    TYTool.updatePd();//显示更新进度条
                    setOnReceivedProgressRun(new OnReceivedProgressRun() {
                        @Override
                        public void OnProgressRunReceived(int progress) {
                            TYTool.pd.setProgress(progress);//给进度条设置数值
                            if (progress == 100) {
                                TYTool.pd.dismiss();
                            }
                        }
                    });

                    firmVerButton.setEnabled(false);
                    firmVerButton.setText(R.string.downloading);
                }
            }
        });
        innerCapTextView.setText(TYTool.getEnSDDiskCon(this));//内部存储空间

        String sdCardPath = TYTool.getOuterPath();//判断外部存储卡是否存在
        if (!TextUtils.isEmpty(sdCardPath)) {//存在
            StorageRadioGroup.setVisibility(View.VISIBLE);
            outerLinearLayout.setVisibility(View.VISIBLE);
            String outSdcard = CommonUtils.getAvailaleSize(sdCardPath, this);
            outerCapTextView.setText(outSdcard);
        } else {
            StorageRadioGroup.setVisibility(View.GONE);
            outerLinearLayout.setVisibility(View.GONE);
        }

        String sdPath = LayoutCache.getSdPath();
        if (!TextUtils.isEmpty(sdPath)) {
            if (sdPath.equals("1") && StorageRadioGroup.getVisibility() == View.VISIBLE) {//1存储路径是外置sd卡,0存储路径是内置sd卡,
                // 保存的是外置的sd卡，而且当前存在
                StorageRadioGroup.check(R.id.rb_service_outer_check);
            } else {
                StorageRadioGroup.check(R.id.rb_service_inner_check);
            }
        }

        //存储路径的选择项
        StorageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rb_service_inner_check:
                        ResourceUpdate.setNewResourcePath(true);
                        LayoutCache.putSdPath("0");
                        break;
                    case R.id.rb_service_outer_check:
                        ResourceUpdate.setNewResourcePath(false);
                        LayoutCache.putSdPath("1");
                        break;
                }
            }
        });

        String ipAdress = "";
        String port = "";
        final String machineIpAdress = LayoutCache.getMechineIp();
        if (!TextUtils.isEmpty(machineIpAdress)) {
            switchBtn.getRightButton().setBackground(getResources().getDrawable(R.drawable.switch_right_press));
            switchBtn.getRightButton().setTextColor(Color.parseColor("#ffffff"));
            switchBtn.getLeftButton().setBackground(getResources().getDrawable(R.drawable.switch_left_btn));
            switchBtn.getLeftButton().setTextColor(Color.parseColor("#000000"));
            layout.setVisibility(View.VISIBLE);
            String[] ipWinName = machineIpAdress.split(",");
            if (ipWinName.length == 2) {
                ipAdress = ipWinName[0];
                port = ipWinName[1];
                ipEditText.setText(ipAdress);
                portEditText.setText(port);
            }
        } else {
            switchBtn.getLeftButton().setBackground(getResources().getDrawable(R.drawable.switch_left_press));
            switchBtn.getLeftButton().setTextColor(Color.parseColor("#ffffff"));
            switchBtn.getRightButton().setBackground(getResources().getDrawable(R.drawable.switch_right_btn));
            switchBtn.getRightButton().setTextColor(Color.parseColor("#000000"));
            layout.setVisibility(View.GONE);
        }

        //云标或者本地服务的按钮选择
        switchBtn.setOnLeftClickListener(new SwitchBtn.OnLeftClickListener() {

            @Override
            public void onLeftClickListener() {
                layout.setVisibility(View.GONE);
//                      Constants.initYbConstant();//初始化云标
                LayoutCache.putMachineIp("");
                //如果当前的服务器是云标的话
                if (Constants.CURRENT_SERVER_TYPE == Constants.YBWEB) {

                } else {
                    show3sDialog().show();
                    countDownTimer.start();
                }
            }
        });
        switchBtn.setOnRightClickListener(new SwitchBtn.OnRightClickListener() {

            @Override
            public void onRightClickListener() {
                layout.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(machineIpAdress)) {
                    ipEditText.setText("192.168.1.136");
                    portEditText.setText("8855");
                }
            }
        });
        //保存设置的ip地址
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchBtn.getId() != R.id.btn_switch_left) {
                    LayoutCache.putMachineIp(ipEditText.getText().toString().trim() + "," + portEditText.getText().toString()
                            .trim());
                    show3sDialog().show();
                    countDownTimer.start();
                }
            }
        });
    }

    /**
     * 获取设备编号
     */
    private static String getDeviceNum() {
        dType = Signaturer.getDType();
        String number = TYTool.getSerNum();
        if (dType == -1) {
            number += "(" + noActive + ")";
        } else if (dType == 0) {
            number += "(" + noAuthor + ")";
        } else if (dType == 1) {
            number += "(" + online + ")";
        } else if (dType == 2) {
            number += "(" + standlone + ")";
        }
        return number;
    }

    private static final int NEWVERSIONCHECK = 0x159652;
    private static Handler switchDialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NEWVERSIONCHECK:
                    boolean newVersion = (boolean) msg.obj;
                    if (newVersion) {
                        firmVerButton.setVisibility(View.VISIBLE);
                        firmVerButton.setText(R.string.click_update);
                    } else {
                        firmVerButton.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    };

    /**
     * 检查是否有新版本
     */
    private void checkNewFirmVer() {
        HashMap<String, String> paramMap = new HashMap();
        paramMap.put("clientVersion", getVersionName());
        paramMap.put("type", VersionUpdateConstants.CURRENT_VERSION + "");

        MyXutils.getInstance().post(VERSION_URL, paramMap, new MyXutils.XCallBack() {
            @Override
            public void onSuccess(String result) {
                if (result.startsWith("\"")) {
                    result = result.substring(1, result.length() - 1);
                }
                switch (result) {
                    case "1": //不需要更新
                        HandleMessageUtils.getInstance().sendHandler(NEWVERSIONCHECK, switchDialogHandler, false);
                        break;
                    case "faile":
                        HandleMessageUtils.getInstance().sendHandler(NEWVERSIONCHECK, switchDialogHandler, false);
                        break;
                    default:
                        HandleMessageUtils.getInstance().sendHandler(NEWVERSIONCHECK, switchDialogHandler, true);
                        break;
                }
            }

            @Override
            public void onError(Throwable ex) {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    /**
     * 获取当前版本号
     *
     * @return
     */
    private String getVersionName() {
        String version = "";
        try {
            // 获取packagemanager的实例
            PackageManager packageManager = HeartBeatClient.getInstance().getMainActivity().getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(HeartBeatClient.getInstance().getMainActivity().getPackageName
                    (), 0);
            version = packInfo.versionName;
        } catch (Exception ignored) {

        }
        return version;
    }

    /**
     * 处理布局
     */
    public void dealLayout(View view) {
        switch (view.getId()) {
            case R.id.btn_service_del://删除布局按钮
                delLayoutDialog(this);
                break;
            case R.id.btn_service_clear://清理磁盘按钮
                delResDialog(this);
                break;
            default:
                break;
        }
    }

    private AlertDialog delLayoutAlertDialog, delResAlertDialog;
    private TextView titleTextView, contentTextView;
    private EditText oneEditText, twoEditText;
    private Button cancelBtn, sureBtn;

    public void delLayoutDialog(final Context context) {
        delLayoutAlertDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_fir_dialog, null);
        titleTextView = (TextView) view.findViewById(R.id.tv_pwd_title);
        contentTextView = (TextView) view.findViewById(R.id.tv_first_entry_pwd_hints);
        oneEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd);
        twoEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd_confirm);
        cancelBtn = (Button) view.findViewById(R.id.btn_first_pwd_cancel);
        sureBtn = (Button) view.findViewById(R.id.btn_first_pwd_sure);

        titleTextView.setText(R.string.is_delete_layout);
        contentTextView.setVisibility(View.VISIBLE);
        contentTextView.setText(R.string.delete_layout_three);
        oneEditText.setVisibility(View.GONE);
        twoEditText.setVisibility(View.GONE);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delLayoutAlertDialog.dismiss();
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutCache.putLayoutCache("");
                show3sDialog().show();
                delLayoutAlertDialog.dismiss();
                countDownTimer.start();
            }
        });

        delLayoutAlertDialog.setView(view, 0, 0, 0, 0);
        delLayoutAlertDialog.show();
    }

    public void delResDialog(final Context context) {
        delResAlertDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_fir_dialog, null);
        titleTextView = (TextView) view.findViewById(R.id.tv_pwd_title);
        contentTextView = (TextView) view.findViewById(R.id.tv_first_entry_pwd_hints);
        oneEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd);
        twoEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd_confirm);
        cancelBtn = (Button) view.findViewById(R.id.btn_first_pwd_cancel);
        sureBtn = (Button) view.findViewById(R.id.btn_first_pwd_sure);

        titleTextView.setText(R.string.is_clear_no_layout);
        contentTextView.setVisibility(View.VISIBLE);
        contentTextView.setText(R.string.click_clear_no_layout);
        oneEditText.setVisibility(View.GONE);
        twoEditText.setVisibility(View.GONE);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delResAlertDialog.dismiss();
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResourceUpdate.deleteOtherFile();
                Toast.makeText(ServiceSetActivity.this, R.string.no_layout_delete, Toast.LENGTH_SHORT).show();
                delResAlertDialog.dismiss();
            }
        });

        delResAlertDialog.setView(view, 0, 0, 0, 0);
        delResAlertDialog.show();
    }

    private ProgressDialog show3sDialog() {
        ProgressDialog pd = new ProgressDialog(ServiceSetActivity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle(R.string.restart_app);// 设置ProgressDialog 标题
        pd.setMessage(getResources().getString(R.string.three_seconds_restart));
        pd.setIndeterminate(false);
        pd.setCancelable(false); // 设置ProgressDialog 是否可以按退回键取消
        return pd;
    }

    CountDownTimer countDownTimer = new CountDownTimer(3 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            CommonUtils.reLoadApp();
        }
    };

    public void sysTo(View view) {
        finish();
    }

    public interface OnReceivedProgressRun {
        void OnProgressRunReceived(int progress);
    }

    public static OnReceivedProgressRun onReceivedProgressRun;

    public static void setOnReceivedProgressRun(OnReceivedProgressRun onReceivedProgressRun) {
        ServiceSetActivity.onReceivedProgressRun = onReceivedProgressRun;
    }
}

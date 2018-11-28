package com.ideafactory.client.business.menuInfo.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.power.PowerOffTool;
import com.ideafactory.client.common.timer.layout.TimerReceiver;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.TYTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OffLineActivity extends BaseActivity {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private ImageButton oneScrImageButton, twoSrcImageButton, threeScrImageButton;
    private CheckBox logoCheckBox, dateCheckBox, timeCheckBox, rollingCheckBox;
    private ImageButton planOneBtn, planTwoBtn, planThreeBtn, planFourBtn;
    private Button okButton;
    private boolean land;
    private Map<String, String> map;
    private TextView musicTextView, rollingTextView, animationTextView;
    private String[] animationItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line);

        initView();
        setView();
    }

    private void setView() {
        oneScrImageButton.setOnFocusChangeListener(layoutOnFocusChangeListener);
        twoSrcImageButton.setOnFocusChangeListener(layoutOnFocusChangeListener);
        threeScrImageButton.setOnFocusChangeListener(layoutOnFocusChangeListener);
        logoCheckBox.setOnFocusChangeListener(checkBoxOnFocusChangeListener);

        String dateTime = sdf.format(new Date());
        dateCheckBox.setText(dateTime.substring(0, 10));
        timeCheckBox.setText(dateTime.substring(11));

        dateCheckBox.setOnFocusChangeListener(checkBoxOnFocusChangeListener);
        timeCheckBox.setOnFocusChangeListener(checkBoxOnFocusChangeListener);
        rollingCheckBox.setOnFocusChangeListener(checkBoxOnFocusChangeListener);
        planOneBtn.setOnFocusChangeListener(styleOnFocusChangeListener);
        planTwoBtn.setOnFocusChangeListener(styleOnFocusChangeListener);
        planThreeBtn.setOnFocusChangeListener(styleOnFocusChangeListener);
        planFourBtn.setOnFocusChangeListener(styleOnFocusChangeListener);

        okButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View paramView, boolean paramBoolean) {
                if (paramBoolean) {
                    paramView.setBackgroundColor(Color.parseColor("#dd6909"));
                } else {
                    paramView.setBackgroundColor(Color.parseColor("#001c58"));
                }

            }
        });
        musicTextView.setText(R.string.hint_bg_music);
        rollingTextView.setOnClickListener(rollingTimeListener);
        rollingTextView.setOnFocusChangeListener(checkBoxOnFocusChangeListener);
        animationTextView.setOnClickListener(animationListener);
        animationTextView.setOnFocusChangeListener(checkBoxOnFocusChangeListener);

        String effNo = getString(R.string.effect_no);
        String EffVer = getString(R.string.effect_ver);
        String EffEnlarge = getString(R.string.effect_enlarge);
        String EffLeft = getString(R.string.effect_left);
        String EffRight = getString(R.string.effect_right);
        String EffRotate = getString(R.string.effect_rotate);
        String EffTran = getString(R.string.effect_transparency);
        String EffEnlargeRotate = getString(R.string.effect_enlarge_rotate);
        animationItems = new String[]{effNo, EffVer, EffEnlarge, EffLeft, EffRight, EffRotate, EffTran, EffEnlargeRotate};
    }

    private void initView() {
        oneScrImageButton = (ImageButton) findViewById(R.id.ibtn_layout_one);
        twoSrcImageButton = (ImageButton) findViewById(R.id.ibtn_layout_two);
        threeScrImageButton = (ImageButton) findViewById(R.id.ibtn_layout_three);
        logoCheckBox = (CheckBox) findViewById(R.id.cb_off_selectHead);
        dateCheckBox = (CheckBox) findViewById(R.id.cb_off_date);
        timeCheckBox = (CheckBox) findViewById(R.id.cb_off_time);
        rollingCheckBox = (CheckBox) findViewById(R.id.cb_off_selectFooter);
        planOneBtn = (ImageButton) findViewById(R.id.ibtn_off_plan1);
        planTwoBtn = (ImageButton) findViewById(R.id.ibtn_off_plan2);
        planThreeBtn = (ImageButton) findViewById(R.id.ibtn_off_plan3);
        planFourBtn = (ImageButton) findViewById(R.id.ibtn_off_plan4);

        okButton = (Button) findViewById(R.id.btn_off_ok);
        musicTextView = (TextView) findViewById(R.id.tv_menu_offline_music);
        rollingTextView = (TextView) findViewById(R.id.tv_offline_rolling_time);
        animationTextView = (TextView) findViewById(R.id.tv_menu_offline_animation);

        land = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        // 初始化数据
        map = new HashMap<>();
        map.put("layout", "1");
        map.put("sytle", "1");
        // 加载配置
        loadingConfiguration();
    }

    /**
     * 设置图片轮播时间
     */
    private String[] items = new String[]{"5", "8", "10", "15", "20", "30", "60"};
    View.OnClickListener rollingTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OffLineActivity.this);
            builder.setTitle(R.string.please_select_time)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rollingTextView.setText(items[which]);
                        }
                    }).create().show();
        }
    };

    /**
     * 设置图片切换效果
     */
    View.OnClickListener animationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OffLineActivity.this);
            builder.setTitle(R.string.please_select_animation)
                    .setItems(animationItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            animationTextView.setText(animationItems[which]);
                        }
                    }).create().show();
        }
    };

    /**
     * 四个方案的监听
     */
    private View.OnFocusChangeListener styleOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.ibtn_off_plan1:
                    if (hasFocus) {
                        planOneBtn.setBackground(getResources().getDrawable(R.mipmap.one_ok));
                    } else {
                        planOneBtn.setBackground(getResources().getDrawable(R.mipmap.one));
                    }
                    break;
                case R.id.ibtn_off_plan2:
                    if (hasFocus) {
                        planTwoBtn.setBackground(getResources().getDrawable(R.mipmap.two_ok));
                    } else {
                        planTwoBtn.setBackground(getResources().getDrawable(R.mipmap.two));
                    }
                    break;
                case R.id.ibtn_off_plan3:
                    if (hasFocus) {
                        planThreeBtn.setBackground(getResources().getDrawable(R.mipmap.three_ok));
                    } else {
                        planThreeBtn.setBackground(getResources().getDrawable(R.mipmap.three));
                    }
                    break;
                case R.id.ibtn_off_plan4:
                    if (hasFocus) {
                        planFourBtn.setBackground(getResources().getDrawable(R.mipmap.four_ok));
                    } else {
                        planFourBtn.setBackground(getResources().getDrawable(R.mipmap.four));
                    }
            }
        }
    };

    /**
     * 四个方案选择样式
     */
    public void isSetPlan(View view) {
        planOneBtn.setImageDrawable(null);
        planTwoBtn.setImageDrawable(null);
        planThreeBtn.setImageDrawable(null);
        planFourBtn.setImageDrawable(null);

        String style = "1";
        switch (view.getId()) {
            case R.id.ibtn_off_plan1:
                planOneBtn.setImageDrawable(getResources().getDrawable(R.mipmap.subtitle_active));
                break;
            case R.id.ibtn_off_plan2:
                planTwoBtn.setImageDrawable(getResources().getDrawable(R.mipmap.subtitle_active));
                style = "2";
                break;
            case R.id.ibtn_off_plan3:
                planThreeBtn.setImageDrawable(getResources().getDrawable(R.mipmap.subtitle_active));
                style = "3";
                break;
            case R.id.ibtn_off_plan4:
                planFourBtn.setImageDrawable(getResources().getDrawable(R.mipmap.subtitle_active));
                style = "4";
        }
        map.put("sytle", style);
    }

    private View.OnFocusChangeListener checkBoxOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setBackground(getResources().getDrawable(R.drawable.input9));
            } else {
                v.setBackground(null);
            }
        }
    };


    /**
     * 选择头部
     */
    public void isSetHead(View view) {
        dateCheckBox.setEnabled(logoCheckBox.isChecked());
        timeCheckBox.setEnabled(logoCheckBox.isChecked());
    }

    /**
     * 选择几分屏
     **/
    private View.OnFocusChangeListener layoutOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.ibtn_layout_one:
                    if (hasFocus) {
                        //横竖屏
//                        oneScrImageButton.setBackground(getResources().getDrawable(land ? R.mipmap.layout1_1_focus_land : R.mipmap.layout1_1_focus));
                        oneScrImageButton.setBackground(getResources().getDrawable(R.mipmap.one_screen_t));
                    } else {
                        oneScrImageButton.setBackground(getResources().getDrawable(R.mipmap.one_screen));
                    }
                    break;
                case R.id.ibtn_layout_two:
                    if (hasFocus) {
                        twoSrcImageButton.setBackground(getResources().getDrawable(R.mipmap.two_screen_t));
                    } else {
                        twoSrcImageButton.setBackground(getResources().getDrawable(R.mipmap.two_screen));
                    }
                    break;
                case R.id.ibtn_layout_three:
                    if (hasFocus) {
                        threeScrImageButton.setBackground(getResources().getDrawable(R.mipmap.three_screen_t));
                    } else {
                        threeScrImageButton.setBackground(getResources().getDrawable(R.mipmap.three_screen));
                    }
            }
        }
    };

    /**
     * 选择几分屏上的对勾
     */
    public void screenChoice(View view) {
        oneScrImageButton.setImageDrawable(null);
        twoSrcImageButton.setImageDrawable(null);
        threeScrImageButton.setImageDrawable(null);
        String layout = "1";
        switch (view.getId()) {
            case R.id.ibtn_layout_one:
                oneScrImageButton.setImageDrawable(getResources().getDrawable(R.mipmap.screen_active));
                break;
            case R.id.ibtn_layout_two:
                twoSrcImageButton.setImageDrawable(getResources().getDrawable(R.mipmap.screen_active));
                layout = "2";
                break;
            case R.id.ibtn_layout_three:
                threeScrImageButton.setImageDrawable(getResources().getDrawable(R.mipmap.screen_active));
                layout = "3";
        }
        map.put("layout", layout);
    }

    private void loadingConfiguration() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/mnt/sdcard/hsd/property/341358386";
        String layout = TYTool.readText(path);
        if (!layout.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONArray(layout).getJSONObject(0);
                // 设置布局
                JSONArray jsonArray = jsonObject.getJSONArray("center");
                int size = jsonArray.length();
                if (size == 1) {
                    screenChoice(oneScrImageButton);
                } else if (size == 2) {
                    screenChoice(twoSrcImageButton);
                } else if (size == 3) {
                    screenChoice(threeScrImageButton);
                }
                // 设置图片轮播时间
                String playTime = jsonArray.getJSONObject(0).getJSONObject("imageDetail").getString("playTime");
                if (!"5".equals(playTime)) {
                    rollingTextView.setText(playTime);
                }
                // 设置图片切换效果
                String imagePlayType = jsonArray.getJSONObject(0).getJSONObject("imageDetail").getString("imagePlayType");
                if (!"0".equals(imagePlayType)) {
                    animationTextView.setText(animationItems[Integer.parseInt(imagePlayType)]);
                }
                // 设置显示头部
                JSONObject header = jsonObject.getJSONObject("header");
                boolean headerDisplay = header.getBoolean("enabled");
                if (!headerDisplay) {// 不显示头部
                    logoCheckBox.setChecked(false);
                    isSetHead(logoCheckBox);
                }
                // 设置显示底部
                JSONObject footer = jsonObject.getJSONObject("footer");
                boolean footerDisplay = footer.getBoolean("enabled");
                if (!footerDisplay) {
                    rollingCheckBox.setChecked(false);
                }
                // 设置样式
                JSONObject style = null;
                if (headerDisplay)
                    style = header;
                else if (footerDisplay)
                    style = footer;
                if (style != null) {
                    Map<String, ImageButton> map = new HashMap<String, ImageButton>();
                    map.put("#e8edf0#ee745f", planOneBtn);
                    map.put("#6a005f#ffffff", planTwoBtn);
                    map.put("#10ac38#ffffff", planThreeBtn);
                    map.put("#000000#d4db64", planFourBtn);
                    String key = style.getString("background") + style.getString("fontColor");
                    isSetPlan(map.get(key));
                }
                // 设置时间格式
                String timeFormat = header.getString("timeFormat");
                if ("yyyy-MM-dd".equals(timeFormat)) {
                    timeCheckBox.setChecked(false);
                } else if (" kk:mm".equals(timeFormat)) {
                    dateCheckBox.setChecked(false);
                } else if (timeFormat.isEmpty()) {
                    dateCheckBox.setChecked(false);
                    timeCheckBox.setChecked(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private AlertDialog completedDialog;
    private TextView titleTextView, contentTextView;
    private EditText oneEditText, twoEditText;
    private Button cancelBtn, sureBtn;

    public void completedDialog(final Context context) {
        completedDialog = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.sys_pwd_fir_dialog, null);
        titleTextView = (TextView) view.findViewById(R.id.tv_pwd_title);
        contentTextView = (TextView) view.findViewById(R.id.tv_first_entry_pwd_hints);
        oneEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd);
        twoEditText = (EditText) view.findViewById(R.id.et_first_entry_pwd_confirm);
        cancelBtn = (Button) view.findViewById(R.id.btn_first_pwd_cancel);
        sureBtn = (Button) view.findViewById(R.id.btn_first_pwd_sure);

        titleTextView.setText(R.string.is_sure_publish_offline);
        contentTextView.setVisibility(View.VISIBLE);
        contentTextView.setText(R.string.hint_publish_offline);
        oneEditText.setVisibility(View.GONE);
        twoEditText.setVisibility(View.GONE);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completedDialog.dismiss();
            }
        });
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOfflineLayout();
                completedDialog.dismiss();
            }
        });

        completedDialog.setView(view, 0, 0, 0, 0);
        completedDialog.show();
    }

    /**
     * 完成按钮，保存配置
     */
    public void completed(View view) {
        completedDialog(this);
    }

    public void runOfflineLayout() {
        LayoutCache.putLayoutPosition("1");
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        // 选择布局
        try {
            InputStream is = getAssets().open("layout/layout" + map.get("layout") + (!map.get("layout").equals("1") && land ? "_land" : "") + ".txt");
            br = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            JSONArray jsonArray = new JSONArray(sb.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String[] style = null;

            if (logoCheckBox.isChecked() || rollingCheckBox.isChecked()) {
                String[] style1 = {"#e8edf0", "#ee745f", "38", "隶书"};
                String[] style2 = {"#6a005f", "#ffffff", "38", "楷体"};
                String[] style3 = {"#10ac38", "#ffffff", "38", "微软雅黑"};
                String[] style4 = {"#000000", "#d4db64", "38", "宋体"};
                String s = map.get("sytle");
                if ("1".equals(s))
                    style = style1;
                else if ("2".equals(s))
                    style = style2;
                else if ("3".equals(s))
                    style = style3;
                else if ("4".equals(s))
                    style = style4;
            }
            // 头部设置
            JSONObject header = jsonObject.getJSONObject("header");
            header.put("enabled", String.valueOf(logoCheckBox.isChecked()));// 是否显示头部
            if (logoCheckBox.isChecked()) {
                header.put("background", style[0]);// 背景颜色
                header.put("fontColor", style[1]);// 文字颜色
                header.put("fontSize", style[2]);// 文字大小
                header.put("fontFamily", style[3]);// 文字字体
                header.put("timeFormat", (dateCheckBox.isChecked() ? "yyyy-MM-dd" : "") + (timeCheckBox.isChecked() ? " kk:mm" : ""));// 时间格式
            }
            // 尾部设置
            JSONObject footer = jsonObject.getJSONObject("footer");
            footer.put("enabled", String.valueOf(rollingCheckBox.isChecked()));// 是否显示头部
            if (rollingCheckBox.isChecked()) {
                footer.put("background", style[0]);// 背景颜色
                footer.put("fontColor", style[1]);// 文字颜色
                footer.put("fontSize", style[2]);// 文字大小
                footer.put("fontFamily", style[3]);// 文字字体
            }
            // 设置图片轮播时间
            String playTime = rollingTextView.getText().toString();
            if (!"5".equals(playTime) && playTime.matches("\\d{1,2}")) {
                JSONArray centers = jsonObject.getJSONArray("center");
                for (int i = 0; i < centers.length(); i++) {
                    centers.getJSONObject(i).getJSONObject("imageDetail").put("playTime", playTime);
                }
            }
            //设置图片切换效果
            //0-无动画   1-竖切飞入   2-放大切入   3-左边切入   4-右边切入   5-旋转切入   6-透明度切入   7-放大旋转切入
            String animation = animationTextView.getText().toString();
            for (int i = 0; i < animationItems.length; i++) {
                if (animationItems[i].equals(animation)) {
                    animation = String.valueOf(i);
                }
            }
            JSONArray centers = jsonObject.getJSONArray("center");
            for (int i = 0; i < centers.length(); i++) {
                centers.getJSONObject(i).getJSONObject("imageDetail").put("imagePlayType", animation);
            }
            // 定时开关机,不设置定时开关机
            PowerOffTool.getPowerOffTool().setLocalRuntime(null, null);
            // 保存布局配置
            LayoutCache.putLayoutCache(jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TimerReceiver.screen();
    }

    public void sysTo(View view) {
        finish();
    }
}

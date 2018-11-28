package com.ideafactory.client.common.message;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ideafactory.client.MainActivity;
import com.ideafactory.client.business.draw.layout.bean.XBHActions;
import com.ideafactory.client.business.draw.review.SoundControl;
import com.ideafactory.client.business.menuInfo.activity.MenuInfoActivity;
import com.ideafactory.client.business.offline.activity.SwitchLayout;
import com.ideafactory.client.business.operationalAds.AdsManager;
import com.ideafactory.client.business.push.PushImage;
import com.ideafactory.client.business.push.PushVideo;
import com.ideafactory.client.business.unicomscreen.UnicomBean2;
import com.ideafactory.client.business.unicomscreen.UnicomTool;
import com.ideafactory.client.business.unicomscreen.UnicomView;
import com.ideafactory.client.business.weichat.weichatutils.WeiChatSave;
import com.ideafactory.client.business.weichat.weichatutils.WeichatFragment;
import com.ideafactory.client.common.cache.LayoutCache;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.common.power.PowerOffTool;
import com.ideafactory.client.common.timer.layout.TimerReceiver;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.MachineDetial;
import com.ideafactory.client.util.RotateScreen;
import com.ideafactory.client.util.SerTool;
import com.ideafactory.client.util.SpUtils;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.ThreadUitls;
import com.ideafactory.client.util.UpdateVersionControl;
import com.ideafactory.client.util.logutils.LogUtils;
import com.ideafactory.client.util.screen.ScreenShot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * xmpp消息处理
 *
 * @author Administrator
 */
public class CoreInfoHandler {
    private static final String TAG = "CoreInfoHandler";

    private static final int ONLINE_TYPE = 1;// 上线
    private static final int CONTENT_TYPE = 2;// 内容修改
    private static final int VOICE_TYPE = 3;// 声音
    private static final int CUTSCREN_TYPE = 4;// 截屏
    private static final int RUNSET_TYPE = 5;// 设备开关机设置
    private static final int SHOW_SERNUM = 6;// 显示设备编号
    private static final int SHOW_VERSION = 7;// 显示版本号
    private static final int SHOW_DISK_IFNO = 8;// 获取磁盘容量
    private static final int POWER_RELOAD = 9;// 设备 开机 重启
    private static final int PUSH_TO_UPDATE = 10;//软件升级
    private final static int HARDWARE_UPDATE = 11;//通知设备硬件更新,上传设备信息
    private final static int HARDWARESCREENROTATE_UPDATE = 12;//屏幕旋转
    private final static int SET_CLEAR_LAYOUT = 13;//一键删除布局
    private final static int PUSH_MESSAGE = 14;//推送广告消息，快发字幕
    private final static int REFERSH_RENEWAL_STATUS = 15;//欠费停机设备支付
    private final static int CHANNEL_TYPE = 16;//输入源选择
    private final static int PUSH_IMAGE = 17;//手机端快发图片
    private final static int FACE_DETECT = 18;//开通人脸识别
    private final static int EARTH_CINEMA = 19;//大地影院
    private final static int UNICOM_SCREEN = 20;//联屏
    private final static int IMAGE_PUSH = 21;//推送的图片
    private final static int VIDEO_PUSH = 22;//推送的视频
    private final static int ADSINFO_PUSH=23;//推送的自运营广告
    private final static int SHARESTATUS_UPDATE=24;//是否是广告机状态更改

    public static void messageReceived(String message) {
        LogUtils.e(TAG, "接收消息：" + message);
        try {
            JSONObject mesJson = new JSONObject(message);
            Integer type = mesJson.getInt("type");
            JSONObject contentJson;
            switch (type.intValue()) {
                case ONLINE_TYPE:
                    // 系统登录
                    contentJson = mesJson.getJSONObject("content");
                    if (!contentJson.isNull("serNum")) {
//                     //第一次系统启动的时候服务器没有设备详细信息，需要向设备传消息
                        MachineDetial.getInstance().upLoadHardWareMessage();
                        String serNum = (String) TYTool.getJsonObj(contentJson, "serNum", "");
                        String decName = (String) TYTool.getJsonObj(contentJson, "deviceName", "");
                        decName = new String(decName.getBytes("ISO-8859-1"), "UTF-8");
                        String pwd = (String) TYTool.getJsonObj(contentJson, "pwd", "");
                        String ticketId = (String) TYTool.getJsonObj(contentJson, "ticket", "");
                        String deviceQrCode = (String) TYTool.getJsonObj(contentJson, "deviceQrCode", "");//手机端绑定的二维码

                        //注册成功失败的提示
                        String ret = (String) TYTool.getJsonObj(contentJson, "status", "");
                        //设备服务状态
                        String runStatus = TYTool.getJsonObj(contentJson, "runStatus", "").toString();
                        //设备是否绑定
                        String bindStatus = TYTool.getJsonObj(contentJson, "bindStatus", "").toString();
                        //设备过期时间
                        String expireDate = TYTool.getJsonObj(contentJson, "expireDate", "").toString();
                        //人脸识别是否开通 0表示过期或未开通  1表示开通
                        String camera = TYTool.getJsonObj(contentJson, "camera", "").toString();
                        //是否显示  0不显示 1显示
                        String cameraShow = TYTool.getJsonObj(contentJson, "cameraShow", "").toString();
                        //0不是镜面    1是镜面
                        String isMirror = TYTool.getJsonObj(contentJson, "isMirror", "").toString();
                        LogUtils.e(TAG, "登录：" + serNum + "_" + decName + "  人脸：" + camera + "/" + cameraShow);

                       //0不是广告机 1是广告机
                        String shareStatus = TYTool.getJsonObj(contentJson, "shareStatus", "").toString();
                        SpUtils.saveString(APP.getContext(),SpUtils.DEVICETYPE,shareStatus);

                        LayoutCache.putExpireDate(expireDate);
                        LayoutCache.putBindStatus(bindStatus);
                        LayoutCache.putRunStatus(runStatus);
                        LayoutCache.putSerNumber(serNum);
                        LayoutCache.putPwd(pwd);
                        LayoutCache.putDecName(decName);
                        LayoutCache.putDeviceQrCode(deviceQrCode);
                        LayoutCache.putFaceDetect(camera);
                        LayoutCache.putFaceShow(cameraShow);
                        LayoutCache.putIsMirror(isMirror);

                        WeiChatSave.saveString(APP.getContext(), WeiChatSave.WEICHAT_CHAT_ID, ticketId);
                        WeiChatSave.saveString(APP.getContext(), WeiChatSave.DEVICEQRCODE, deviceQrCode);

                        //是否有密码
                        String password = TYTool.getJsonObj(contentJson, "password", "").toString();
                        LogUtils.e(TAG, "*****" + password);
                        if (TextUtils.isEmpty(password) || password.equals(" ") || password.equals("null")) {
                            SpUtils.saveString(APP.getContext(), SpUtils.MENU_PWD, "");
                        } else {
                            SpUtils.saveString(APP.getContext(), SpUtils.MENU_PWD, password);
                        }

                        if (SwitchLayout.onReceivedSn != null) {
                            SwitchLayout.onReceivedSn.Onreceived(serNum, pwd, ret, deviceQrCode);
                        }

                        if (MainActivity.onReceivedDecRun != null) {
                            MainActivity.onReceivedDecRun.OnDecRunReceived(runStatus);
                        }

                        //微信直发用ticketid获取二维码的链接
                        if (WeichatFragment.onReceivedECode != null) {
                            WeichatFragment.onReceivedECode.eCodeReceived(ticketId);
                        }
                    }

                    String runKey = (String) TYTool.getJsonObj(contentJson, "runKey", "");
                    LayoutCache.putRunKey(runKey);

                    Integer dtype = (Integer) TYTool.getJsonObj(contentJson, "dtype", -1);
                    if (dtype == 2) {
                        SerTool.stopFloatWm();
                    }
                    LayoutCache.putDTypeKey(dtype + "");//-1未激活  0未授权  1网络版  2单机版
                    if (SwitchLayout.onReceivedSn != null) {
                        SwitchLayout.onReceivedSn.OnreceivedDtype(dtype);
                    }
                    break;
                case CONTENT_TYPE:// 内容更新
                    TYTool.releaseAllView();
                    TYTool.downloadResource();
                    break;
                case VOICE_TYPE:// 声音控制
                    JSONObject jsonObject = mesJson.getJSONObject("content");
                    if (jsonObject != null) {
                        SoundControl.setMusicSound(jsonObject.getDouble("voice"));
                    }
                    break;
                case CUTSCREN_TYPE:
                    ThreadUitls.runInThread(new Runnable() {// 截图控制
                        @Override
                        public void run() {
                            ScreenShot.getInstanse().shootScreen();
                        }
                    });
                    break;
                case RUNSET_TYPE:
                    ThreadUitls.runInThread(new Runnable() {
                        @Override
                        public void run() {// 开关机时间设置
                            PowerOffTool.getPowerOffTool().getPowerOffTime(HeartBeatClient.getDeviceNo());
                        }
                    });
                    break;
                case SHOW_SERNUM:
                    contentJson = (JSONObject) TYTool.getJsonObj(mesJson, "content", null);
                    if (contentJson != null) {
                        Integer showType = (Integer) TYTool.getJsonObj(contentJson, "showType", null);
                        if (showType != null && showType == 0) {//状态栏  视美泰主板
                            Integer showValue = (Integer) TYTool.getJsonObj(contentJson, "showValue", null);
                            if (showValue == 0) {//显示
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), true);
                            } else if (showValue == 1) {//隐藏
                                APP.getSmdt().smdtSetStatusBar(APP.getContext().getApplicationContext(), false);
                            }
                        } else { // 显示设备编号
                            TYTool.showTitleTip(LayoutCache.getSerNumber());
                        }
                    }
                    break;
                case SHOW_VERSION:// 版本信息
                    ResourceUpdate.uploadAppVersion();
                    break;
                case SHOW_DISK_IFNO:
                    contentJson = mesJson.getJSONObject("content");
                    Integer flag = (Integer) TYTool.getJsonObj(contentJson, "flag", null);
                    if (flag != null) {
                        if (flag == 0) { //显示
                            ResourceUpdate.uploadDiskInfo();
                        } else if (flag == 1) {// 清理磁盘
                            ResourceUpdate.deleteOtherFile();
                            ResourceUpdate.uploadDiskInfo();
                        }
                    }
                    break;
                case POWER_RELOAD:// 机器重启
                    contentJson = mesJson.getJSONObject("content");
                    Integer restart = (Integer) TYTool.getJsonObj(contentJson, "restart", null);
                    if (restart != null) {
                        if (restart == 0) {
                            ProgressDialog progressDialog = TYTool.coreInfoShow3sDialog();
                            progressDialog.setTitle("关机");
                            progressDialog.setMessage("3秒后将关闭设备");
                            progressDialog.show();
                            TYTool.powerShutDown.start();
                        } else if (restart == 1) {
                            ProgressDialog progressDialog = TYTool.coreInfoShow3sDialog();
                            progressDialog.setTitle("重启");
                            progressDialog.setMessage("3秒后将重启设备");
                            progressDialog.show();
                            TYTool.restart.start();
                        }
                    }
                    break;
                case PUSH_TO_UPDATE:
                    TYTool.updatePd();
                    UpdateVersionControl.getInstance().checkUpdate();
                    setOnReceivedProgressRun(new OnReceivedProgressRun() {
                        @Override
                        public void OnProgressRunReceived(int progress) {
                            TYTool.pd.setProgress(progress);//给进度条设置数值
                            if (progress == 100) {
                                TYTool.pd.dismiss();
                            }
                        }
                    });
                    break;
                case HARDWARE_UPDATE:
                    MachineDetial.getInstance().upLoadHardWareMessage();
                    break;
                case HARDWARESCREENROTATE_UPDATE://屏幕旋转
                    contentJson = mesJson.getJSONObject("content");
                    Integer rotate = (Integer) TYTool.getJsonObj(contentJson, "screenRotate", null);
                    LayoutCache.putRotate(String.valueOf(rotate));//保存要设置的旋转角度
                    if (TYTool.boardIsXBH()) {
                        RotateScreen.getInstance().rotateScreenXBH(String.valueOf(rotate));
                    } else if (TYTool.boardIsJYD()) {
                        RotateScreen.getInstance().rotateScreenJYD(String.valueOf(rotate));
                    } else {
                        RotateScreen.getInstance().rotateScreen(String.valueOf(rotate));
                    }
                    break;
                case SET_CLEAR_LAYOUT:
                    TYTool.destroyAllView();
                    break;
                case PUSH_MESSAGE:
                    contentJson = mesJson.getJSONObject("content");
                    Integer playtype = contentJson.getInt("playType");
                    JSONObject content = contentJson.getJSONObject("content");
                    switch (playtype) {
                        case 1:
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_LOCATION, content.getInt("location"));
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_FONTSIZE, content.getInt("fontSize"));
                            SpUtils.saveString(APP.getContext(), SpUtils.TV_BACKGROUD, content.getString("background"));
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_PLAYTYPE, content.getInt("playType"));
                            SpUtils.saveString(APP.getContext(), SpUtils.TV_FONTCOLOR, content.getString("fontColor"));
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_PLAYSPEED, content.getInt("playSpeed"));
                            SpUtils.saveString(APP.getContext(), SpUtils.TV_PLAYDATE, content.getString("playDate"));
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_PLAYTIME, content.getInt("playTime"));

                            SpUtils.saveString(APP.getContext(), SpUtils.TV_TEXT, contentJson.getString("text"));
                            try {
                                if (content.get("playCurTime") != null) {
                                    SpUtils.saveString(APP.getContext(), SpUtils.TV_PLAYCURTIME, content.optString
                                            ("playCurTime"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_TRANSPARENT, content.getInt("transparent"));//背景是否透明  1透明
                            SpUtils.saveInt(APP.getContext(), SpUtils.TV_SPEECHCOUNT, content.getInt("speechCount"));//语音播报
                            // 0不支持  1一次  2两次  -1循环播放

                            int playTime = content.getInt("playTime");

                            if (playTime != 0) {
                                TYTool.startFloatService();
                                TYTool.closeMessage(playTime);
                            } else {
                                TYTool.setMessageHandler();
                            }
                            break;
                        case 2:
                            TYTool.stopService();
                            break;
                    }
                    break;
                case REFERSH_RENEWAL_STATUS://欠费停机设备支付
                    contentJson = mesJson.getJSONObject("content");
                    String codePay = TYTool.getJsonObj(contentJson, "status", "").toString();
                    MenuInfoActivity.dealCodePay(codePay);
                    break;
                case CHANNEL_TYPE://输入信号源选择
                    contentJson = mesJson.getJSONObject("content");
                    int channel = contentJson.getInt("channel");
                    if (TYTool.boardIsXBH()) {
                        if (channel == 0) {
                            TYTool.sendBroadcast(XBHActions.CHANGE_TO_AV);
                        } else if (channel == 1) {
                            TYTool.sendBroadcast(XBHActions.CHANGE_TO_VGA);
                        } else if (channel == 2) {
                            TYTool.sendBroadcast(XBHActions.CHANGE_TO_HDMI);
                        }
                    } else {
                        Toast.makeText(APP.getContext(), "暂不支持该功能", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PUSH_IMAGE:
                    JSONObject appContentJson = mesJson.getJSONObject("content");
                    String imageContent = appContentJson.getString("content");
                    TYTool.downloadAPPImage(imageContent);
                    break;
                case FACE_DETECT:
                    LayoutCache.putFaceDetect("1");
                    JSONObject faceJson = mesJson.getJSONObject("content");
                    String cameraShow = (String) TYTool.getJsonObj(faceJson, "cameraShow", "");
                    LayoutCache.putFaceShow(cameraShow);
                    CommonUtils.reLoadApp();
                    break;
                case EARTH_CINEMA:
                    //TODO 现在服务器没json记录  目前解决 按离线布局标识离线版 开机不去服务器判断
                    LayoutCache.putLayoutPosition("1");
                    JSONObject JsonCinema = mesJson.getJSONObject("content");
                    String jsonInfo = JsonCinema.getString("content");
                    LayoutCache.putLayoutCache(jsonInfo);//保存layout布局文件
                    TimerReceiver.screen();
                    break;
                case IMAGE_PUSH:
                    PushImage.play(mesJson);
                    break;
                case VIDEO_PUSH:
                    PushVideo.play(mesJson);
                    break;
                case UNICOM_SCREEN:
                    JSONObject JsonScreen2 = mesJson.getJSONObject("content");
                    String status = JsonScreen2.getString("start");
                    switch (status) {
                        case "1": //一次性
                            UnicomBean2 bean2 = new Gson().fromJson(JsonScreen2.toString(), UnicomBean2.class);
                            new UnicomView(APP.getContext(), bean2);
                            SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_URL, null);
                            APP.getMainActivity().playHandler.removeMessages(1);
                            break;
                        case "2": //定时
                            SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_URL, JsonScreen2.toString());
                            UnicomBean2 bean3 = new Gson().fromJson(JsonScreen2.toString(), UnicomBean2.class);
                            new UnicomView(APP.getContext(), bean3);
                            APP.getMainActivity().playHandler.removeMessages(1);
                            break;
                        case "3": //停止
                            UnicomTool.stopVideoService();
                            UnicomTool.stopImgService();
                            UnicomTool.stopScreenService();
                            break;
                        case "4": //清除
                            APP.getMainActivity().playHandler.removeMessages(1);
                            SpUtils.saveString(APP.getContext(), SpUtils.UNICOM_URL, null);
                            UnicomTool.stopVideoService();
                            UnicomTool.stopImgService();
                            UnicomTool.stopScreenService();
                            break;
                    }
                    break;

                case ADSINFO_PUSH://自运营广告推送
                    String deviceType = SpUtils.getString(APP.getContext(), SpUtils.DEVICETYPE, "");
                    if ("1".equals(deviceType)){
                        TYTool.downloadAdsResource(null);
                    }else {
                        SpUtils.saveString(APP.getContext(),SpUtils.DEVICETYPE,"1");
                        AdsManager.getInstance().init();
                    }
                   break;
                case SHARESTATUS_UPDATE://是否是广告机状态更改
                    if (mesJson.has("shareStatus")){
                        String shareStatus = mesJson.getString("shareStatus");
                        String dType = SpUtils.getString(APP.getContext(), SpUtils.DEVICETYPE, "");
                        //存储广告机状态
                        SpUtils.saveString(APP.getContext(),SpUtils.DEVICETYPE,shareStatus);
                    }
                    break;

                default:
                    break;
            }
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public interface OnReceivedProgressRun {
        void OnProgressRunReceived(int progress);
    }

    public static OnReceivedProgressRun onReceivedProgressRun;

    public static void setOnReceivedProgressRun(OnReceivedProgressRun onReceivedProgressRun) {
        CoreInfoHandler.onReceivedProgressRun = onReceivedProgressRun;
    }
}
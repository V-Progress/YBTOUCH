package com.ideafactory.client.business.localnetcall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ideafactory.client.R;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.draw.review.SoundControl;
import com.ideafactory.client.common.net.ResourceUpdate;
import com.ideafactory.client.heartbeat.APP;
import com.ideafactory.client.heartbeat.BaseActivity;
import com.ideafactory.client.util.CommonUtils;
import com.ideafactory.client.util.HandleMessageUtils;
import com.ideafactory.client.util.TYTool;
import com.ideafactory.client.util.TextToSpeechAll;
import com.ideafactory.client.util.xutil.MyXutils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 局域网叫号系统
 */
public class CallNum {

    private static CallNum callNum = null;

    public static CallNum callNumInstance() {
        if (callNum == null) {
            synchronized (CallNum.class) {
                if (callNum == null) {
                    callNum = new CallNum();
                }
            }
        }
        return callNum;
    }

    private CallNum() {
        init();
    }

    /**
     * 连接界面显示的service
     */
    public class ConService implements ServiceConnection {
        //获取连接
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            callTextService = (CallTextService) service;
            if (callTextService != null && layoutPosition != null) {
                callTextService.setShowLayoutParameter(layoutPosition);
            }
        }

        //失去连接
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    String keyCodeStr = "";
    String timeleCodeStr = "";//暂时存放的信息
    long lastTime = 0;
    int timeCount;

    private CallTextService callTextService;
    private ConService conn;
    AudioManager mAudioManager = (AudioManager) APP.getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    private SocketControl socketControl;
    private boolean isSingleInited = false;
    public static String mainIpAddress;
    private String winName;
    private String localIpAdress = CommonUtils.getIpAddress();
    private StringBuffer sb = new StringBuffer();//扫码叫号

    private static final String TAG = "CallNum";

    public void handKeyCode(int keyCode, KeyEvent event) {
        Log.d(TAG, "handKeyCode: " + keyCode);
        if (keyCode >= 144 && keyCode <= 153) {// 数字键
            if (isSingleDev == 1) {
                if (!isSingleInited) {
                    initTextToSpeech();
                    initService();
                    isSingleInited = true;
                }
            }
            keyCodeStr += (keyCode - 144) + " ";
            timeleCodeStr = keyCodeStr;//叫号信息转移
            sendCallCodeToShow(timeleCodeStr);
        } else if (keyCode == 158) {
            keyCodeStr += ".";
            timeleCodeStr = keyCodeStr;//叫号信息转移
            sendCallCodeToShow(timeleCodeStr);
        } else if (keyCode == 155) {//按*键表示
            keyCodeStr += "-";
            timeleCodeStr = keyCodeStr;//叫号信息转移
            sendCallCodeToShow(timeleCodeStr);
        } else if (keyCode == 160) { //enter键
            if (keyCodeStr.equals("1 1 2 2 3 3 ")) {
                showSettingDialog();
            } else {
                long current = SystemClock.elapsedRealtime();
                long intervalTime = Math.abs(current - lastTime);
                //一秒钟最多响应两次
                if (intervalTime < 1000) {
                    timeCount++;
                    if (timeCount < 2) {
                        if (isSingleDev == 1) {
                            textToSpeech(true);
                        } else {
                            textToSpeech(false);
                        }
                    }
                } else {
                    timeCount = 0;
                    if (isSingleDev == 1) {
                        textToSpeech(true);
                    } else {
                        textToSpeech(false);
                    }
                    lastTime = current;
                }
            }
            keyCodeStr = "";
            sendCallCodeToShow("");
        } else if (keyCode == 67) {// 按删除键都取消
            keyCodeStr = "";
            timeleCodeStr = "";
            sendCallCodeToShow(timeleCodeStr);
        } else if (keyCode == 156) {//声音减
            initTextToSpeech();
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_LOWER, AudioManager
                    .FX_FOCUS_NAVIGATION_UP);
            TextToSpeechAll.getInstance().textToSpeech.speak("减少叫号音量", TextToSpeech.QUEUE_FLUSH, textToSpeechMap);
        } else if (keyCode == 157) {//声音加
            initTextToSpeech();
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, AudioManager
                    .FX_FOCUS_NAVIGATION_UP);
            TextToSpeechAll.getInstance().textToSpeech.speak("增加叫号音量", TextToSpeech.QUEUE_FLUSH, textToSpeechMap);
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (builder != null && builder.isShowing()) {
                builder.dismiss();
            }
        } else if (keyCode != 82 && keyCode != 4) {
            sb.append((keyCode - 7) + "");
            if (sb.length() == 27) {
                String str = sb.toString().substring(14, sb.toString().length());

                Map<String, String> map = new HashMap<>();
                map.put("orderSernum", "busself" + str);
                MyXutils.getInstance().post(ResourceUpdate.SCAN_TO_CALL, map, new MyXutils.XCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        parseJson(result);
                    }

                    @Override
                    public void onError(Throwable ex) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
                sb.delete(0, sb.toString().length());
            }
        }
    }

    private void sendCallCodeToShow(String timeleCode) {
        if (CallQueueView.onReceivedQueueAdd != null) {
            CallQueueView.onReceivedQueueAdd.receivedOnTime(timeleCode);
        }
    }

    /**
     * 解析扫码叫号信息
     *
     * @param result
     */
    private void parseJson(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject orderNumJson = jsonObject.getJSONObject("busSelfOrder");
            timeleCodeStr = (String) TYTool.getJsonObj(orderNumJson, "orderCallNum", "");
            if (isSingleDev == 1) {
                textToSpeech(true);
            } else {
                textToSpeech(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //单机版和局域网的分别处理
    private void textToSpeech(boolean singleDevice) {
        //判断叫号信息中是否有数字，没有数字就不进行下一步处理了
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(timeleCodeStr);
        if (m.find()) {
            if (singleDevice) {
                speakVoice();//本地
            } else {
                toSpeak();//局域网
            }
        }
    }

    private static String mainIpMessage;

    private int isMianServer = 0;//0表示不是服务器

    private int isSingleDev = 1;

    /**
     * 初始化socket
     */
    public void initSocketController() {
        socketControl = socketControl.getInstance();
        socketControl.setServerIpAddress(mainIpAddress);
        isMianServer = Integer.parseInt(CallNumCache.getIsMainServer());
        if (isMianServer == 0) {
            socketControl.setIsMainServer(false);
        } else {
            socketControl.setIsMainServer(true);
        }
        socketControl.connectSocket();
    }

    /**
     * 分割字符串，获取ip和窗口名称
     */
    public void getIpAddressWinName() {
        String[] ipWinName = mainIpMessage.split(",");
        if (ipWinName.length == 2) {
            mainIpAddress = ipWinName[0];
            winName = ipWinName[1];
        }
    }

    public void init() {
        winName = "";
        isSingleDev = Integer.parseInt(CallNumCache.getIsSingleDevice());
        mainIpMessage = CallNumCache.getMainIpadress();

        if (!TextUtils.isEmpty(mainIpMessage)) {
            getIpAddressWinName();
            initTextToSpeech();
            initService();
        }

        if (isSingleDev != 1) { //单个设备
            if (!TextUtils.isEmpty(mainIpMessage)) {
                initSocketController();
            }
        }
    }

    private Dialog builder;

    //展示显示设置的界面
    private void showSettingDialog() {

        if (callTextService != null) {
            callTextService.hideMessageWindow();
        }

        View view = LayoutInflater.from(BaseActivity.getActivity()).inflate(R.layout.c_server_setting_layout, null);
        final RadioGroup radiogroup_server_type = (RadioGroup) view.findViewById(R.id.radiogroup_server_type);
        final RadioGroup radiogroup_fuwu_type = (RadioGroup) view.findViewById(R.id.radiogroup_fuwu_type);
        final LinearLayout setting_fuwu_type = (LinearLayout) view.findViewById(R.id.setting_fuwu_type);
        final LinearLayout setting_ip_adress = (LinearLayout) view.findViewById(R.id.setting_ip_adress);
        final EditText mainserver_ip_input = (EditText) view.findViewById(R.id.mainserver_ip_input);
        final EditText et_ipname = (EditText) view.findViewById(R.id.et_ipname);
        final EditText et_socket_port = (EditText) view.findViewById(R.id.et_socket_port);
        final TextView tv_local_ip = (TextView) view.findViewById(R.id.tv_local_ip);
        tv_local_ip.setText("本机IP:" + localIpAdress);

        boolean savedServerBoolean;
        String isServer = CallNumCache.getIsMainServer();
        savedServerBoolean = isServer.equals("1");

        String socketPort = CallNumCache.getSocketPortNum();

        int isSingleDevsave = Integer.parseInt(CallNumCache.getIsSingleDevice());
        mainIpMessage = CallNumCache.getMainIpadress();

        if (!TextUtils.isEmpty(mainIpMessage)) {
            getIpAddressWinName();
            et_ipname.setText(winName);
        }

        et_socket_port.setText(socketPort);
        if (isSingleDevsave == 1) {
            radiogroup_server_type.check(R.id.single_device);
        } else {
            setting_fuwu_type.setVisibility(View.VISIBLE);
            radiogroup_server_type.check(R.id.multi_device);
            if (savedServerBoolean) {
                radiogroup_fuwu_type.check(R.id.main_userid);
            } else {
                radiogroup_fuwu_type.check(R.id.device_id);
                setting_ip_adress.setVisibility(View.VISIBLE);
                mainserver_ip_input.setText(mainIpAddress);
            }
        }

        Button button_ip_setting_ok = (Button) view.findViewById(R.id.button_ip_setting_ok);
        Button button_out = (Button) view.findViewById(R.id.button_out);

        radiogroup_server_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.single_device:
                        setting_fuwu_type.setVisibility(View.GONE);
                        break;
                    case R.id.multi_device:
                        setting_fuwu_type.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        radiogroup_fuwu_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_userid:
                        setting_ip_adress.setVisibility(View.GONE);
                        break;
                    case R.id.device_id:
                        setting_ip_adress.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        button_ip_setting_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (radiogroup_server_type.getCheckedRadioButtonId() == R.id.single_device) {
                    isSingleDev = 1;
                } else {
                    isSingleDev = 0;
                }

                if (radiogroup_fuwu_type.getCheckedRadioButtonId() == R.id.main_userid) {
                    isMianServer = 1;
                } else {
                    isMianServer = 0;
                }

                if (socketControl != null) {
                    socketControl.disconnectSocket();
                    socketControl = null;
                }

                if (isMianServer == 1) {
                    mainIpAddress = localIpAdress;
                } else {
                    String mainServerIp = mainserver_ip_input.getText().toString();
                    mainIpAddress = mainServerIp;
                }
                winName = et_ipname.getText().toString();
                CallNumCache.putDevicetypeConstant(String.valueOf(isMianServer));//是否是服务器
                CallNumCache.putIsSingleDevice(String.valueOf(isSingleDev));
                CallNumCache.putMainIpadress(mainIpAddress + "," + winName);
                CallNumCache.putSocketPortNum(et_socket_port.getText().toString());
                builder.dismiss();
                CommonUtils.reLoadApp();
            }
        });

        button_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });
        builder = new AlertDialog.Builder(BaseActivity.getActivity()).setTitle("叫号设置").setView(view).setCancelable(false)
                .create();
        builder.show();
    }

    private HashMap<String, String> textToSpeechMap = new HashMap<>();
    private List<String> ontimeList = new ArrayList<>();
    private boolean isInited = false;

    public void initTextToSpeech() {
        if (!isInited) {
            textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            textToSpeechMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_SYSTEM));
            TextToSpeechAll.getInstance().initTextToSpeech(APP.getContext());
            TextToSpeechAll.getInstance().textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech
                    .OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    if (utteranceId.equals("UniqueID")) {
                        if (ontimeList.size() > 0) {
                            ontimeList.remove(0);
                            if (ontimeList.size() > 0) {
                                HandleMessageUtils.getInstance().sendHandler(SENDTOSHOW, mhandler, ontimeList.get(0));
                            } else {
                                //播放完成后恢复后台音乐或者视频（如果有的话）
                                SoundControl.restartCurrentVolume();
                            }
                        }
                    }
                    isInited = true;
                }
            });

        }
    }

    private void initService() {
        if (conn == null) {
            //绑定叫号服务
            Intent intent = new Intent(BaseActivity.getActivity(), CallNumBerShow.class);
            conn = new ConService();
            isBind = BaseActivity.getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
            //服务端口接收到信息之后处理
            ClientSide.getInstance().setOnreceivedMsg(new ClientSide.OnreceivedMsg() {
                @Override
                public void onrecedMsg(String msg) {
                    handReceivedMeg(msg);
                }
            });
            //发送失败自己播放
            ClientSide.getInstance().setConFaile(new ClientSide.ConFaile() {
                @Override
                public void received(String msgs) {
                    handReceivedMeg(msgs);
                }
            });
        }
    }

    private LayoutPosition layoutPosition;

    /*如果后台推送有*/
    public void setLayoutPosition(LayoutPosition layoutPosition) {
        this.layoutPosition = layoutPosition;
        if (callTextService != null) {
            callTextService.setShowLayoutParameter(layoutPosition);
        }
    }

    private String callHeadSingle = "请";//叫号提示语，后续用于组装叫号句柄
    private String callEndSingle = "";

    /*设置叫号信息 例如 */
    public void setCallStyle(String callMessage) {
        Log.d(TAG, "setCallStyle: " + callMessage);
        callHeadSingle = callMessage.substring(0, callMessage.indexOf("x"));
        callEndSingle = callMessage.substring(callMessage.lastIndexOf("x") + 1, callMessage.length());
    }

    private void handReceivedMeg(String msg) {
        if (msg != null && !TextUtils.isEmpty(msg)) {
            String[] ipWinName = msg.split(",");
            String message = ipWinName[0];
            String sourseWinName = ipWinName[1];
            String onTimeString = message;
            String stringmsg = handVoice(message, sourseWinName);
            ontimeList.add(ontimeList.size(), stringmsg);
            onTimeString = shutDisplay(onTimeString, sourseWinName);
            playText(onTimeString);
        }
    }

    private static final int SENDTOSHOW = 0x211231;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENDTOSHOW:
                    String message = (String) msg.obj;
                    if (callTextService != null) {
                        callTextService.callMethodInService(message);
                    }
                    if (CallQueueView.onReceivedQueueAdd != null) {
                        if (message.contains(callHeadSingle)) {
                            message = message.replaceAll(callHeadSingle, "");
                        }
                        if (!TextUtils.isEmpty(callEndSingle) && message.contains(callEndSingle)) {
                            message = message.replaceAll(callEndSingle, "");
                        } else {
                            message = message.substring(0, message.indexOf("号") + 1);
                        }
                        CallQueueView.onReceivedQueueAdd.received(message);
                    }
                    break;
            }
        }
    };

    /**
     * 单机版的直接叫号
     */
    private void speakVoice() {
        //显示处理
        String message = handVoice(timeleCodeStr, winName);
        ontimeList.add(ontimeList.size(), message);
        //处理叫号
        String callMessage = shutDisplay(timeleCodeStr, winName);
        playText(callMessage);
    }

    /**
     * 处理叫号信息
     *
     * @param timeleCodeStr
     * @return
     */
    private String shutDisplay(String timeleCodeStr, String sourseWinName) {
        String callMessage = handBlank(timeleCodeStr);
        if (callMessage.contains(".")) {
            if (callMessage.endsWith(".")) {
                callMessage = callMessage.substring(0, callMessage.length() - 1);
            }
            callMessage = hand2StirngPoint(callMessage);
            Log.e("clientport", callMessage);
            callMessage = callMessage.replaceAll("\\.", ";");
            callMessage = getCallSentence(callMessage, "座位号", sourseWinName);

        } else if (callMessage.contains("-")) {
            if (callMessage.endsWith("-")) {
                callMessage = callMessage.substring(0, callMessage.length() - 1);
            }
            callMessage = hand2Stirng(callMessage);
            Log.e("clientport", callMessage);
            callMessage = callMessage.replaceAll("-", ";");
            callMessage = getCallSentence(callMessage, "号", sourseWinName);
        } else {
            callMessage = getCallSentence(callMessage, "号", sourseWinName);
        }
        return callMessage;
    }


    /**
     * 处理叫号句柄，根据后台推送或者前段设置局域网叫号信息设置叫号详情
     *
     * @param callMessage   例如 固定 请 客套语
     * @param addedCall     XXX号 之后的部分
     * @param sourseWinName 窗口名称
     * @return
     */
    private String getCallSentence(String callMessage, String addedCall, String sourseWinName) {
        if (TextUtils.isEmpty(callEndSingle)) {
            return callHeadSingle + callMessage + addedCall + "顾客来" + sourseWinName + "取餐";
        }
        if (callEndSingle.contains("号")) {
            callEndSingle = callEndSingle.replaceAll("号", "");
        }
        return callHeadSingle + callMessage + addedCall + callEndSingle;
    }


    /**
     * 处理带2的字符串
     *
     * @param stringToCall
     */
    private String hand2StirngPoint(String stringToCall) {
        String result = "";
        String[] stringArr = stringToCall.split("\\.");
        for (int i = 0; i < stringArr.length; i++) {
            if (stringArr[i].startsWith("2")) {
                stringArr[i] = ";" + stringArr[i];
            }
            stringArr[i] = stringArr[i] + ".";
            result += stringArr[i];
        }
        return result;
    }

    private String hand2Stirng(String stringToCall) {
        String result = "";
        String[] stringArr = stringToCall.split("-");
        for (int i = 0; i < stringArr.length; i++) {
            if (stringArr[i].startsWith("2")) {
                stringArr[i] = ";" + stringArr[i];
            }
            stringArr[i] = stringArr[i] + "-";
            result += stringArr[i];
        }
        if (result.endsWith("-")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private void toSpeak() {
        if (!TextUtils.isEmpty(timeleCodeStr)) {
            HandleMessageUtils.getInstance().runInThread(new Runnable() {
                @Override
                public void run() {
                    socketControl.sendData(timeleCodeStr + "," + winName);
                }
            });
        }
    }

    private void playText(String message) {
        /**
         * 如果有背景音乐的处理
         */
        SoundControl.stopCurrentVolume();
        TextToSpeechAll.getInstance().textToSpeech.speak(message, TextToSpeech.QUEUE_ADD, textToSpeechMap);
        if (ontimeList.size() == 1) {
            HandleMessageUtils.getInstance().sendHandler(SENDTOSHOW, mhandler, ontimeList.get(0));
        }
    }

    /**
     * 处理空格
     *
     * @param string
     * @return
     */
    private String handBlank(String string) {
        String voices;
        int starLocation = string.indexOf("-");
        int pointLocation = string.indexOf(".");
        if (starLocation != -1 || pointLocation != -1) {
            voices = handBlacks(string);
        } else {
            if (string.length() < 7) {//每个数字带有一个空格，3位数长度为6
                voices = string.replaceAll(" ", "");
            } else {
                voices = string;
            }
        }
        return voices;
    }

    /**
     * 处理多个字符串之间的空格的操作
     *
     * @param msg
     * @return
     */
    private String handBlacks(String msg) {
        String result = "";
        String[] message;
        if (msg.indexOf("-") != -1) {
            message = msg.split("-");
            for (int i = 0; i < message.length; i++) {
                if (message[i].length() < 7) {
                    message[i] = message[i].replaceAll(" ", "");
                }
                result += message[i] + "-";
            }
            result = result.substring(0, result.length() - 1);
        } else if (msg.indexOf(".") != -1) {
            message = msg.split("\\.");
            for (int i = 0; i < message.length; i++) {
                if (message[i].length() < 7) {
                    message[i] = message[i].replaceAll(" ", "");
                }
                result += message[i] + ".";
            }
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String handVoice(String voiceString, String sourseWinName) {
        String voices = handBlank(voiceString);
        if (voices.contains(".")) {
            if (voices.endsWith(".")) {
                voices = voices.substring(0, voices.length() - 1);
            }
            voices = voices.replaceAll("\\.", ";");
            voices = getCallSentence(voices, "座位号", sourseWinName);
        } else if (voices.contains("-")) {
            if (voices.endsWith("-")) {
                voices = voices.substring(0, voices.length() - 1);
            }
            voices = voices.replaceAll("-", ";");
            voices = getCallSentence(voices, "号", sourseWinName);
        } else {
            voices = getCallSentence(voices, "号", sourseWinName);
        }
        return voices;
    }

    public void releaseSourse() {
//      unbind();
        TextToSpeechAll.getInstance().destoryTextToSpeech();
        if (socketControl != null) {
            socketControl.disconnectSocket();
        }
    }

    private boolean isBind = false;

//解除服务
//    public void unbind() {
//        if (conn!=null && isBind) {
//            BaseActivity.getActivity().unbindService(conn);
//        }
//    }

    String lastFlag = null;

    public void initCaptionSpeech(final String str, final Integer playCount, final String flag) {
        initTextToSpeech();
        lastFlag = flag;
        playSound(str, playCount, flag);
    }

    public void playSound(final String str, final Integer playCount, final String flag) {
        if (flag.equals(lastFlag) == false) {
            return;
        }
        try {
            Thread.sleep(4 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TextToSpeechAll.getInstance().textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, textToSpeechMap);
        Log.i("textToSpeech", "playSound: ");
        TextToSpeechAll.getInstance().textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                int temC = playCount - 1;
                if (temC > 0 || playCount == -1) {
                    if (playCount == -1) {
                        CallNum.callNumInstance().playSound(str, playCount, flag);
                        Log.i("textToSpeech", "playSound: " + playCount);
                    } else {
                        CallNum.callNumInstance().playSound(str, temC - 1, flag);
                        Log.i("textToSpeech", "playSound: " + (temC - 1));
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
                Log.i("textToSpeech", "playSound: onError" + utteranceId);
            }
        });
    }
}

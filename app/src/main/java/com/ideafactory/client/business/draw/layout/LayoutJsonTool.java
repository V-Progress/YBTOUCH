package com.ideafactory.client.business.draw.layout;

import android.graphics.Point;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.ideafactory.client.business.draw.CreateElement;
import com.ideafactory.client.business.draw.layout.bean.AdsData;
import com.ideafactory.client.business.draw.layout.bean.AdsDetail;
import com.ideafactory.client.business.draw.layout.bean.AdsInfo;
import com.ideafactory.client.business.draw.layout.bean.CaramDetail;
import com.ideafactory.client.business.draw.layout.bean.Container;
import com.ideafactory.client.business.draw.layout.bean.ControlsDetail;
import com.ideafactory.client.business.draw.layout.bean.ImageDetail;
import com.ideafactory.client.business.draw.layout.bean.LayoutFoot;
import com.ideafactory.client.business.draw.layout.bean.LayoutInfo;
import com.ideafactory.client.business.draw.layout.bean.LayoutMenu;
import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;
import com.ideafactory.client.business.draw.layout.bean.MusicDetail;
import com.ideafactory.client.business.draw.layout.bean.TextDetail;
import com.ideafactory.client.business.draw.layout.bean.VideoDetail;
import com.ideafactory.client.business.draw.layout.bean.WebDetail;
import com.ideafactory.client.business.draw.layout.bean.WeiDetail;
import com.ideafactory.client.business.localnetcall.CallQueueOrderDetail;
import com.ideafactory.client.util.TYTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class LayoutJsonTool {

    /**
     * 背景音乐消息解析
     *
     * @param layoutMenuJson
     * @return
     */
    static String getLayoutBackMuisc(String layoutMenuJson, int position) {
        if (layoutMenuJson.equals("null") || layoutMenuJson.equals("") || layoutMenuJson.equals("faile")) {
            return "";
        }
        String bgMusic = "";
        String layoutListPlayTime = "";
        JSONTokener jsonParser = new JSONTokener(layoutMenuJson);
        try {
            JSONObject urlJson = (JSONObject) jsonParser.nextValue();
            JSONArray jsonArray;
            try {
                JSONObject layoutJson = new JSONObject(layoutMenuJson);
                layoutListPlayTime = layoutJson.getString("playTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(layoutListPlayTime)) {//单个布局 背景音乐
                jsonArray = (JSONArray) TYTool.getJsonObj(urlJson, "move", null);
            } else {//混合布局 背景音乐
                JSONArray layoutList = urlJson.getJSONArray("layoutList");
                JSONObject jsonObject1 = layoutList.getJSONObject(position);
                jsonArray = (JSONArray) TYTool.getJsonObj(jsonObject1, "move", null);
            }

            if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject moveObj = (JSONObject) jsonArray.get(i);
                    JSONObject musicJsonObj = (JSONObject) TYTool.getJsonObj(moveObj, "bgmusic", null);
                    if (musicJsonObj != null) {
                        String music = (String) TYTool.getJsonObj(musicJsonObj, "music", null);
                        if (music != null) {
                            bgMusic = music;
                        }
                        break;
                    }
                }
            }

            if (!bgMusic.equals("null") && !bgMusic.equals("") && bgMusic.startsWith("http:")) {
                bgMusic = bgMusic.substring(bgMusic.lastIndexOf("/") + 1, bgMusic.length());
            } else if (bgMusic.equals("播放本地背景音乐")) {
                bgMusic = "播放本地背景音乐";
            } else {
                bgMusic = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bgMusic;
    }

    /**
     * 获取layoutMenu
     *
     * @param layoutMenuJson
     * @return
     */
    static LayoutMenu getLayoutMenu(String layoutMenuJson) {
        LayoutMenu layoutMenu = new LayoutMenu();
        if (layoutMenuJson.equals("") || layoutMenuJson.equals("faile")) {
            layoutMenu.setShow(false);
        } else {
            JSONTokener jsonParser = new JSONTokener(layoutMenuJson);
            try {
                JSONObject urlJson = (JSONObject) jsonParser.nextValue();
                JSONObject layoutJson = urlJson.getJSONObject("header");
                boolean enabled = layoutJson.getBoolean("enabled");
                if (enabled) {
                    String logoimg = layoutJson.getString("logoimg");
                    String weatherShow = layoutJson.getString("weatherShow");
                    String address = layoutJson.getString("address");
                    String timeShow = layoutJson.getString("timeShow");
                    String timeFormat = layoutJson.getString("timeFormat");
                    String fontFamily = layoutJson.getString("fontFamily");
                    String fontSize = layoutJson.getString("fontSize");
                    String fontColor = layoutJson.getString("fontColor");
                    String backGround = layoutJson.getString("background");

                    boolean timeShowBoolean = false;
                    if (timeShow.equals("1")) {
                        timeShowBoolean = true;
                    }
                    boolean weatherShowBoolean = false;
                    if (weatherShow.equals("1")) {
                        weatherShowBoolean = true;
                    }

                    layoutMenu.setShow(true);
                    layoutMenu.setTimeShow(timeShowBoolean);
                    layoutMenu.setWeatherShow(weatherShowBoolean);
                    layoutMenu.setBackGround(backGround);
                    layoutMenu.setAddress(address);
                    layoutMenu.setFontColor(fontColor);
                    layoutMenu.setFontFamily(fontFamily);
                    fontSize = fontSize.replaceAll("px", "");
                    layoutMenu.setFontSize(Float.valueOf(fontSize));
                    layoutMenu.setTimeFormat(timeFormat.replace("hh", "kk").replace("HH", "kk"));

                    if (logoimg.startsWith("http:")) {
                        logoimg = logoimg.substring(logoimg.lastIndexOf("/") + 1, logoimg.length());
                    }
                    layoutMenu.setLogoImage(logoimg);
                } else {
                    layoutMenu.setShow(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return layoutMenu;
    }

    /**
     * 获取getLayoutFoot
     *
     * @param layoutMenuJson
     * @return
     */
    static LayoutFoot getLayoutFoot(String layoutMenuJson) {
        LayoutFoot layoutFoot = new LayoutFoot();
        if (layoutMenuJson.equals("") || layoutMenuJson.equals("faile")) {
            layoutFoot.setEnabled(false);
        } else {
            JSONTokener jsonParser = new JSONTokener(layoutMenuJson);
            try {
                JSONObject urlJson = (JSONObject) jsonParser.nextValue();
                JSONObject layoutJson = urlJson.getJSONObject("footer");
                boolean enabled = layoutJson.getBoolean("enabled");
                if (enabled) {
                    String footerText = layoutJson.getString("footerText");
                    String playTime = layoutJson.getString("playTime");
                    String isPlay = layoutJson.getString("isPlay");
                    String fontFamily = layoutJson.getString("fontFamily");
                    String fontSize = layoutJson.getString("fontSize");
                    String fontColor = layoutJson.getString("fontColor");
                    String background = layoutJson.getString("background");

                    layoutFoot.setEnabled(true);
                    layoutFoot.setFooterText(footerText);
                    try {
                        float playTimef = Float.parseFloat(playTime);
                        layoutFoot.setPlayTime(playTimef);
                    } catch (Exception e) {
                        layoutFoot.setPlayTime(1f);
                    }

                    layoutFoot.setIsPlay(isPlay);
                    layoutFoot.setBackground(background);
                    layoutFoot.setFontColor(fontColor);
                    layoutFoot.setFontFamily(fontFamily);

                    fontSize = fontSize.replaceAll("px", "");
                    layoutFoot.setFontSize(Float.parseFloat(fontSize));
                } else {
                    layoutFoot.setEnabled(false);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return layoutFoot;
    }

    /**
     * 获取布局
     */
    public static List<LayoutInfo> getLayoutInfo(String json) {

        List list = new ArrayList();
        try {

            JSONTokener jsonParser = new JSONTokener(json);

            JSONObject objJson = (JSONObject) jsonParser.nextValue();
            JSONArray person = objJson.getJSONArray("center");
            for (int i = 0; i < person.length(); i++) {

                LayoutInfo layoutInfo = new LayoutInfo();

                JSONObject jsonObject = (JSONObject) person.get(i);
                String id = (String) TYTool.getJsonObj(jsonObject, "id", "");
                Integer type = Integer.parseInt((String) TYTool.getJsonObj(jsonObject, "type", null));

                layoutInfo.setId(id);
                layoutInfo.setType(type);

                Container containerObj = getContainer(jsonObject);
                layoutInfo.setContainer(containerObj);

                //倒计时
                ControlsDetail controlsDetailObj = getControlsDetail(jsonObject);
                layoutInfo.setControlsDetail(controlsDetailObj);


                if (type == 2) {// 文本消息处理
                    TextDetail textDetail = getTextDetail(jsonObject);
                    layoutInfo.setTextDetail(textDetail);
                } else if (type == 1 || type == 6 || type == 8) {// 图片消息处理
                    ImageDetail imageDetail = getImageDetail(jsonObject);
                    layoutInfo.setImageDetail(imageDetail);
                } else if (type == 3) {// 视频消息处理
                    VideoDetail imageDetail = getVideoDetail(jsonObject);
                    layoutInfo.setVideoDetail(imageDetail);
                } else if (type == 4) {// 微信内容解析
                    WeiDetail weiDetail = getWeiDetail(jsonObject);
                    layoutInfo.setWeiDetail(weiDetail);
                } else if (type == 5) {// 网页内容解析
                    WebDetail webDetail = getWebDetail(jsonObject);
                    layoutInfo.setWebDetail(webDetail);
                } else if (type == 13) {//排队叫号解析
                    CallQueueOrderDetail callQueueOrderDetail = getCallQueueDetial(jsonObject);
                    layoutInfo.setCallQueueOrderDetail(callQueueOrderDetail);
                } else if (type == 14) {//windowType
                    try {
                        int windowType = Integer.parseInt((String) TYTool.getJsonObj(jsonObject, "windowType", "1"));
                        layoutInfo.setWindowType(windowType);
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                } else if (type == 12) {//摄像头 或者 HDMI
                    CaramDetail caramDetail = getCaramDetail(jsonObject);
                    layoutInfo.setCaramDetail(caramDetail);
                } else if (type == 17) {
                    AdsDetail adsDetail = getAdsDetail(jsonObject);
                    layoutInfo.setAdsDetail(adsDetail);

                    ImageDetail imageDetail = new ImageDetail();
                    imageDetail.setImagePlayType("0");
                    imageDetail.setIsAutoPlay("true");
                    imageDetail.setPlayTime("5");
                    layoutInfo.setImageDetail(imageDetail);
                }else if (type==18){
                    JSONObject adsInfoObj = (JSONObject) TYTool.getJsonObj(jsonObject, "adsInfo", null);
                    AdsInfo adsInfo=null;
                    if (adsInfoObj!=null){
                        adsInfo = new Gson().fromJson(adsInfoObj.toString(), AdsInfo.class);
                        List<AdsData> adsData = adsInfo.getAdsData();
                        //url路径处理
                        if (adsData != null) {
                            List<AdsData> adsTemp=new ArrayList<>();
                            for (int j = 0; j < adsData.size(); j++) {
                                AdsData data = adsData.get(j);
                                String con = data.getUrl();
                                if (con.startsWith("http:") && type != 5) {
                                    con = con.substring(con.lastIndexOf("/") + 1, con.length());
                                }
                                data.setUrl(con);
                                adsTemp.add(data);
                            }
                            adsInfo.setAdsData(adsTemp);
                        }
                    }
                    layoutInfo.setAdsInfo(adsInfo);
                }

                // 显示内容处理
                // JSONArray content = jsonObject.getJSONArray("content");
                JSONArray content = (JSONArray) TYTool.getJsonObj(jsonObject, "content", null);
                if (content != null) {
                    String[] contentArray = new String[content.length()];
                    for (int j = 0; j < content.length(); j++) {
                        String con = (String) content.get(j);
                        if (con.startsWith("http:") && type != 5) {
                            con = con.substring(con.lastIndexOf("/") + 1, con.length());
                        }
                        contentArray[j] = con;
                    }
                    layoutInfo.setContent(contentArray);
                }
                list.add(layoutInfo);
            }
            parseMovePanel(objJson, list);
        } catch (Exception e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }


    private static CallQueueOrderDetail getCallQueueDetial(JSONObject jsonObject) {
        JSONObject callQueue = (JSONObject) TYTool.getJsonObj(jsonObject, "orderDetail", null);
        if (callQueue != null) {
            String linkman = (String) TYTool.getJsonObj(callQueue, "linkman", "");
            String orderStoreName = (String) TYTool.getJsonObj(callQueue, "orderStoreName", "");
            String phoneNum = (String) TYTool.getJsonObj(callQueue, "phoneNum", "");
            String prompt = (String) TYTool.getJsonObj(callQueue, "prompt", "");
            CallQueueOrderDetail callQueueOrderDetial = new CallQueueOrderDetail();
            callQueueOrderDetial.setLinkman(linkman);
            callQueueOrderDetial.setOrderStoreName(orderStoreName);
            callQueueOrderDetial.setPhoneNum(phoneNum);
            callQueueOrderDetial.setPrompt(prompt);
            return callQueueOrderDetial;
        } else {
            return null;
        }
    }

    /**
     * 移动窗口处理
     */
    private static void parseMovePanel(JSONObject objJson, List list) throws JSONException {
        JSONArray move = (JSONArray) TYTool.getJsonObj(objJson, "move", null);
        if (move != null) {
            for (int i = 0; i < move.length(); i++) {
                LayoutInfo layoutInfo = new LayoutInfo();

                JSONObject jsonObject = (JSONObject) move.get(i);
                String type = (String) TYTool.getJsonObj(jsonObject, "type", null);

                Container containerObj = getContainer(jsonObject);
                layoutInfo.setContainer(containerObj);

                if (type.contains("_1")) {// 图片消息处理
                    layoutInfo.setType(1);
                    ImageDetail imageDetail = getImageDetail(jsonObject);
                    layoutInfo.setImageDetail(imageDetail);
                } else if (type.contains("_3")) {// 音乐
                    layoutInfo.setType(-3);
                    MusicDetail musicDetail = getMusicDetail(jsonObject);
                    layoutInfo.setMusicDetail(musicDetail);
                } else if (type.contains("_2")) {//文本
                    layoutInfo.setType(2);
                    TextDetail textDetail = getTextDetail(jsonObject);
                    layoutInfo.setTextDetail(textDetail);
                }

                // 显示内容处理
                JSONArray content = (JSONArray) TYTool.getJsonObj(jsonObject, "content", null);
                if (content != null) {
                    String[] contentArray = new String[content.length()];
                    for (int j = 0; j < content.length(); j++) {
                        String con = (String) content.get(j);
                        if (con.startsWith("http:")) {
                            con = con.substring(con.lastIndexOf("/") + 1, con.length());
                        }
                        contentArray[j] = con;
                    }
                    layoutInfo.setContent(contentArray);
                }
                list.add(layoutInfo);
            }
        }
    }

    /**
     * 获取布局的位置
     */
    public static LayoutPosition getViewPostion(LayoutInfo layoutInfo, WindowManager wm) {
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = point.x;
        int height = point.y - CreateElement.TOOL_HEIGHT - CreateElement.FOOT_HEIGHT;

        LayoutPosition layoutPostion = new LayoutPosition();
        String widths = layoutInfo.getContainer().getWidth();
        String heights = layoutInfo.getContainer().getHeight();
        String lefts = layoutInfo.getContainer().getLeft();
        String tops = layoutInfo.getContainer().getTop();

        float layoutHeight = Float.valueOf(heights.substring(0, heights.indexOf("%"))) / 100;
        float layoutWidth = Float.valueOf(widths.substring(0, widths.indexOf("%"))) / 100;
        float layoutLeft = Float.valueOf(lefts.substring(0, lefts.indexOf("%"))) / 100;
        float layoutTop = Float.valueOf(tops.substring(0, tops.indexOf("%"))) / 100;

        Float layHeight = (layoutHeight * height);
        Float layWidth = (layoutWidth * width);
        Float layLeft = (layoutLeft * width);
        Float layTop = (layoutTop * height);

        layoutPostion.setHeight(TYTool.getFloatToInt(layHeight, "#"));
        layoutPostion.setWidth(TYTool.getFloatToInt(layWidth, "#"));
        layoutPostion.setLeft(TYTool.getFloatToInt(layLeft, "#"));
        layoutPostion.setTop(TYTool.getFloatToInt(layTop, "#") + CreateElement.TOOL_HEIGHT);

        return layoutPostion;
    }


    /**
     * 获取倒计时信息
     */
    public static ControlsDetail getCountDown(LayoutInfo layoutInfo) {
        ControlsDetail controlsDetail = new ControlsDetail();
        String endDate = layoutInfo.getControlsDetail().getEndDate();
        String slogan = layoutInfo.getControlsDetail().getSlogan();
        String title = layoutInfo.getControlsDetail().getTitle();
        String startDate = layoutInfo.getControlsDetail().getStartDate();
        String textColor = layoutInfo.getControlsDetail().getTextColor();
        String bgColor = layoutInfo.getControlsDetail().getBgColor();
        String timeColor = layoutInfo.getControlsDetail().getTimeColor();

        controlsDetail.setEndDate(endDate);
        controlsDetail.setSlogan(slogan);
        controlsDetail.setTitle(title);
        controlsDetail.setStartDate(startDate);
        controlsDetail.setTextColor(textColor);
        controlsDetail.setBgColor(bgColor);
        controlsDetail.setTimeColor(timeColor);

        return controlsDetail;
    }

    private static Container getContainer(JSONObject container) {
        JSONObject containerJson = (JSONObject) TYTool.getJsonObj(container, "container", null);
        if (containerJson != null) {
            String width = (String) TYTool.getJsonObj(containerJson, "width", "");
            String height = (String) TYTool.getJsonObj(containerJson, "height", "");
            String top = (String) TYTool.getJsonObj(containerJson, "top", "");
            String left = (String) TYTool.getJsonObj(containerJson, "left", "");

            Container containerObj = new Container();
            containerObj.setWidth(width);
            containerObj.setHeight(height);
            containerObj.setTop(top);
            containerObj.setLeft(left);

            return containerObj;
        } else {
            return null;
        }
    }

    private static ControlsDetail getControlsDetail(JSONObject controlsDetail) {
        JSONObject controlsDetailJson = (JSONObject) TYTool.getJsonObj(controlsDetail, "controlsDetail", null);
        if (controlsDetailJson != null) {
            String endDate = (String) TYTool.getJsonObj(controlsDetailJson, "endDate", "");
            String slogan = (String) TYTool.getJsonObj(controlsDetailJson, "slogan", "");
            String title = (String) TYTool.getJsonObj(controlsDetailJson, "title", "");
            String startDate = (String) TYTool.getJsonObj(controlsDetailJson, "startDate", "");
            String textColor = (String) TYTool.getJsonObj(controlsDetailJson, "textColor", "");
            String bgColor = (String) TYTool.getJsonObj(controlsDetailJson, "bgColor", "");
            String timeColor = (String) TYTool.getJsonObj(controlsDetailJson, "timeColor", "");

            ControlsDetail controlsDetailObj = new ControlsDetail();
            controlsDetailObj.setEndDate(endDate);
            controlsDetailObj.setSlogan(slogan);
            controlsDetailObj.setTitle(title);
            controlsDetailObj.setStartDate(startDate);
            controlsDetailObj.setTextColor(textColor);
            controlsDetailObj.setBgColor(bgColor);
            controlsDetailObj.setTimeColor(timeColor);

            return controlsDetailObj;
        } else {
            return null;
        }
    }

    private static TextDetail getTextDetail(JSONObject jsonObject) {
        JSONObject textDetail = (JSONObject) TYTool.getJsonObj(jsonObject, "textDetail", null);
        if (textDetail != null) {
            Boolean isPlay = Boolean.parseBoolean((String) TYTool.getJsonObj(textDetail, "isPlay", "false"));
            String playTime = (String) TYTool.getJsonObj(textDetail, "playTime", "5");
            String fontSize = (String) TYTool.getJsonObj(textDetail, "fontSize", "");
            String fontColor = (String) TYTool.getJsonObj(textDetail, "fontColor", "");
            String fontFamily = (String) TYTool.getJsonObj(textDetail, "fontFamily", "");
            String background = (String) TYTool.getJsonObj(textDetail, "background", "");
            String playType = (String) TYTool.getJsonObj(textDetail, "playType", "0"); //滚动方式
            Integer dataType = (Integer) TYTool.getJsonObj(textDetail, "dataType", 0); //汉王 空气监控
            String textAlign = (String) TYTool.getJsonObj(textDetail, "textAlign", ""); //对齐方式 靠左  居中  靠右

            TextDetail textDetailObj = new TextDetail();
            textDetailObj.setIsPlay(isPlay);
            textDetailObj.setPlayTime(Float.parseFloat(playTime));
            fontSize = fontSize.replaceAll("px", "");
            textDetailObj.setFontSize(Integer.parseInt(fontSize));
            textDetailObj.setFontColor(fontColor);
            textDetailObj.setFontFamily(fontFamily);
            textDetailObj.setBackground(background);
            textDetailObj.setPlayType(playType);
            textDetailObj.setDataType(dataType);
            textDetailObj.setTextAlign(textAlign);

            return textDetailObj;
        } else {
            return null;
        }
    }

    private static ImageDetail getImageDetail(JSONObject jsonObject) {
        JSONObject imageDetail = (JSONObject) TYTool.getJsonObj(jsonObject, "imageDetail", null);
        if (imageDetail != null) {
            String isPlay = (String) TYTool.getJsonObj(imageDetail, "isAutoPlay", "");
            String playTime = (String) TYTool.getJsonObj(imageDetail, "playTime", "");
            String imagePlayType = (String) TYTool.getJsonObj(imageDetail, "imagePlayType", "0");
            ImageDetail imageDetailObj = new ImageDetail();
            imageDetailObj.setPlayTime(playTime);
            imageDetailObj.setIsAutoPlay(isPlay);
            imageDetailObj.setImagePlayType(imagePlayType);
            return imageDetailObj;
        } else {
            return null;
        }
    }

    private static VideoDetail getVideoDetail(JSONObject videoJson) {
        JSONObject videoDetail = (JSONObject) TYTool.getJsonObj(videoJson, "videoDetail", null);
        if (videoDetail != null) {
            boolean loop = (Boolean) TYTool.getJsonObj(videoDetail, "loop", true);
            VideoDetail videoDetailObj = new VideoDetail();
            videoDetailObj.setLoop(loop);
            return videoDetailObj;
        } else {
            return null;
        }
    }

    private static WeiDetail getWeiDetail(JSONObject jsonObject) {
        String ticketid = (String) TYTool.getJsonObj(jsonObject, "ticketid", "");
        JSONObject weiDetail = (JSONObject) TYTool.getJsonObj(jsonObject, "textDetail", "");
        WeiDetail weiDetailObj = new WeiDetail();
        if (weiDetail != null) {
            int fontSize;
            try {
                fontSize = (Integer) TYTool.getJsonObj(weiDetail, "fontSize", 38);
            } catch (Exception e) {
                fontSize = 38;
            }

            String fontColor = (String) TYTool.getJsonObj(weiDetail, "fontColor", "");
            String fontFamily = (String) TYTool.getJsonObj(weiDetail, "fontFamily", "");
            String background = (String) TYTool.getJsonObj(weiDetail, "background", "");

            weiDetailObj.setFontSize(fontSize);
            weiDetailObj.setFontColor(fontColor);
            weiDetailObj.setFontFamily(fontFamily);
            weiDetailObj.setBackground(background);
            weiDetailObj.setTicket(ticketid);
        }

        JSONObject weiMsgDetail = (JSONObject) TYTool.getJsonObj(jsonObject, "weimsg", null);
        if (weiMsgDetail != null) {
            // "msgShowType":"2","msgSize":"10","msgSource":"1","isMeet":"1"
            String msgShowType = (String) TYTool.getJsonObj(weiMsgDetail, "msgShowType", "2");
            String msgSize = (String) TYTool.getJsonObj(weiMsgDetail, "msgSize", "25");
            String msgSource = (String) TYTool.getJsonObj(weiMsgDetail, "msgSource", "1");
            String meet = (String) TYTool.getJsonObj(weiMsgDetail, "isMeet", "0");

            if (meet.equals("1")) {
                weiDetailObj.setIsMeet(true);
            } else {
                weiDetailObj.setIsMeet(false);
            }
            weiDetailObj.setMsgShowType(msgShowType);
            weiDetailObj.setMsgSize(msgSize);
            weiDetailObj.setMsgSource(msgSource);
        } else {
            weiDetailObj.setIsMeet(false);
            weiDetailObj.setMsgSize("25");
        }
        return weiDetailObj;
    }

    private static WebDetail getWebDetail(JSONObject jsonObject) {
        JSONObject webmsg = (JSONObject) TYTool.getJsonObj(jsonObject, "webmsg", null);
        if (webmsg != null) {
            String autoFlus = (String) TYTool.getJsonObj(webmsg, "autoflus", "");
            String flusTime = (String) TYTool.getJsonObj(webmsg, "flustime", "");
            String webType = (String) TYTool.getJsonObj(webmsg, "webType", "");

            WebDetail webDetail = new WebDetail();
            if ("0".equals(autoFlus)) {
                webDetail.setAutoFlus(true);
            } else {
                webDetail.setAutoFlus(false);
            }
            webDetail.setFlusTime(flusTime);
            webDetail.setWebType(webType);

            return webDetail;
        } else {
            return null;
        }
    }

    private static MusicDetail getMusicDetail(JSONObject jsonObject) {
        JSONObject bgmusic = (JSONObject) TYTool.getJsonObj(jsonObject, "bgmusic", null);
        if (bgmusic != null) {
            String music = (String) TYTool.getJsonObj(bgmusic, "music", "");
            String name = (String) TYTool.getJsonObj(bgmusic, "name", "");

            MusicDetail musicDetail = new MusicDetail();
            musicDetail.setMusic(music);
            musicDetail.setName(name);

            return musicDetail;
        } else {
            return null;
        }
    }

    private static CaramDetail getCaramDetail(JSONObject jsonObject) {
        JSONObject caram = (JSONObject) TYTool.getJsonObj(jsonObject, "caramDetail", null);
        if (caram != null) {
            String caramType = (String) TYTool.getJsonObj(caram, "caramType", "");

            CaramDetail caramDetail = new CaramDetail();
            caramDetail.setCaramType(caramType);

            return caramDetail;
        } else {
            return null;
        }
    }

    private static AdsDetail getAdsDetail(JSONObject jsonObject) {
        JSONObject caram = (JSONObject) TYTool.getJsonObj(jsonObject, "adsDetail", null);
        if (caram != null) {
            String appID = (String) TYTool.getJsonObj(caram, "appID", "");
            String adslotId = (String) TYTool.getJsonObj(caram, "adslotId", "");
            String showTime = (String) TYTool.getJsonObj(caram, "showTime", "");

            AdsDetail adsDetail = new AdsDetail();
            adsDetail.setAppID(appID);
            adsDetail.setAdslotId(adslotId);
            adsDetail.setShowTime(showTime);

            return adsDetail;
        } else {
            return null;
        }
    }

}

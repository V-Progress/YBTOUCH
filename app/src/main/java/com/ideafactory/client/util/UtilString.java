package com.ideafactory.client.util;
/**
 * @(#)UtilString.java 1.00
 * Apr 26, 2008 4:20:03 PM
 * Copyright (c) 2007-2008 __MyCorp 有限公司 版权所有
 * __Mycorp Company of China. All rights reserved.
 * <p>
 * This software is the confidential and proprietary
 * information of __Mycorp Company of China.
 * <p>
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement
 * you entered into with __Mycorp.
 */

import android.annotation.TargetApi;
import android.os.Build;

import java.util.*;

/**
 * 字符串处理.
 *
 * @since BASE 0.1
 */

public final class UtilString {
    public UtilString() {
    }

    /**
     * 数组转换为字符串.
     *
     * @param strSource     要分解的数组
     * @param strDelimiter  分隔符
     * @param bProcessEmpty 是否跳空值
     * @return String 转换后串
     * @since BASE 0.1
     */
    private static String arrayToString(String[] strSource, String strDelimiter, boolean bProcessEmpty) {
        //空值返回
        if (strSource.length == 0)
            return "";

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < strSource.length; i++) {
            //空值跳过
            if (bProcessEmpty) {
                if (strSource[i] == null || strSource[i].equals("")
                        || strSource[i].length() == 0)
                    continue;
            }
            buf.append(strSource[i]);
            if (i != strSource.length - 1) {
                buf.append(strDelimiter);
            }

        }
        return buf.toString();
    }

    /**
     * @param strSource
     * @param strDelimiter
     * @return
     * @since BASE 0.1
     */
    //
    public static String arrayToString(String[] strSource,
                                       String strDelimiter) {
        return arrayToString(strSource, strDelimiter, true);
    }

    /**
     * 字符串转为数组.
     *
     * @param strSource 源字符串
     * @param delimiter 分隔符
     * @return 字符串数组
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] stringToArray(String strSource,
                                          String delimiter) {
        if (strSource == null)
            return null;

        //去掉尾部分隔符
        if (Objects.equals(strSource.substring(strSource.length() - delimiter.length()), delimiter))
            strSource = strSource.substring(0, strSource.length()
                    - delimiter.length());

        StringTokenizer token = new StringTokenizer(strSource, delimiter);
        String[] array = new String[token.countTokens()];
        int i = 0;
        //取值
        while (token.hasMoreTokens()) {
            array[i] = token.nextToken();
            i++;
        }
        return array;
    }

    /**
     * 使用字符串替换.
     *
     * @param strData
     * @param strSource
     * @param strTarget
     * @return
     */
    public static String replaceAll(String strData, String strSource,
                                    String strTarget) {
        StringBuilder buf = new StringBuilder(strData);
        int pos = buf.indexOf(strSource);

        while (pos >= 0) {
            buf.replace(pos, pos + strSource.length(), strTarget);
            pos = buf.indexOf(strSource, pos + strTarget.length());
        }
        return buf.toString();
    }


    public static String[] stringToArray(String str) {
        //默认为，号分隔
        return stringToArray(str, ",");
    }

    /**
     * 判断字符串是否为“空白字字符”.
     *
     * @param strSource
     * @return
     */
    public static boolean isWhiteSpace(String strSource) {
        if (strSource.length() == 0)
            return false;

        char[] ch = strSource.toCharArray();
        for (char aCh : ch) {
            if (!Character.isWhitespace(aCh)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得空白字符串位置.
     *
     * @param source
     * @param start
     * @return
     * @since BASE 0.1
     */
    public static int indexOfWhiteSpace(String source, int start) {

        for (int i = start; i < source.length(); i++) {
            if (Character.isWhitespace(source.charAt(i)))
                return i;
        }
        return -1;

    }

    public static boolean isNull(String str) {
        return (str == null || str.equals(""));
    }

    public static boolean isNotNull(String str) {
        if (str == null || str.trim().equals("") || str.trim().equalsIgnoreCase("null")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 将空白字符串替换成分隔符.
     *
     * @param strSource
     * @return
     */
    public static final String replaceWhiteSpace(String strSource,
                                                 String strReplace) {
        String data = strSource.trim();
        boolean found = false;
        StringBuilder buf = new StringBuilder();

        if (data.length() == 0)
            return "";

        char[] ch = data.toCharArray();
        for (char aCh : ch) {
            if (Character.isWhitespace(aCh)) {
                //连续出现空白字符，则移除
                if (found) {
                    continue;
                } else {
                    buf.append(strReplace);
                    found = true;
                    continue;
                }
            } else {
                found = false;
            }
            buf.append(aCh);
        }
        return buf.toString();
    }

    /**
     * 二行数据制转换成十六进制字符串.
     *
     * @return String 十六进制字符串
     * @since BASE 0.1
     */
    public static String byteToHexString(byte[] digest) { //
        StringBuilder hexString = new StringBuilder();
        synchronized (hexString) {
            for (byte aDigest : digest) {
                final String plainText = Integer.toHexString(0xFF & aDigest);
                if (plainText.length() < 2) {
                    hexString.append("0");
                }
                hexString.append(plainText);
            }
        }
        return hexString.toString();
    }

    /**
     * 将字符串编码成UNICODE编码格式字符串.
     *
     * @param s 原字符串.
     * @return 编码后的字符串.
     */
    public static String String2Unicode(String s) {
        if (s == null || s.length() == 0)
            return "";
        char[] charA = s.toCharArray();
        StringBuffer t = new StringBuffer("");
        String tt = "";
        for (char aCharA : charA) {
            tt = Integer.toHexString((int) aCharA);
            if (tt.length() == 2)
                tt = "%" + tt;
            else
                tt = "%u" + tt;
            t.append(tt);
        }
        return t.toString();
    }

    /**
     * 首字大写.
     *
     * @param source String
     * @return String
     */
    public static String wordCap(String source) {
        if (source == null || source.length() == 0) {
            return "";
        }
        char firstChar = source.charAt(0);
        if (Character.isLetter(firstChar)) {
            String rc = Character.toUpperCase(firstChar) + source.substring(1);
            return rc;
        } else {
            return source;
        }
    }

    /**
     * 首字小写.
     *
     * @param source String
     * @return String
     */
    public static String wordLow(String source) {
        if (source == null || source.length() == 0) {
            return "";
        }
        char firstChar = source.charAt(0);
        if (!Character.isLetter(firstChar)) {
            String rc = Character.toLowerCase(firstChar) + source.substring(1);
            return rc;
        } else {
            return source;
        }

    }

    /**
     * 转义字符串中的特殊字符
     *
     * @param s
     * @return
     */
    public static String transString(String s) {
        s = UtilString.replaceAll(s, "\\", "\\\\");
        s = UtilString.replaceAll(s, "\"", "\\\"");
        return s;
    }

    public static String noNull(String s) {
        return noNull(s, "");
    }

    private static String noNull(String s, String tag) {
        if (s == null || s.equals("null"))
            return tag;
        return s;
    }

    public static String noEmpty(String s, String tag) {
        if (s == null || s.equals(""))
            return tag;
        return s;
    }

    /**
     * 是否为空字符串，是否为null
     *
     * @param s
     * @return
     */
    public static boolean isValidString(String s) {
        return !(s == null || s.equals(""));
    }

    public static String isValidString(String s, String s1) {
        if (s == null || s.equals("")) {
            return s1;
        } else {
            return s;
        }
    }

    /**
     * 把由间隔符分隔的字符串(key=value&key1=value1)转换成map
     *
     * @return
     */
    public static Map<String, String> string2map(String srcStr, String mark) {
        Map<String, String> map = new HashMap<String, String>();
        String tem[] = UtilString.stringToArray(srcStr, mark);
        if (tem == null) {
            return map;
        }
        for (String aTem : tem) {
            String s[] = aTem.split("=");
            //System.out.println(s[0]+":"+s[1]);
            if (s.length == 1) {
                map.put(s[0], "");
            } else if (s.length == 2) {
                map.put(s[0], s[1]);
            }
        }
        return map;
    }

    private static String map2string(Map<String, String> map, String mark) {
        Set<String> set = map.keySet();
        StringBuffer sb = new StringBuffer();
        for (String s : set) {
            sb.append(s).append("=").append(map.get(s)).append(mark);
        }
        String result = sb.toString();
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String unEncode(String str) {
        StringBuffer sb = new StringBuffer("");
        String[] vs = str.split("#", Integer.MAX_VALUE);
        for (int i = 1; i < vs.length; i++) {
            sb.append((char) Integer.parseInt(vs[i]));
        }
        return sb.toString();
    }

    /**
     * 格式化talkerIdList,使id排序
     *
     * @param talkerIdList
     * @return
     */
    public static String formatTalkerIdList(String talkerIdList) {
        talkerIdList = talkerIdList.trim();
        if (talkerIdList.startsWith(","))
            talkerIdList = talkerIdList.substring(1);
        if (talkerIdList.endsWith(","))
            talkerIdList = talkerIdList.substring(0, talkerIdList.length() - 1);
        String idList = "";
        String[] list = talkerIdList.split(",");
        Arrays.sort(list);
        for (int i = 0; i < list.length; i++) {
            if (i != 0)
                idList += ",";
            idList += list[i];
        }
        return idList;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "AA");
        map.put("b", "BB");
        System.out.println(UtilString.map2string(map, "&"));
    }
}

package com.ideafactory.client.business.localnetcall;

import android.text.TextUtils;

/**
 * 存储本地叫号端口
 */
public class ServerConstant {

    public static int PORT = 10000;//设置默认端口号10000

    static{
        String portNum = CallNumCache.getSocketPortNum();
        if(!TextUtils.isEmpty(portNum)){
            PORT = Integer.valueOf(portNum);
        }
    }


}

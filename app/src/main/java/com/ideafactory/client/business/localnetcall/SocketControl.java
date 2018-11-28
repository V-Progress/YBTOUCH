package com.ideafactory.client.business.localnetcall;

import android.util.Log;

/**
 * 服务端和客户端的中间部分
 */
public class SocketControl {

    private static SocketControl socketControl;
    private String serverIpAddress;
    private boolean isMainServer;
    private boolean isUnicomScreen=false;
    public static final String TAG = "clientport";
    private SocketControl(){

    }

    public static SocketControl getInstance() {
        if (socketControl == null) {
            socketControl = new SocketControl();
        }
        return socketControl;
    }


    public boolean isUnicomScreen() {
        return isUnicomScreen;
    }

    public void setUnicomScreen(boolean unicomScreen) {
        isUnicomScreen = unicomScreen;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public void setIsMainServer(boolean isMainServer) {
        this.isMainServer = isMainServer;
    }

    public boolean getIsMainServer(){
        return isMainServer;
    }
    /**
     * 初始化
     */
    public void connectSocket() {
        Log.i(TAG, "初始化connectSocket:" + isMainServer);
        if (isMainServer) {
            if (isUnicomScreen){
                UnicomServerSide.getInstance().startServerThread();
            }else {
                ServerSide.getInstance().startServerThread();
            }
        }else {
            ClientSide.getInstance().setServerIpAddress(serverIpAddress);
        }


    }

    public void disconnectSocket() {
        Log.i(TAG, "disconnectSocket:" + isMainServer);
        if (isMainServer) {
            //断开服务器连接
            ServerSide.getInstance().disconnectSocket();
        }
        //断开客户端连接
        ClientSide.getInstance().disconnectSocket();
    }

    public void sendData(String message) {
        ClientSide.getInstance().sendData(message);
    }
}

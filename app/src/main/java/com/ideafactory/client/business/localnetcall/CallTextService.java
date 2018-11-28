package com.ideafactory.client.business.localnetcall;

import com.ideafactory.client.business.draw.layout.bean.LayoutPosition;

/**
 * 调用服务中方法的接口
 */
public interface CallTextService {
     void callMethodInService(String textNum);
     void hideMessageWindow();
     void setShowLayoutParameter(LayoutPosition layoutParameter);
}

/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ideafactory.client.business.weichat;

import com.ideafactory.client.MainActivity;
import com.ideafactory.client.heartbeat.HeartBeatClient;
import com.ideafactory.client.xmpp.ServiceManager;

/**
 * This is an androidpn client demo application.
 * @author xiongcheng
 */
public class PnServerActivity {
	private static ServiceManager serverManager ;

	public static void startXMPP() {

		if (serverManager == null) {
			MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
			serverManager = new ServiceManager(mainActivity);
			serverManager.startService();
		}
	}

	public static void stopXMPP() {
		if (serverManager != null) {
			serverManager.stopService();
			serverManager = null;
		}
	}
}
package com.ideafactory.client.common.net;

import com.ideafactory.client.util.HandleMessageUtils;

public class FileCacheTool {
	private static final String TAG = "FileCacheTool";

	public static boolean downWeiCacheFile(String urlAddres, String saveName) {
		long size = FileUtil.downWeiFile(saveName, urlAddres);
		return size > 0;
	}

    public static void downWebCacheFile(final String urlAddres, final String saveName, final DownloadCounter downloadCounter) {
        HandleMessageUtils.getInstance().runInThread(new Runnable() {
            @Override
            public void run() {
                FileUtil.downWebFile(saveName, urlAddres, downloadCounter);
            }
        });
    }

}

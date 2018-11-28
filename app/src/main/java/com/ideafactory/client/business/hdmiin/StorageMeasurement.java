/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.ideafactory.client.business.hdmiin;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.android.internal.app.IMediaContainerService;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for measuring the disk usage of internal storage or a physical
 * {@link StorageVolume}. Connects with a remote {@link IMediaContainerService}
 * and delivers results to {@link MeasurementReceiver}.
 */
public class StorageMeasurement {
    private static final String TAG = "StorageMeasurement";

    private static final boolean LOCAL_LOGV = true;
    static final boolean LOGV = LOCAL_LOGV && Log.isLoggable(TAG, Log.VERBOSE);

    private static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";

    public static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(
            DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");


    private static StorageMeasurement sInstances;

    /**
     * Obtain shared instance of {@link StorageMeasurement} for given physical
     * {@link StorageVolume}, or internal storage if {@code null}.
     */
    public static StorageMeasurement getInstance(Context context, StorageVolume volume) {
        if (sInstances == null) {
            sInstances = new StorageMeasurement(
                    context.getApplicationContext(), volume);
        }
        return sInstances;
    }

    public static class MeasurementDetails {
        public long totalSize;
        public long availSize;
    }


    /**
     * Physical volume being measured, or {@code null} for internal.
     */
    private final StorageVolume mVolume;

    private final boolean mIsInternal;
    private final boolean mIsPrimary;

    private final MeasurementHandler mHandler;

    private long mTotalSize;
    private long mAvailSize;

    private long MAX_LARGE_AVAILSIZE = 100 * 1024 * 1024;

    List<FileInfo> mFileInfoFoStoreDir;

    @SuppressLint("NewApi")
    private StorageMeasurement(Context context, StorageVolume volume) {
        mVolume = volume;
        mIsInternal = volume == null;
        mIsPrimary = volume != null ? volume.isPrimary() : false;

        // Start the thread that will measure the disk usage.
        final HandlerThread handlerThread = new HandlerThread("MemoryMeasurement");
        handlerThread.start();
        mHandler = new MeasurementHandler(context, handlerThread.getLooper());
    }


    public void measure() {
        if (!mHandler.hasMessages(MeasurementHandler.MSG_MEASURE)) {
            mHandler.sendEmptyMessage(MeasurementHandler.MSG_MEASURE);
        }
    }

    public void cleanUp() {
        mHandler.removeMessages(MeasurementHandler.MSG_MEASURE);
        mHandler.sendEmptyMessage(MeasurementHandler.MSG_DISCONNECT);
    }

    public void invalidate() {
        mHandler.sendEmptyMessage(MeasurementHandler.MSG_INVALIDATE);
    }

    private class MeasurementHandler extends Handler {
        public static final int MSG_MEASURE = 1;
        public static final int MSG_CONNECTED = 2;
        public static final int MSG_DISCONNECT = 3;
        public static final int MSG_COMPLETED = 4;
        public static final int MSG_INVALIDATE = 5;

        private Object mLock = new Object();

        private IMediaContainerService mDefaultContainer;

        private volatile boolean mBound = false;


        private final WeakReference<Context> mContext;

        private final ServiceConnection mDefContainerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final IMediaContainerService imcs = IMediaContainerService.Stub.asInterface(
                        service);
                mDefaultContainer = imcs;
                mBound = true;
                sendMessage(obtainMessage(MSG_CONNECTED, imcs));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
                removeMessages(MSG_CONNECTED);
            }
        };

        public MeasurementHandler(Context context, Looper looper) {
            super(looper);
            mContext = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MEASURE: {

                    final Context context = (mContext != null) ? mContext.get() : null;
                    if (context == null) {
                        return;
                    }

                    synchronized (mLock) {
                        /*if (mBound) {
                            removeMessages(MSG_DISCONNECT);
                            sendMessage(obtainMessage(MSG_CONNECTED, mDefaultContainer));
                        } else {
                            Intent service = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
                            context.bindService(service, mDefContainerConn, Context.BIND_AUTO_CREATE);
                        }*/
                    }
                    break;
                }
                case MSG_CONNECTED: {
                    IMediaContainerService imcs = (IMediaContainerService) msg.obj;
                    measureApproximateStorage(imcs);
                    measureExactStorage(imcs);
                    break;
                }
                case MSG_DISCONNECT: {
                    synchronized (mLock) {
                        if (mBound) {
                            final Context context = (mContext != null) ? mContext.get() : null;
                            if (context == null) {
                                return;
                            }

                            mBound = false;
                            context.unbindService(mDefContainerConn);
                        }
                    }
                    break;
                }
                case MSG_COMPLETED: {
                    break;
                }
                case MSG_INVALIDATE: {
                    break;
                }
            }
        }

        private void measureApproximateStorage(IMediaContainerService imcs) {
            final String path = mVolume != null ? mVolume.getPath()
                    : Environment.getDataDirectory().getPath();
            try {
                final long[] stats = imcs.getFileSystemStats(path);
                mTotalSize = stats[0];
                mAvailSize = stats[1];
                Log.d(TAG, "measureApproximateStorage(" + path + ") returned mTotalSize:" + mTotalSize + ",mAvailSize" + mAvailSize);
            } catch (Exception e) {
                Log.w(TAG, "Problem in container service", e);
            }
        }

        private void measureExactStorage(IMediaContainerService imcs) {
            final Context context = mContext != null ? mContext.get() : null;
            if (context == null) {
                return;
            }
            final MeasurementDetails details = new MeasurementDetails();
            final Message finished = obtainMessage(MSG_COMPLETED, details);

            details.totalSize = mTotalSize;
            details.availSize = mAvailSize;

            long availSize = mAvailSize;

            if (mAvailSize > MAX_LARGE_AVAILSIZE) return;

            measureStoreDir(imcs, CameraUtil.getMediaDir());
            int count = 0;
            while (availSize < MAX_LARGE_AVAILSIZE && count < mFileInfoFoStoreDir.size()) {
                FileInfo fileInfo = mFileInfoFoStoreDir.get(count);

                //  Log.d(TAG, availSize+"del   == DirectorySize(" + fileInfo.mFileName + ") returned "+fileInfo.mSize );
                File dir = new File(fileInfo.mFileName);
                if (delDir(dir)) {
                    availSize += fileInfo.mSize;
                    count++;
                }

            }
            finished.sendToTarget();
        }
    }

    public boolean delDir(File dir) {
        boolean ret = true;
        File[] file = dir.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {

                if (!file[i].delete()) {
                    Log.e(TAG,
                            "  ------- :    Delete file " + file[i].getPath()
                                    + " fail~~");
                    ret = false;
                }
            } else {
                delDir(file[i]);
            }
        }
        dir.delete();
        return ret;
    }

    private static long getDirectorySize(IMediaContainerService imcs, File path) {
        try {
            final long size = imcs.calculateDirectorySize(path.toString());
            // Log.d(TAG, "getDirectorySize(" + path + ") returned " + size);
            return size;
        } catch (Exception e) {
            Log.w(TAG, "Could not read memory from default container service for " + path, e);
            return 0;
        }
    }

    private long measureStoreDir(IMediaContainerService imcs, File dir) {
        mFileInfoFoStoreDir = new ArrayList<FileInfo>();

        final File[] files = dir.listFiles();
        if (files == null) return 0;

        // Get sizes of all top level nodes except the ones already computed
        long counter = 0;
        long miscSize = 0;

        for (File file : files) {
            final String path = file.getAbsolutePath();
            final String name = file.getName();

            if (file.isFile()) {
                final long fileSize = file.length();
                mFileInfoFoStoreDir.add(new FileInfo(path, fileSize, counter++));
                miscSize += fileSize;
            } else if (file.isDirectory()) {
                final long dirSize = getDirectorySize(imcs, file);
                mFileInfoFoStoreDir.add(new FileInfo(path, dirSize, counter++));
                miscSize += dirSize;
            } else {
                // Non directory, non file: not listed
            }
        }
        // sort the list of FileInfo objects collected above in descending order of their sizes
        Collections.sort(mFileInfoFoStoreDir);
        return miscSize;
    }

    static class FileInfo implements Comparable<FileInfo> {
        final String mFileName;
        final long mSize;
        final long mId;

        FileInfo(String fileName, long size, long id) {
            mFileName = fileName;
            mSize = size;
            mId = id;
        }

        @Override
        public int compareTo(FileInfo that) {
            if (this == that || mFileName.equals(that.mFileName)) return 0;
            else return this.mFileName.compareToIgnoreCase(that.mFileName);// for descending sort
        }

        @Override
        public String toString() {
            return mFileName + " : " + mSize + ", id:" + mId;
        }
    }
}

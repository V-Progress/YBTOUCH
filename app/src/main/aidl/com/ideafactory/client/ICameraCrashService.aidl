// ICameraCrashService.aidl
package com.ideafactory.client;

// Declare any non-default types here with import statements

interface ICameraCrashService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean prepareMedia();
	void stopRecording();
	boolean startRecording();
	int getRecordState();
	void startPreview();
	void stopPreview() ;
	void pip(int width, int heigth);
}

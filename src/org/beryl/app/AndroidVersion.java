package org.beryl.app;

public class AndroidVersion {
	private static final int _ANDROID_SDK_VERSION;
	
	static {
		int android_sdk = 3;
		try {
			android_sdk = Integer.parseInt(android.os.Build.VERSION.SDK);
		}
		catch (Exception e) { }
		finally {}
		
		_ANDROID_SDK_VERSION = android_sdk;
	}
	
	public static int getSdkVersion() {
		return _ANDROID_SDK_VERSION;
	}
	
	public static boolean isEclairOrHigher() {
		return _ANDROID_SDK_VERSION >= android.os.Build.VERSION_CODES.ECLAIR;
	}
}

package com.moodstream.util;

public class AWSUtils {
public static final String AUDIENCE="server:client_id:295173411808-0rihfll869kme3o6kpl3llc2dk13gp25.apps.googleusercontent.com";
	
	//AWS S3 CREDENTIALS & CONSTANTS
	public static String ACCESS_KEY_ID = "AKIAI2HQRUQNPA6TWV7A";
	public static final String SECRET_KEY = "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";
	public static final String PICTURE_BUCKET = "moodstream2-bucket";
		
	public static String getPictureBucket()
	{
		return PICTURE_BUCKET;
	}
}

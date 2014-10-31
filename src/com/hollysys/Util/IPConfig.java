package com.hollysys.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.content.Context;
import android.util.Log;

import com.hollysys.holliscadamobile.R;

/**
 * IP地址配置类
 * @author huangdebao
 *
 */
public class IPConfig {

	private static String ip;
	private static String port;
	private static String fileName = "IPConfig.properties";
		
	public static void readConfigFile(Context context) {
		Properties props = new Properties();
		try {
			File file = new File(context.getFilesDir(), fileName);
			InputStream is;
			if(file.exists())
				is = context.openFileInput(fileName);
			else
				is = context.getResources().openRawResource(R.raw.ip_config); 
			props.load(is);
			ip = props.getProperty("ip");
			port = props.getProperty("port");
			is.close();
		} catch (Exception e) {
			Log.i("HollySys", "IPConfig.class :" + e.getMessage());
		}
	}
	
	public static void writeConfigFile(Context context){
		 Properties prop = new Properties();
	      try {
	            OutputStream fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);  
	            prop.setProperty("ip", ip);
	            prop.setProperty("port", port);
	             //以适合使用 load 方法加载到 Properties 表中的格式，
	            //将此 Properties 表中的属性列表（键和元素对）写入输出流
	            prop.store(fos, "Update  value");
	            fos.close();
	         } catch (IOException e) {
	        	 Log.i("HollySys", "IPConfig.class :" + e.getMessage());
	         }
	}
	public static String getIp() {
		return ip;
	}
	public static void setIp(String ip) {
		IPConfig.ip = ip;
	}
	public static String getPort() {
		return port;
	}
	public static void setPort(String port) {
		IPConfig.port = port;
	}
}

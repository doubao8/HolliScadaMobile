package com.hollysys.service;

import java.util.ArrayList;
import java.util.List;

import com.hollysys.util.ParseXML;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * 定义服务类，获取报警信息
 * @author huangdebao
 *
 */
public class MyService extends Service {

	private final IBinder mBinder = new LocalBinder();
	private List<String> list; //随机种子
	private Thread myThread;
	private Handler handler=null;  //处理更新界面
	private Runnable runnable; //处理界面程序体
	
	public class LocalBinder extends Binder {
		public MyService getService() {
			Log.i("HollySys", "getService ---> " + MyService.this);
			return MyService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("HollySys", "MyService onBind");
		return mBinder;	
		//如果这边不返回一个IBinder的接口实例，那么ServiceConnection中的onServiceConnected就不会被调用
		//那么bind所具有的传递数据的功能也就体现不出来（这个返回值是被作为onServiceConnected中的第二个参数的）
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i("HollySys", "MyService onCreate");
		super.onCreate();
		
		ParseXML.parseXml(this);
		
		list = new ArrayList<String>();
		list.add("C906ZCSYS5");
		list.add("连采二队");
		list.add("101上仓皮带");
		list.add("31煤机头");
		list.add("水文监测");
		list.add("连采一队局扇");
		list.add("中央2#水泵房_1");
		list.add("C906ZCSYS6");
		list.add("HZSG");
		list.add("GL");
		
		myThread = new MyThread();
		myThread.start();
		
	}

	private void randomSetIsAlarm(List<String> list) {
		ParseXML.setAllElementAttr("IsAlarmming", "0");
		List<String> randList = new ArrayList<String>();
		while(randList.size()<4){ //随机四个
			int rand = (int)(Math.random()*9);
			randList.add(list.get(rand));
		}
		ParseXML.setIsAlarmmingEqualsOne(randList);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("HollySys", "MyService onDestroy");
		super.onDestroy();
		myThread.interrupt();
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		Log.i("HollySys", "MyService onStart");
		super.onStart(intent, startId);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("HollySys", "MyService onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("HollySys", "MyService onUnbind");
		this.handler=null;
		this.runnable=null;
		super.onUnbind(intent);
		return true;
	}

	/**
	 * 随机产生报警数据
	 * @author 黄德宝
	 *
	 */
	public class MyThread extends Thread {
	    private boolean isInterrupted=false;
	   
	   public void interrupt(){
	       isInterrupted = true;
	       super.interrupt();
	      }
	   
	   public void run(){
	       while(!isInterrupted){
	           try{
	        	   randomSetIsAlarm(list);
	        	   if(null!=handler && null !=runnable)
	        		   handler.post(runnable);
//	        	   Log.i("HollySys", "重新生成数据");
	               Thread.sleep(10000);
	           }catch(InterruptedException e){
	        	   break;
	           }
	       }
	       Log.i("HollySys", "线程退出");
	   }
	}
	
	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
}


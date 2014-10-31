package com.hollysys.holliscadamobile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.hollysys.basic.Dialog;
import com.hollysys.basic.ExitApplication;
import com.hollysys.service.MyService;
import com.hollysys.util.ParseXML;
import com.hollysys.util.Util;

public class MainActivity extends Activity implements OnGestureListener, OnTouchListener  {
	private List<Element> childNodes;  //配置文件中读取的一级菜单项
	private ViewFlipper myViewFlipper;  //保存动态生成的GridView
	private GestureDetector myGestureDetector;  // 定义一个(手势识别类)对象的引用   
	private LinearLayout dotLayout; //切换图形时底部原点显示效果
	private Handler handler=null;  //处理更新界面
	private ServiceConnection sc;  
	private MyService myService;
	private boolean isBind = false; //是否绑定服务
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("HollySys", "MainActivity onCreate");
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_main);
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);  
		Element root =ParseXML.getRoot();
		childNodes = ParseXML.findChild(root);
		myViewFlipper = (ViewFlipper) findViewById(R.id.menuViewFlipper);
		dotLayout = (LinearLayout) findViewById(R.id.dot_layout); 
		myGestureDetector = new GestureDetector(this, this); 
		
		//创建属于主线程的handler ，主要处理更新界面
		handler=new Handler();  
		setServiceConnection(); //设置与服务绑定连接
		//构造初始界面
		initUI(factory);
	}
	
	//设置ServiceConnection
	private void setServiceConnection() {
		sc = new ServiceConnection() {
			/*
			 * 只有在MyService中的onBind方法中返回一个IBinder实例才会在Bind的时候
			 * 调用onServiceConnection回调方法
			 * 第二个参数service就是MyService中onBind方法return的那个IBinder实例，可以利用这个来传递数据
			 */
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				myService = ((MyService.LocalBinder) service).getService();
				// 更新界面
				final Runnable refreshUIRunnable = new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < myViewFlipper.getChildCount(); i++) {
							GridView view = (GridView) myViewFlipper.getChildAt(i);
							MainAdapter adaper = (MainAdapter) view.getAdapter();
							adaper.notifyDataSetChanged();
						}
					}
				};
				myService.setHandler(handler);
				myService.setRunnable(refreshUIRunnable);
				Log.i("HollySys", "myService Connected");	
			}
			@Override
			public void onServiceDisconnected(ComponentName name) {
				/*
				 * 只有在service因异常而断开连接的时候，这个方法才会用到
				 */
				sc = null;
				Log.i("TAG", "onServiceDisconnected : ServiceConnection --->"
						+ sc);
			}
		};
	}
	/**
	 * 初始化界面，动态生成滑动界面及底部原点显示效果
	 * @param factory
	 */
	private void initUI(LayoutInflater factory) {
		int menuCount = calcMenuItemCount(); //计算每页显示菜单项个数
		
		int count = childNodes.size()/menuCount+1; //计算界面数
		
		for(int i=0; i<count; i++){
			GridView view = (GridView) factory.inflate(R.layout.grid_view, null);
			view.setOnTouchListener(this); //设置手势触摸监听器
			List<Element> nodes = new ArrayList<Element>();
			int num = (i+1)*menuCount; //每个界面显示菜单项的个数
			if(num>childNodes.size())
				num=childNodes.size();
			for(int j=menuCount*i; j<num; j++){
				nodes.add(childNodes.get(j));
			}
			MainAdapter adapter = new MainAdapter(nodes);
			view.setAdapter(adapter); //设置Adapter
			view.setOnItemClickListener(new ItemClickListener());
			
			myViewFlipper.addView(view);
			
			//设置底部原点切换显示效果
			if(count<=1)
				continue;
			ImageView imageView = new ImageView(MainActivity.this);
			if(0==i)
				imageView.setImageResource(R.drawable.bullet_green);
			else
				imageView.setImageResource(R.drawable.bullet_white);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(     
					LinearLayout.LayoutParams.WRAP_CONTENT,     
					LinearLayout.LayoutParams.WRAP_CONTENT     
					);   
			dotLayout.addView(imageView, params);
		}			
	}
	
	/**
	 * 计算每页显示菜单项个数
	 * @return 子项个数
	 */
	private int calcMenuItemCount() {
		ImageView image = new ImageView(MainActivity.this);
		image.setBackgroundResource(R.drawable.mo_ren);
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
		int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
		image.measure(w, h);  
		int height =image.getMeasuredHeight();  
		TextView text = new TextView(MainActivity.this);
		text.measure(w, h);  

		h = height+text.getMeasuredHeight()+ 30; //行间距为30
		DisplayMetrics dm = getResources().getDisplayMetrics(); 
		
		int screenHeight = dm.heightPixels-h-h; //减去标题栏的高度，再减去预留空间高度
		int menuCount = (screenHeight/h)*3; //每个界面菜单个数
		return menuCount;
	}

	@Override
	public void onResume() {
		Log.i("HollySys", "MainActivity onResume");
		//绑定服务
		Intent intentService = new Intent(); 
		intentService.setAction("com.hollysys.service.AlARM_SERVER");
		bindService(intentService, sc, Context.BIND_AUTO_CREATE);
		isBind=true;
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.i("HollySys", "MainActivity onPause");
		if(isBind){//解除绑定服务
			unbindService(sc);
			isBind=false;
		}
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public final class ViewHolder {
		public ImageView img;
		public ImageView updateImg;
		public TextView title;
	}
	
	class MainAdapter extends BaseAdapter {

		private List<Element> childElements = null;

		public MainAdapter(List<Element> childNodes) {
			this.childElements = childNodes;
		}

		public List<Element> getChildElement(){
			return childElements;
		}
		
		@Override
		public int getCount() {
			return childElements.size();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(MainActivity.this,R.layout.first_menu, null);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.updateImg = (ImageView) convertView.findViewById(R.id.update_img);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(null!=childElements && childElements.size()>0){
				Element node =(Element) childElements.get(position);
				String title = node.getAttribute("MenuName");
				String menuIcon = node.getAttribute("MenuIcon");
				String imgName = "";
				if(!menuIcon.equals("")){
					imgName = (menuIcon.split("\\."))[0];
				}
				if(!imgName.equals("")){
					Integer imgCode = (Integer)Util.getPropertyValue(R.drawable.class,imgName);
					if(null==imgCode)
						imgCode = R.drawable.mo_ren;
					holder.img.setBackgroundResource(imgCode);
				}
				holder.title.setText(title);
				if(node.getAttribute("IsAlarmming").equals("0")){
					holder.updateImg.setVisibility(View.INVISIBLE);
				}else{
					holder.updateImg.setVisibility(View.VISIBLE);
				}
			}

			return convertView;
		}
	}

	//当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件     
	class  ItemClickListener implements OnItemClickListener  {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			MainAdapter adapter = (MainAdapter)parent.getAdapter();
			List<Element> childElements = adapter.getChildElement();
			Element element =(Element) childElements.get(position);
			List<Element> childs = ParseXML.findChild(element);
			if(!childs.isEmpty()){
				Intent intent = new Intent(MainActivity.this, MenuActivity.class);
				ParseXML.setCurrentElement(element);
				intent.putExtra("MenuName", element.getAttribute("MenuName"));
				
				String menuIcon = element.getAttribute("MenuIcon");
				String imgName = "";
				if(!menuIcon.equals("")){
					imgName = (menuIcon.split("\\."))[0];
				}
				if(!imgName.equals("")){
					Integer imgCode = (Integer)Util.getPropertyValue(R.drawable.class,imgName);
					if(null==imgCode)
						imgCode = (Integer)Util.getPropertyValue(R.drawable.class,"mo_ren");
					intent.putExtra("MenuIcon", imgCode);
				}
				
				startActivity(intent);
			}
			else{
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra("Page", element.getAttribute("Page"));
				startActivity(intent);
			}
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return myGestureDetector.onTouchEvent(event); 
	}

	

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// 参数e1是按下事件，e2是放开事件，剩下两个是滑动的速度分量，这里用不到
		// 按下时的横坐标大于放开时的横坐标，从右向左滑动
		if (e1.getX() - e2.getX() > 120) {
			if (myViewFlipper.getDisplayedChild() == myViewFlipper.getChildCount()-1) {
				myViewFlipper.stopFlipping();
				return false;
			} else {
				this.myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
						this, R.anim.left_in));
				this.myViewFlipper.setOutAnimation(AnimationUtils
						.loadAnimation(this, R.anim.left_out));
				
				//切换底部圆点
				if(dotLayout.getChildCount()>1){
					ImageView currentImageView = (ImageView)dotLayout.getChildAt(myViewFlipper.getDisplayedChild());
					currentImageView.setImageResource(R.drawable.bullet_white);
					ImageView nextImageView = (ImageView)dotLayout.getChildAt(myViewFlipper.getDisplayedChild()+1);
					nextImageView.setImageResource(R.drawable.bullet_green);
				}
			
				this.myViewFlipper.showNext();
				return true;
			}
		} else if (e1.getX() - e2.getX() < -120) { // 按下时的横坐标小于放开时的横坐标，从左向右滑动
			if (myViewFlipper.getDisplayedChild() == 0) {
				myViewFlipper.stopFlipping();
				return false;
			} else {
				this.myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
						this, R.anim.right_in));
				this.myViewFlipper.setOutAnimation(AnimationUtils
						.loadAnimation(this, R.anim.right_out));
				
				//切换底部圆点
				if(dotLayout.getChildCount()>1){
					ImageView currentImageView = (ImageView)dotLayout.getChildAt(myViewFlipper.getDisplayedChild());
					currentImageView.setImageResource(R.drawable.bullet_white);
					ImageView nextImageView = (ImageView)dotLayout.getChildAt(myViewFlipper.getDisplayedChild()-1);
					nextImageView.setImageResource(R.drawable.bullet_green);
				}
				
				this.myViewFlipper.showPrevious();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}

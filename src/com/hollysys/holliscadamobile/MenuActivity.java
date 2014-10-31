package com.hollysys.holliscadamobile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hollysys.basic.Dialog;
import com.hollysys.basic.ExitApplication;
import com.hollysys.holliscadamobile.MainActivity.MainAdapter;
import com.hollysys.service.MyService;
import com.hollysys.util.ParseXML;
import com.hollysys.util.Util;

public class MenuActivity extends ExpandableListActivity  {
	private List<Element> groupList;
	private List<List<Element>> childList;
	private ExpandableListView myExpandableListView;
	private Handler handler=null;  //处理更新界面
	private ServiceConnection sc;  
	private MyService myService;
	private boolean isBind = false; //是否绑定服务

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		//创建属于主线程的handler  
		  
		setContentView(R.layout.activity_menu);
		initializeData(); 
		this.setTitle(this.getIntent().getExtras().getString("MenuName"));
		ActionBar actionBar=getActionBar();
		actionBar.setIcon(this.getIntent().getExtras().getInt("MenuIcon"));
		actionBar.setDisplayUseLogoEnabled(false);
		initListView();
		
		handler=new Handler();
		setServiceConnection(); //设置与服务绑定连接
	}

	/**
	 * 初始化ExpandableListView
	 */
	private void initListView() {
		MyexpandableListAdapter myAdapter = new MyexpandableListAdapter(MenuActivity.this);
		myExpandableListView=getExpandableListView();
		myExpandableListView.setAdapter(myAdapter);  
		myExpandableListView.setCacheColorHint(0);  //设置拖动列表的时候防止出现黑色背景
		myExpandableListView  //设置每次只展开一组
				.setOnGroupExpandListener(new OnGroupExpandListener() {

					@Override
					public void onGroupExpand(int groupPosition) {
						// TODO Auto-generated method stub
						for (int i = 0; i < groupList.size(); i++) {
							if (groupPosition != i) {
								myExpandableListView.collapseGroup(i);
							}
						}
					}
				});
		myExpandableListView.setOnGroupClickListener(new OnGroupClickListener(){

	            @Override
	            public boolean onGroupClick(ExpandableListView parent, View v,
	                    int groupPosition, long id) {
	                // TODO Auto-generated method stub
	                if(childList.get(groupPosition).isEmpty()){
	                	Element element = groupList.get(groupPosition); 
	                	Intent intent = new Intent(MenuActivity.this, WebViewActivity.class);
						intent.putExtra("Page", element.getAttribute("Page"));
						startActivity(intent);
	                	return true;
	                }
	                return false;
	            }});
		myExpandableListView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Element element =childList.get(groupPosition).get(childPosition);
				List<Element> childs = ParseXML.findChild(element);
				if(!childs.isEmpty()){
					Intent intent = new Intent(MenuActivity.this, MenuActivity.class);
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
					Intent intent = new Intent(MenuActivity.this, WebViewActivity.class);
					intent.putExtra("Page", element.getAttribute("Page"));
					startActivity(intent);
				}
				return true;
			}
			
		});
	}

	// 设置ServiceConnection
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
				// 更新界面
				final Runnable refreshUIRunnable = new Runnable() {
					@Override
					public void run() {
						((MyexpandableListAdapter) myExpandableListView
								.getExpandableListAdapter())
								.notifyDataSetChanged();
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
	
	private void initializeData(){
		Element currentElement = ParseXML.getCurrentElement();
		groupList = ParseXML.findChild(currentElement);
		childList = new ArrayList<List<Element>>();
		
		for(int i = 0; i < groupList.size(); i++){
			List<Element> child = ParseXML.findChild(groupList.get(i));
			childList.add(child);
		}
	}

	public final class ViewHolder {
		public ImageView img;
		public ImageView updateImg;
		public TextView title;
	}
	
	class MyexpandableListAdapter extends BaseExpandableListAdapter {
		private Context context;
		private LayoutInflater inflater;

		public MyexpandableListAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		// 返回父列表个数
		@Override
		public int getGroupCount() {
			return groupList.size();
		}

		// 返回子列表个数
		@Override
		public int getChildrenCount(int groupPosition) {
			return childList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {

			return groupList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {

			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			Element father = (Element)getGroup(groupPosition);
			ViewHolder groupHolder = null;
			if (convertView == null) {
				groupHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.text_image_line, null);
				groupHolder.img = (ImageView) convertView.findViewById(R.id.menu_img);
				groupHolder.updateImg = (ImageView) convertView.findViewById(R.id.update_img);
				groupHolder.title = (TextView) convertView.findViewById(R.id.menu_title);
				convertView.setTag(groupHolder);
			} else {
				groupHolder = (ViewHolder) convertView.getTag();
			}
			groupHolder.title.setText(father.getAttribute("MenuName"));
			if(!ParseXML.findChild(father).isEmpty()){
				if (isExpanded)// ture is Expanded or false is not isExpanded
					groupHolder.img.setImageResource(R.drawable.shou_suo);
				else
					groupHolder.img.setImageResource(R.drawable.zhan_kai);
			}
			if(father.getAttribute("IsAlarmming").equals("0")){
				groupHolder.updateImg.setVisibility(View.INVISIBLE);
			}else{
				groupHolder.updateImg.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			Element child = (Element)getChild(groupPosition,childPosition);
			ViewHolder groupHolder = null;
			if (convertView == null) {
				groupHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.activity_second_menu, null);
				groupHolder.title = (TextView) convertView.findViewById(R.id.second_menu);
				groupHolder.updateImg = (ImageView) convertView.findViewById(R.id.update_img);
				convertView.setTag(groupHolder);
			}else {
				groupHolder = (ViewHolder) convertView.getTag();
			}
			groupHolder.title.setText(child.getAttribute("MenuName"));
			if(child.getAttribute("IsAlarmming").equals("0")){
				groupHolder.updateImg.setVisibility(View.INVISIBLE);
			}else{
				groupHolder.updateImg.setVisibility(View.VISIBLE);
			}
			return convertView;
		}


		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
 
	@Override
	public void onResume() {
		Log.i("HollySys", "MenuActivity onResume");
		//绑定服务
		Intent intentService = new Intent(); 
		intentService.setAction("com.hollysys.service.AlARM_SERVER");
		bindService(intentService, sc, Context.BIND_AUTO_CREATE);
		isBind=true;
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.i("HollySys", "MenuActivity onPause");
		if(isBind){//解除绑定服务
			unbindService(sc);
			isBind=false;
		}
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
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
}

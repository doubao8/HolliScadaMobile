package com.hollysys.holliscadamobile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.hollysys.Util.ParseXML;
import com.hollysys.Util.Util;
import com.hollysys.basic.Dialog;
import com.hollysys.basic.ExitApplication;

public class MainActivity extends Activity implements OnGestureListener, OnTouchListener  {
	private List<Element> childNodes;  //配置文件中读取的一级菜单项
	private ViewFlipper myViewFlipper;  //保存动态生成的GridView
	private GestureDetector myGestureDetector;  // 定义一个(手势识别类)对象的引用   
	private LinearLayout dotLayout; //切换图形时底部原点显示效果
	 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_main);
		
		LayoutInflater factory = LayoutInflater.from(MainActivity.this);  
		ParseXML.parseXml(this); //解析配置文件
		Element root =ParseXML.getRoot();
		childNodes = ParseXML.findChild(root);
		myViewFlipper = (ViewFlipper) findViewById(R.id.menuViewFlipper);
		dotLayout = (LinearLayout) findViewById(R.id.dot_layout); 
		myGestureDetector = new GestureDetector(this, this); 
		//构造初始界面
		initUI(factory);
	}
	/**
	 * 初始化界面，动态生成滑动界面及底部原点显示效果
	 * @param factory
	 */
	private void initUI(LayoutInflater factory) {
		int count = childNodes.size()/12+1; //每个界面最多显示十二个菜单项，计算界面数
		for(int i=0; i<count; i++){
			GridView view = (GridView) factory.inflate(R.layout.grid_view, null);
			view.setOnTouchListener(this); //设置手势触摸监听器
			List<Element> nodes = new ArrayList<Element>();
			int num = (i+1)*12; //每个界面显示菜单项的个数
			if(num>childNodes.size())
				num=childNodes.size();
			for(int j=12*i; j<num; j++){
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
						imgCode = (Integer)Util.getPropertyValue(R.drawable.class,"mo_ren");
					holder.img.setBackgroundResource(imgCode);
				}
				holder.title.setText(title);
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
			if(element.getAttribute("Page").equals("")){
				Intent intent = new Intent(MainActivity.this, SecondMenuActivity.class);
				ParseXML.setCurrentElement(element);
				intent.putExtra("MenuName", element.getAttribute("MenuName"));
				startActivity(intent);
			}
			else{
				Dialog.alert(MainActivity.this, element.getAttribute("MenuName"));
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

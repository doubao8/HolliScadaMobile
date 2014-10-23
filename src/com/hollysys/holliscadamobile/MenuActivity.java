package com.hollysys.holliscadamobile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hollysys.Util.ParseXML;
import com.hollysys.Util.Util;
import com.hollysys.basic.Dialog;

public class MenuActivity extends ExpandableListActivity  {
	private List<Element> groupList;
	private List<List<Element>> childList;
	private ExpandableListView myExpandableListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		initializeData(); 
		this.setTitle(this.getIntent().getExtras().getString("MenuName"));
		ActionBar actionBar=getActionBar();
		actionBar.setIcon(this.getIntent().getExtras().getInt("MenuIcon"));
		actionBar.setDisplayUseLogoEnabled(false);
		initListView();
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
	                	Dialog.alert(MenuActivity.this, element.getAttribute("MenuName"));
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
					Dialog.alert(MenuActivity.this, element.getAttribute("MenuName"));
				}
				return true;
			}
			
		});
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
				groupHolder.title = (TextView) convertView.findViewById(R.id.menu_title);
				convertView.setTag(groupHolder);
			} else {
				groupHolder = (ViewHolder) convertView.getTag();
			}
			groupHolder.title.setText(father.getAttribute("MenuName"));
			if(!ParseXML.findChild(father).isEmpty()){
				if (isExpanded)// ture is Expanded or false is not isExpanded
					groupHolder.img.setImageResource(R.drawable.shou_suo1);
				else
					groupHolder.img.setImageResource(R.drawable.zhan_kai1);
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
				convertView.setTag(groupHolder);
			}else {
				groupHolder = (ViewHolder) convertView.getTag();
			}
			groupHolder.title.setText(child.getAttribute("MenuName"));
			return convertView;
		}


		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
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

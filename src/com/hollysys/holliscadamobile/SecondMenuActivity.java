package com.hollysys.holliscadamobile;

import java.util.List;

import org.w3c.dom.Element;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hollysys.Util.ParseXML;
import com.hollysys.basic.ExitApplication;

public class SecondMenuActivity extends ListActivity {
	private List<Element> childElements;  //含有子菜单项
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitApplication.getInstance().addActivity(this);
		Element currentElement = ParseXML.getCurrentElement();
		childElements = ParseXML.findChild(currentElement);
		this.setTitle(this.getIntent().getExtras().getString("MenuName"));
		MyAdapter adapter = new MyAdapter(this);
		setListAdapter(adapter);
	}

	public final class ViewHolder {
		public ImageView img;
		public TextView title;
	}
	
	class MyAdapter extends BaseAdapter {

		private LayoutInflater mInflater = null;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
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

				convertView = mInflater.inflate(R.layout.activity_second_menu, null);
//				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.second_menu);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

//			holder.img.setBackgroundResource((Integer) lstData.get(position)
//					.get("img"));
			holder.title.setText( childElements.get(position).getAttribute("MenuName"));

			return convertView;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
//		String info= (String) lstData.get(position).get("info");
//		Intent intent;
//		if(info.equals("GongYeDianShi")){
//			intent = new Intent(MainActivity.this, VideoActivity.class);
//		}else{
//			intent = new Intent(MainActivity.this, SvgActivity.class);
//		}
//		Bundle bundle = new Bundle();
//		bundle.putString("svg", (String) lstData.get(position).get("info"));
//		intent.putExtra("svgInfo", bundle);
//
//		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second_menu, menu);
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

package com.hollysys.holliscadamobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.hollysys.basic.Dialog;
import com.hollysys.basic.ExitApplication;

public class LoginActivity extends Activity {
	
	private EditText txtLoginUsername = null;

	private EditText txtLoginPassword = null;

	private Button btnLoginLogin = null;

	private Button btnLoginClose = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		this.setTitle(R.string.title_name);

		this.txtLoginUsername = (EditText) this
				.findViewById(R.id.txtLoginUsername);
		this.txtLoginUsername.setText("admin");

		this.txtLoginPassword = (EditText) this
				.findViewById(R.id.txtLoginPassword);

		this.btnLoginLogin = (Button) this.findViewById(R.id.btnLoginLogin);
		this.btnLoginLogin.setOnClickListener(new ClickEvent());

		this.btnLoginClose = (Button) this.findViewById(R.id.btnLoginClose);
		this.btnLoginClose.setOnClickListener(new ClickEvent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	class ClickEvent implements OnClickListener {
		public void onClick(View v) {
			if (v == btnLoginLogin) {
				doBtnLoginClick(v);
			} else if (v == btnLoginClose) {
				doBtnCloseClick(v);
			}
		}

	}
	
	private void doBtnLoginClick(View v) {

		if ("admin".equals(this.txtLoginUsername.getText().toString())
				&& "123".equals(this.txtLoginPassword.getText().toString())) {

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString("username", this.txtLoginUsername.getText()
					.toString());
			intent.putExtra("loginInfo", bundle);

			startActivity(intent);

		} else {
			Dialog.alert(this, this.getString(R.string.loginFail));
		}

	}
	
	private void doBtnCloseClick(View v) {
		ExitApplication.getInstance().exit();
	}
}

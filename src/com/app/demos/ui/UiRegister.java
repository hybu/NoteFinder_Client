package com.app.demos.ui;

import java.util.HashMap;

//import android.content.Context;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
import android.widget.EditText;

import com.app.demos.R;
import com.app.demos.base.BaseAuth;
import com.app.demos.base.BaseMessage;
//import com.app.demos.base.BaseService;
import com.app.demos.base.BaseUi;
import com.app.demos.base.C;
//import com.app.demos.model.Customer;
//import com.app.demos.service.NoticeService;

public class UiRegister extends BaseUi {

	private EditText mEditName;
	private EditText mEditPass;
	//private SharedPreferences settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// check if login
		if (BaseAuth.isLogin()) {
			this.forward(UiIndex.class);
		}
		
		// set view after check login
		setContentView(R.layout.ui_register);
		
		// remember password
		mEditName = (EditText) this.findViewById(R.id.app_register_edit_name);
		mEditPass = (EditText) this.findViewById(R.id.app_register_edit_pass);
				
		// login submit
		OnClickListener mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.app_register_btn_submit : 
						doTaskRegister();
						break;
				}
			}
		};
		findViewById(R.id.app_register_btn_submit).setOnClickListener(mOnClickListener);
	}
	
	private void doTaskRegister() {
		app.setLong(System.currentTimeMillis());
		if (mEditName.length() > 0 && mEditPass.length() > 0) {
			HashMap<String, String> urlParams = new HashMap<String, String>();
			urlParams.put("name", mEditName.getText().toString());
			urlParams.put("pass", mEditPass.getText().toString());
			urlParams.put("sign", "暂无签名");
			urlParams.put("face", "1");
			try {
				this.doTaskAsync(C.task.register, C.api.register, urlParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// async task callback methods
	
	@Override
	public void onTaskComplete(int taskId, BaseMessage message) {
		super.onTaskComplete(taskId, message);
		switch (taskId) {
			case C.task.register:
				/*Customer customer = null;
				// login logic
				try {
					customer = (Customer) message.getResult("Customer");
					// login success
					if (customer.getName() != null) {
						BaseAuth.setCustomer(customer);
						BaseAuth.setLogin(true);
					// login fail
					} else {
						BaseAuth.setCustomer(customer); // set sid
						BaseAuth.setLogin(false);
						toast(this.getString(R.string.msg_loginfail));
					}
				} catch (Exception e) {
					e.printStackTrace();
					toast(e.getMessage());
				}*/
				// login complete
				
				int code = Integer.decode(message.getCode());
				
				long startTime = app.getLong();
				long registerTime = System.currentTimeMillis() - startTime;
				Log.w("RegisterTime", Long.toString(registerTime));
				// turn to index
				
				if (code == 10000) {
					toast("注册成功！");
					forward(UiLogin.class);
				} else {
					toast(message.getMessage());
				}
				break;
		}
	}
	
	@Override
	public void onNetworkError (int taskId) {
		super.onNetworkError(taskId);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// other methods
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//doFinish();
			forward(UiLogin.class);
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
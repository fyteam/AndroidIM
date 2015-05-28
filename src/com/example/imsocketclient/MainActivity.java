package com.example.imsocketclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UTFDataFormatException;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private EditText et;

	private TextView tv;

	private Button bt;

	private Socket socket = null;

	private BufferedReader bufferedReader = null;

	private BufferedWriter bufferedWriter = null;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String message = bundle.getString("message");
			if (msg.what == 1) {
				System.out.println(message);
				tv.setText("你要显示的内容: " + message);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		setContentView(R.layout.activity_main);

		et = (EditText) findViewById(R.id.input);
		tv = (TextView) findViewById(R.id.show);
		bt = (Button) findViewById(R.id.bt);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String inputContent = et.getText().toString();
				if (!inputContent.trim().equals("") && inputContent != null) {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.write(inputContent + '\n');
							bufferedWriter.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		// 初始化socket连接
		new Thread(new Runnable() {

			@Override
			public void run() {
				initSocket();
			}
		}).start();
	}

	public void initSocket() {
		try {
			socket = new Socket("10.0.2.2", 5222);
			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			startServerReplyListener(bufferedReader);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startServerReplyListener(final BufferedReader reader) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String response;
					while ((response = reader.readLine()) != null) {
						Message message = Message.obtain();
						message.what = 1;
						Bundle bundleData = new Bundle();
						bundleData.putString("message", response);
						message.setData(bundleData);
						handler.sendMessage(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		try {
			bufferedReader.close();
			bufferedWriter.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

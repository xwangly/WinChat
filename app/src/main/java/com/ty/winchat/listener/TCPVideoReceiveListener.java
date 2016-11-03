package com.ty.winchat.listener;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ty.winchat.listener.inter.OnBitmapLoaded;
import com.ty.winchat.util.Constant;
import com.ty.winchat.util.Util;

/**
 * 视屏接收器
 * @author wj
 * @creation 2013-5-16
 */
public class TCPVideoReceiveListener extends TCPListener{
	public static final int THREAD_COUNT=80;//线程数
	
	private int port=Constant.VIDEO_PORT;
	//用来加载图片
	private ExecutorService executors=Executors.newFixedThreadPool(THREAD_COUNT);
	
	private OnBitmapLoaded bitmapLoaded;
	
	boolean isReceived;//刚进来默认是正在接收数据的
	
	private static TCPVideoReceiveListener instance;
	
	private TCPVideoReceiveListener(){}
	
	public static TCPVideoReceiveListener getInstance(){
		return instance==null?instance=new TCPVideoReceiveListener():instance;
	}
	
	@Override
	void init() {
		setPort(port);
	}
	
	public void onReceiveData(final Socket socket) throws IOException{
		InputStream inputStream = socket.getInputStream();
		byte[] buffer = new byte[4];
		int len = inputStream.read(buffer, 0, 4);
		if (len == 4) {
			int bodyLen = Util.byte2Int(buffer, 0);
			if (bodyLen > 0) {
				byte[] body = new byte[bodyLen];
				int readBody = inputStream.read(body, 0, bodyLen);
				if (readBody == bodyLen) {

					Bitmap bitmap = BitmapFactory.decodeByteArray(body, 0, bodyLen);
					bitmapLoaded.onBitmapLoaded(bitmap);
				}
			}
		}
//		connectionReceive(socket);
	}
	
	private void connectionReceive(final Socket socket){
		executors.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Bitmap bitmap = BitmapFactory.decodeStream(socket.getInputStream());
					System.out.println("Receive bitmap from endpoint");
					bitmapLoaded.onBitmapLoaded(bitmap);
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public OnBitmapLoaded getBitmapLoaded() {
		return bitmapLoaded;
	}

	public void setBitmapLoaded(OnBitmapLoaded bitmapLoaded) {
		this.bitmapLoaded = bitmapLoaded;
	}

	@Override
	public void noticeReceiveError(IOException e) {
		
	}


	@Override
	public void noticeSendFileError(IOException e) {
		
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		isReceived=false;
		executors.shutdownNow();
		instance=null;
	}


}

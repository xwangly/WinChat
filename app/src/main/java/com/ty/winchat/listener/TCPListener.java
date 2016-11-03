package com.ty.winchat.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public abstract class TCPListener extends Listener{
	
	private int port;
	private boolean go;
	//������������
	private  ServerSocket server;	
	//��ʶ�Ƿ���
	private boolean running;
	
	
	/**
	 * ��ʼ������
	 * ���ñ���·��
	 * �˿ڳ�ʼ��
	 */
	abstract void init();
		

	private void createServer() throws IOException{
		init();
		server=new ServerSocket(port);
		go=true;
		running=true;
		start();
	}
	
	
	@Override
	public void run() {
		Log.d("TCPListener", "����TCP������");
		try {
			Socket socket = server.accept();
			while(go){
                try {
                    onReceiveData(socket);
    //				socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    noticeReceiveError(e);
                }
            }
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		running=false;
		try {
				if(server!=null)
					server.close();
				server=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public abstract void onReceiveData(Socket socket) throws IOException;
	/**֪ͨ�û������ļ�����*/
	public abstract void noticeReceiveError(IOException e);
	/**֪ͨ�û������ļ�����*/
	public abstract void noticeSendFileError(IOException e);
	
	
	
	
	
	@Override
	public void open() throws IOException {
		createServer();
		setPriority(MAX_PRIORITY);
	}

	@Override
	public void close() throws IOException {
		go=false;
		running=false;
		interrupt();
		if(server!=null)  server.close();
		server=null;
	}

	public boolean isRunning() {
		return running;
	}


	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}

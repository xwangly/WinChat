package com.ty.winchat.listener;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ty.winchat.WinChatApplication;
import com.ty.winchat.listener.inter.OnUDPReceiveMessage;
import com.ty.winchat.model.UDPMessage;
import com.ty.winchat.model.User;
import com.ty.winchat.ui.Set;
import com.ty.winchat.util.Constant;

/**
 * �ı���Ϣ�շ���
 *
 */
public class UDPMessageListener extends UDPListener{
	
	//�ı���Ϣ�����˿�
	private final int port=Constant.MESSAGE_PORT;
	private final int BUFFER_SIZE=1024*3;//3k�����ݻ�����
	private OnUDPReceiveMessage onReceiveMessage;
	//���浱ǰ�����û�����ֵΪ�û���ip
	 final Map<String,User> users;
	//�����û�������Ϣ��ÿ��ip���Ὺ��һ����Ϣ������������Ϣ
	 final Map<String, Queue<UDPMessage>> messages;
	
	private static UDPMessageListener instance;
	
	
	private UDPMessageListener(Map<String,User> users,Map<String, Queue<UDPMessage>> messages){
		this.users=users;
		this.messages=messages;
	}
	
	public static UDPMessageListener getInstance(Map<String,User> users,Map<String, Queue<UDPMessage>> messages){
		return instance==null?instance=new UDPMessageListener(users,messages):instance;
	}
	
	@Override
  void init() {
		setPort(port);
		setBufferSize(BUFFER_SIZE);
  }
	
  @Override
  public void onReceive(byte[] data, DatagramPacket packet) {
    try {
	    String temp=new String(data,0,packet.getLength(),Constant.ENCOD);//�õ����յ���Ϣ
	    UDPMessage msg = new UDPMessage(new JSONObject(temp));
//	    Log.d("====", "�յ���Ϣ��"+msg.toString());
	    String sourceIp=packet.getAddress().getHostAddress();//�Է�ip
	    int type=msg.getType();
	    switch (type) {
				case ADD_USER://����һ���û�
					User user=new User();
					user.setIp(sourceIp);
					user.setUserName(msg.getSenderName());
					user.setDeviceCode(msg.getDeviceCode());
					//������ͱ�������
					if(!WinChatApplication.mainInstance.getLocalIp().equals(user.getIp())){
						users.put(sourceIp, user);
						send(WinChatApplication.mainInstance.getMyUdpMessage("", LOGIN_SUCC).toString(), packet.getAddress());
					}
					break;
					
				case LOGIN_SUCC://�ڶԷ���½�ɹ��󷵻ص���֤��Ϣ
					user=new User();
					user.setIp(sourceIp);
					user.setUserName(msg.getSenderName());
					user.setDeviceCode(msg.getDeviceCode());
					users.put(sourceIp, user);
					break;
					
				case REMOVE_USER://ɾ���û�
					users.remove(sourceIp);
					break;
					
				case ASK_VIDEO:
				case REPLAY_VIDEO_ALLOW:
				case REPLAY_VIDEO_NOT_ALLOW:
				case REPLAY_SEND_FILE://�ظ��ļ���������
				case ASK_SEND_FILE://�յ��ļ���������
				case RECEIVE_MSG://���յ���Ϣ
					if(messages.containsKey(sourceIp)){
						messages.get(sourceIp).add(msg);//��������
					}else{
						Queue<UDPMessage> queue=new ConcurrentLinkedQueue<UDPMessage>();
						queue.add(msg);
						messages.put(sourceIp, queue);//����
					}
					break;
					
				case TO_ALL_MESSAGE://message to all
					if(messages.containsKey(Constant.ALL_ADDRESS)){
						messages.get(Constant.ALL_ADDRESS).add(msg);//��������
					}else{
						Queue<UDPMessage> queue=new ConcurrentLinkedQueue<UDPMessage>();
						queue.add(msg);
						messages.put(Constant.ALL_ADDRESS, queue);//����
					}
					break;
					
				case HEART_BEAT://���������
					send(WinChatApplication.mainInstance.getMyUdpMessage("", HEART_BEAT_REPLY).toString(), packet.getAddress());//�ظ�������
					user=users.get(sourceIp);
					if(user!=null){
						user.setHeartTime(System.currentTimeMillis()+"");
						Log.e("UDPMessageListener", "������������"+user.getHeartTime());
					}
					break;
					
				case HEART_BEAT_REPLY://���յ�������
					user=users.get(sourceIp);
					if(user!=null)
						user.setHeartTime(System.currentTimeMillis()+"");//���������������ʱ��
					break;
					
				case REQUIRE_ICON://����ͷ��
					File file=new File(WinChatApplication.iconPath+Set.iconName);
					if(file.exists()){
//						TCPFileListener listener=TCPFileListener.getInstance();
//						if(!listener.isRunning()){
//							try {
//								listener.open();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//						listener.sendFile(sourceIp, file, WinChatApplication.iconPath,WinChatApplication.mainInstance.getDeviceCode());
					}
					break;
			}
	    if(onReceiveMessage!=null)
	    	onReceiveMessage.onReceive(type);
    } catch (UnsupportedEncodingException e) {
    	e.printStackTrace();
	}catch (JSONException e) {
		e.printStackTrace();
	}
  }


	@Override
  void noticeOffline() {
		try {
			send(WinChatApplication.mainInstance.getMyUdpMessage("", REMOVE_USER).toString(), InetAddress.getByName(Constant.ALL_ADDRESS));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
  }
	
	@Override
	public void noticeOnline(){
		try {
			send(WinChatApplication.mainInstance.getMyUdpMessage("", ADD_USER).toString(), InetAddress.getByName(Constant.ALL_ADDRESS));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����UDP���ݰ�
	 * @param msg  ��Ϣ
	 * @param destIp  Ŀ���ַ
	 * @param destPort  Ŀ��˿�
	 * @throws IOException 
	 */
	public void send(String msg, InetAddress destIp){
//		Log.d("====", "������Ϣ��"+msg);
		send(msg, destIp, Constant.MESSAGE_PORT);
	}

	public OnUDPReceiveMessage getOnReceiveMessage() {
  	return onReceiveMessage;
  }

	public void setOnReceiveMessage(OnUDPReceiveMessage onReceiveMessage) {
  	this.onReceiveMessage = onReceiveMessage;
  }
	
	@Override
	public void close() throws IOException {
		super.close();
		//���һ��Ҫ�ÿգ���Ȼ�����already start��bug,��Ϊinstance��static�ģ������˳��󣬵�ǰdvm���ڣ����ǻᱣ�ֶ�ԭ�б���������
		instance=null;
		if(users!=null)	users.clear();
		if(messages!=null)	messages.clear();
	}

	@Override
	void sendMsgFailure() {
		 if(onReceiveMessage!=null)
			 onReceiveMessage.sendFailure();
	}

}

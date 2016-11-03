package com.ty.winchat.listener;

import java.io.IOException;

public abstract class Listener extends Thread{
	
	public static final int NONE=0;//û�ж���
	
	public static final int ADD_USER=1;//�����û�
	public static final int LOGIN_SUCC=2;//�����û��ɹ�
	public static final int REMOVE_USER=3;//ɾ���û�
	
	public static final int RECEIVE_MSG=4;//������Ϣ
	public static final int RECEIVE_FILE=5;//�����ļ�
	
	public static final int HEART_BEAT=6;//����������
	public static final int HEART_BEAT_REPLY=7;//�������ظ�
	
	public static final int ASK_SEND_FILE=8;//�������ļ�
	public static final int REPLAY_SEND_FILE=9;//�ظ��������ļ�
	
	public static final int REQUIRE_ICON=10;//������ͷ��
	
	public static final int ASK_VIDEO=11;//������������
	public static final int REPLAY_VIDEO_ALLOW=12;//������������
	public static final int REPLAY_VIDEO_NOT_ALLOW=13;//������������
	
	public static final int TO_ALL_MESSAGE=14;//��������Ⱥ����Ϣ
	
	/**�򿪼�����*/
	abstract void open() throws IOException;
	/**�رռ�����*/
	abstract void close() throws IOException;
}

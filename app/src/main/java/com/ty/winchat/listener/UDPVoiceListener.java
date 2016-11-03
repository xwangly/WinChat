package com.ty.winchat.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.ty.winchat.util.Constant;
/**
 * �����շ���
 * @author wj
 * @creation 2013-5-16
 */
public class UDPVoiceListener extends UDPListener{

		//�ı���Ϣ�����˿�
		private final int port=Constant.VOICE_PORT;
		private final int BUFFER_SIZE=1024*6;//6k�����ݻ�����
		
		static final int frequency = 44100;  
	    int recBufSize,playBufSize;  
	    private AudioRecord audioRecord;  
	    private AudioTrack audioTrack;  
	    private InetAddress address; 
	    
	    private static UDPVoiceListener instance;
	    
	    private boolean go=true;
	    
	    public static UDPVoiceListener getInstance(InetAddress address){
	    	if(instance==null){
	    		instance=new UDPVoiceListener(address);
	    	}
	    	return instance;
	    }
	     
	    private UDPVoiceListener(InetAddress address){
	    	this.address=address;
	    }
	    

	@Override
	    void init() {
		setPort(port);
		setBufferSize(BUFFER_SIZE);
		
		recBufSize = AudioRecord.getMinBufferSize(frequency, AudioFormat. CHANNEL_CONFIGURATION_MONO,  AudioFormat.ENCODING_PCM_16BIT);  
        playBufSize=AudioTrack.getMinBufferSize(frequency,   AudioFormat. CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);  
        
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequency,  AudioFormat. CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);  
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,   AudioFormat. CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);  
        
	    audioTrack.setStereoVolume(0.8f, 0.8f);//���õ�ǰ������С  
	    
	    audioRecord.startRecording();//��ʼ¼��  
	    audioTrack.play();//��ʼ����  
      
	      new Thread(new Runnable() {
			@Override
			public void run() {
				while(go){
					send();
				}
			}
		}).start();
	}
	
	/**
	 * ����������
	 */
	void send() {
		byte[]  buffer = new byte[recBufSize]; 
        //��MIC�������ݵ�������  
        int bufferReadResult = audioRecord.read(buffer, 0,   recBufSize);  
        //д�����ݼ�����,��������
        if(bufferReadResult>0)
		 send(buffer,bufferReadResult, address, port);
	}
	
	
	@Override
	public void onReceive(byte[] data, DatagramPacket packet) {
            audioTrack.write(data, 0, packet.getLength());  
	}

	@Override
	void noticeOffline() throws IOException {
		
	}

	@Override
	void noticeOnline() throws IOException {
		
	}

	@Override
	void sendMsgFailure() {
		
	}
	
	@Override
	public void open() throws IOException {
		super.open();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		go=false;
		if(audioTrack!=null)
		 audioTrack.stop();  
		if(audioRecord!=null)
         audioRecord.stop();  
         instance=null;
	}

}

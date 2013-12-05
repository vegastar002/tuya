package com.zhangke.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


public class CommenUtil {

	public ArrayList<String> realID = new ArrayList<String>();
	public ArrayList<String> cardID = new ArrayList<String>();
	public ArrayList<String> cardPW = new ArrayList<String>();
	public ArrayList<Integer> cardAM = new ArrayList<Integer>();//���

	public void readTxtFile(String filePath) {
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = "";
				while ((lineTxt = bufferedReader.readLine()) != null) {
//					System.out.println(lineTxt);
					String[] para = lineTxt.split(",");
					Float flout = new Float( para[3] );
					
					realID.add(para[0]);
					cardID.add(para[1]);
					cardPW.add(para[2]);
					cardAM.add(flout.intValue());
					
				}
				read.close();
			} else {
				System.out.println("�Ҳ���ָ�����ļ�");
			}
		} catch (Exception e) {
			System.out.println("��ȡ�ļ����ݳ���");
			e.printStackTrace();
		}

	}
	
	
	//������д�����ݿ���
	public String updateCardInfoToDB() {
		String rss= "";
		Connection conn = null;

		try {
			Class.forName(FileUpload.classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(FileUpload.tuyaURL, FileUpload.uid, FileUpload.pwd);
			
			for (int i = 0; i < cardID.size(); i++) {
				PreparedStatement psce = conn.prepareStatement("insert into cardInfo(realID,cardID,cardPW,cardAM,used) values(?,?,?,?,?)");
				psce.setString(1, realID.get(i));
				psce.setString(2, cardID.get(i));
				psce.setString(3, cardPW.get(i));
				psce.setInt(4, cardAM.get(i));
				psce.setBoolean(5, false);
				psce.executeUpdate();
			}
			
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print("������");
		}

		System.out.print("���");
		return "none";
	}
	
	
	//�ӿ���ȡ��һ������
	public String getOneCardID(String amount, String phoneNum) {//�����Ǯ�Ŀ�
		String rss= "";
		Connection conn = null;

		if ( Integer.valueOf(amount) == 50 ){
			amount = "60";
		}
		if ( Integer.valueOf(amount) == 100 ){
			amount = "130";
		}
		
		try {
			Class.forName(FileUpload.classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(FileUpload.tuyaURL, FileUpload.uid, FileUpload.pwd);
			
			
			PreparedStatement queryPI = conn.prepareStatement("SELECT * FROM cardInfo WHERE cardAM=? and used=0");
			queryPI.setInt(1, Integer.valueOf(amount));
			ResultSet queryRS = queryPI.executeQuery();
			if ( queryRS.next() ){
				String cardID = queryRS.getString("cardID");
				String cardPW = queryRS.getString("cardPW");
				int mid = queryRS.getInt("mid");
				rss = "http://da.bigo.me/interface/pay/mobile/"+ phoneNum +"/cardid/"+cardID+ "/cardpwd/"+ cardPW;
				
				//������������Ϊ����״̬
				PreparedStatement updateContent = conn.prepareStatement("UPDATE cardInfo SET used=1 WHERE mid=?");
				updateContent.setInt(1, mid);
				updateContent.execute();
			}
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print("������");
		}

		System.out.print("���");
		return "none";
	}
	
	
	//�õ�һ��һ��Ǯ�Ŀ���
	public String getKaMiOne() {
		String rss= "";
		Connection conn = null;

		try {
			Class.forName(FileUpload.classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(FileUpload.tuyaURL, FileUpload.uid, FileUpload.pwd);
			
			PreparedStatement queryPI = conn.prepareStatement("SELECT * FROM cardInfo WHERE cardAM=? and used=0");
			queryPI.setInt(1, 1);
			ResultSet queryRS = queryPI.executeQuery();
			if ( queryRS.next() ){
				String cardID = queryRS.getString("cardID");
				String cardPW = queryRS.getString("cardPW");
				int mid = queryRS.getInt("mid");
				rss = cardID+","+cardPW;
				
				//������������Ϊ����״̬
				PreparedStatement updateContent = conn.prepareStatement("UPDATE cardInfo SET used=1 WHERE mid=?");
				updateContent.setInt(1, mid);
				updateContent.execute();
			}
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print("������");
		}

		System.out.print("���");
		return "none";
	}
	
	
	
	//ͳ���û�APP��װ���
	public String statistics_spy(String lables) {
		String rss= "";
		Connection conn = null;
		
		try {
			Class.forName(FileUpload.classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(FileUpload.tuyaURL, FileUpload.uid, FileUpload.pwd);
			
			
			String[] appLables = lables.split(",");
			for (int i = 0; i < appLables.length; i++) {
				String theLable = appLables[i];
				
				PreparedStatement queryPI = conn.prepareStatement("SELECT * FROM spy WHERE appLable=?");
				queryPI.setString(1, theLable);
				ResultSet queryRS = queryPI.executeQuery();
				if ( queryRS.next() ){
					int installNum = queryRS.getInt("installNum") + 1;
					String appLable = queryRS.getString("appLable");
					
					//�Լ�1
					PreparedStatement updateContent = conn.prepareStatement("UPDATE spy SET installNum=? WHERE appLable=?");
					updateContent.setInt(1, installNum);
					updateContent.setString(2, appLable);
					updateContent.execute();
					
				}else {
					
					PreparedStatement psce = conn.prepareStatement("insert into spy(appLable,installNum) values(?,?)");
					psce.setString(1, theLable);
					psce.setInt(2, 1);
					psce.executeUpdate();
					
				}
				
				
			}
			
			rss = "ok";
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print("������");
		}

		System.out.print("���");
		return "none";
	}
	
	
	
	
	
	//�ҵ���ߵİ汾��
	public int getTheLastVersion() {
		int lastversion = 0;
		Connection conn = null;

		try {
			Class.forName(FileUpload.classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(FileUpload.tuyaURL, FileUpload.uid, FileUpload.pwd);
			
			
			PreparedStatement queryPI = conn.prepareStatement("SELECT * FROM lastVersion");
			ResultSet queryRS = queryPI.executeQuery();
			if ( queryRS.next() ){
				lastversion = queryRS.getInt("lastversion");
			}
			
			return lastversion;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.print("������");
		}

		return lastversion;
	}
	
	
	
	
	
}

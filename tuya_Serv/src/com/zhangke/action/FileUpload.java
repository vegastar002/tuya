package com.zhangke.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

/***
 * 文件上传例子   resource code encoding is utf-8
 * <br>主要为了android客户端实现功能   代码写的乱   请大家见谅
 *
 */
public class FileUpload extends ActionSupport {

	private String savePath;
	/**这里的名字和html的名字必须对称*/
	private File img;
	/**要上传的文件类型*/
	private String imgContentType;                                       
	/**文件的名称*/
	private String imgFileName;
	
	private String orderId, tihao, teacher, answer;
	/**
	 * 指定的上传类型   zip 和   图片格式的文件
	 */
	private static final String[] types = { "application/octet-stream",
			"ZIP", "image/pjpeg","image/x-png" };  //"application/octet-stream; charset=utf-8",

	
	public static String tuyaURL = "jdbc:sqlserver://121.199.3.19:1433;DatabaseName=tuya;useunicode=true;characterEncoding=UTF-8";
	public static String url = "jdbc:sqlserver://121.199.3.19:1433;DatabaseName=openfire;useunicode=true;characterEncoding=UTF-8";
	public static String eFlowUrl = "jdbc:sqlserver://121.199.3.19:1433;DatabaseName=eFlowIM;useunicode=true;characterEncoding=UTF-8";
	public static String classforname = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static String uid = "xiajinsong";
	public static String pwd = "miaomiao_0011";
	
	
	/***
	 * 判断文件的类型是否为指定的文件类型
	 * @return
	 */
	public boolean filterType() {
		boolean isFileType = false;
		String fileType = getImgContentType();
//		System.out.println(fileType);
		for (String type : types) {
			if (type.equals(fileType)) {
				isFileType = true;
				break;
			}
		}
		return true;
	}

	public String getSavePath() {
		String realPath = ServletActionContext.getRequest().getRealPath(savePath);
//		System.out.println("savePaht -- " + realPath);
		if ( !new File(savePath).exists() ){
			new File(savePath).mkdir();
		}
		
		return savePath;
	}

	public File getImg() {
		return img;
	}

	public String getImgFileName() {
		return imgFileName;
	}

	public void setSavePath(String value) {
		this.savePath = value;
	}

	public void setImgFileName(String imgFileName) {
		this.imgFileName = imgFileName;
	}

	public void setImg(File img) {
		this.img = img;
	}

	public String getImgContentType() {
		return imgContentType;
	}

	public void setImgContentType(String imgContentType) {
		this.imgContentType = imgContentType;
	}

	/**
	 * 取得文件夹大小
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public long getFileSize(File f) throws Exception {
		return f.length();
	}

	public String FormetFileSize(long fileS) {// 转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * 上传文件操作
	 * 
	 * @return
	 * @throws Exception
	 */
	public String upload() throws Exception {
		
		String ct  =  ServletActionContext.getRequest().getHeader("Content-Type");
//		System.out.println("Content-Type="+ct);
		String result = "unknow error";
//		System.out.println("orderId="+getOrderId());
		PrintWriter out = ServletActionContext.getResponse().getWriter();
		if (!filterType()) {
//			System.out.println("文件类型不正确");
			ServletActionContext.getRequest().setAttribute("typeError",	"您要上传的文件类型不正确");
			result = "error:" + getImgContentType() + " type not upload file type";
		} else {
//			System.out.println("当前文件大小为："	+ FormetFileSize(getFileSize(getImg())));
			FileOutputStream fos = null;
			FileInputStream fis = null;
			try {
				// 保存文件那一个路径
				fos = new FileOutputStream(getSavePath() + "\\"	+ getImgFileName());
				fis = new FileInputStream(getImg());
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = fis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				//result = "上传成功!";
				result = "Upload File Success !";
			} catch (Exception e) {
				result = "Upload File Failed ! ";
				e.printStackTrace();
			} finally {
				fos.close();
				fis.close();
			}
		}
		out.print(result);
		return null;
	}
	
	/*
	 * 有信录入电话号码接口
	*/
	public void updatePersonalInfo() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("UTF-8");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("UTF-8");
		
		String myres = "";
		PrintWriter out = response.getWriter();
		String commString = request.getParameter("command");
		if ( "telnumber".equals(commString.split(",")[0]) ){
			//查电话号码
			myres = selectRecord(commString.split(",")[1]);
			
		}else if ( "updatetel".equals(commString.split(",")[0]) ) {
			//改电码号码
			myres = updateRecord(commString.split(",")[1], commString.split(",")[2]);
			
		}else if ( "password".equals(commString.split(",")[0]) ) {
			//查密码，查不到就新建一条
			myres = passwordRecord(commString.split(",")[1]);
			
		}else if ( "changepw".equals(commString.split(",")[0]) ) {
			//改密码
			myres = changePWRecord(commString.split(",")[1], commString.split(",")[2]);
			
		}else if ( "multitel".equals(commString.split(",")[0]) ) {
			//查多人电话号码
			String msgContent = commString.split(",")[1];
			myres = multi_telnum(commString.split(",")[2]);
			
			if ( "".equals(myres) )
				return;
			
			try {
				HttpClient client = new HttpClient();
				PostMethod post = new PostMethod("http://gbk.sms.webchinese.cn");
				post.addRequestHeader("Content-Type",
						"application/x-www-form-urlencoded;charset=gbk");// 在头文件中设置转码
				NameValuePair[] data = { new NameValuePair("Uid", "hust_mse"),
						new NameValuePair("Key", "86b3aa069a2494a7f5xz"),
						new NameValuePair("smsMob", myres),
						new NameValuePair("smsText", msgContent) };
				post.setRequestBody(data);

				client.executeMethod(post);
//				Header[] headers = post.getResponseHeaders();
//				int statusCode = post.getStatusCode();
//				System.out.println("statusCode:" + statusCode);
//				for (Header h : headers) {
//					System.out.println(h.toString());
//				}
				String result = new String(post.getResponseBodyAsString().getBytes("gbk"));
				System.out.println(result);

				post.releaseConnection();
				myres = result;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		
		out.print(myres);
		out.flush();
	}
	
	public String multi_telnum(String names) {
		String rss= "";
		Connection conn = null;

		try {
			String[] only = names.split(":");
			
			Class.forName(classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(url, uid, pwd);
			
			for (int i = 0; i < only.length; i++) {
				PreparedStatement c2 = conn.prepareStatement("SELECT * FROM personInfo WHERE name=?");
				c2.setString(1, only[i]);
				ResultSet c2RS = c2.executeQuery();
				if ( c2RS.next() ){
					if ( !"".equals(c2RS.getString("tel")) ) {
						rss += c2RS.getString("tel")+ ",";
					}
					
				}
			}
			
			System.out.print(rss);
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return "none";
	}
	
	
	public String changePWRecord(String username, String newPWord) {
		String rss= "";
		Connection conn = null;

		try {
			Class.forName(classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(url, uid, pwd);
			
			PreparedStatement c2 = conn.prepareStatement("UPDATE personInfo SET password=? WHERE name=?");
			c2.setString(1, newPWord);
			c2.setString(2, username);
			c2.execute();
			rss = "ok";
			
			return rss;
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			rss = "no";
		}

		return rss;
	}
	
	
	
	public String passwordRecord(String username) {
		String rss= "";
		Connection conn = null;

		try {
			Class.forName(classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(url, uid, pwd);
			
			PreparedStatement c2 = conn.prepareStatement("SELECT * FROM personInfo WHERE name=?");
			c2.setString(1, username);
			ResultSet c2RS = c2.executeQuery();
			if ( c2RS.next() ){
				rss = c2RS.getString("password");
			}else {
				PreparedStatement psce = conn.prepareStatement("insert into personInfo(name,tel,password) values(?,?,?)");
				psce.setString(1, username);
				psce.setString(2, "");
				psce.setString(3, "123456");
				psce.executeUpdate();
				rss = "123456";
			}
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return "none";
	}
	
	
	public String selectRecord(String chen) {
		String rss= "";
		Connection conn = null;
		Statement stmt;
		ResultSet rs = null;

		try {
			Class.forName(classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(url, uid, pwd);
			stmt = conn.createStatement();
			
			if ( chen == null || "".equals(chen) ){
				rs = stmt.executeQuery("SELECT name, tel FROM personInfo");
			}else {
				rs = stmt.executeQuery("SELECT name, tel FROM personInfo WHERE name = '"+ chen + "'");
			}
			
			if (rs.next()) {
				rss = rs.getString("tel");
			}else {
				rss = "";
			}
			
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return "none";
	}
	
	public String updateRecord(String name, String tel) {
		String rss= "";
		Connection conn = null;

		try {
			Class.forName(classforname);
			if (conn == null || conn.isClosed())
				conn = DriverManager.getConnection(url, uid, pwd);
			
			
			PreparedStatement queryPI = conn.prepareStatement("SELECT * FROM personInfo WHERE name=?");
			queryPI.setString(1, name);
			ResultSet queryRS = queryPI.executeQuery();
			if ( queryRS.next() ){
				//如果有这个人就update
				PreparedStatement updateContent = conn.prepareStatement("UPDATE personInfo SET tel=? WHERE name=?");
				updateContent.setString(1, tel);
				updateContent.setString(2, name);
				updateContent.execute();
				rss = "updated";
			}else {
				PreparedStatement psce = conn.prepareStatement("insert into personInfo(name,tel) values(?,?)");
				psce.setString(1, name);
				psce.setString(2, tel);
				psce.executeUpdate();
				rss = "insert";
			}
			
			return rss;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return "none";
	}
	
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getTihao() {
		return tihao;
	}
	
	public void setTihao(String tihao) {
		this.tihao = tihao;
	}
	
	public String getTeacher() {
		return teacher;
	}
	
	public void setTeacher(String t) {
		this.teacher = t;
	}
	
	public String getAnswer() {
		return answer;
	}
	
	public void setAnswer(String a) {
		this.answer = a;
	}
	
	
	public String retrieveInputStream(HttpEntity httpEntity) {
		int length = (int) httpEntity.getContentLength();
		if (length < 0)
			length = 10000;
		StringBuffer stringBuffer = new StringBuffer(length);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(), HTTP.UTF_8);
			char buffer[] = new char[length];
			int count;
			while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
				stringBuffer.append(buffer, 0, count);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuffer.toString();
	}
	
	
	
	//收到钱后要给它充值
	public void payForUsers() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("GBK");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("GBK");
		
		String myres = "";
		PrintWriter out = response.getWriter();
		String command = request.getParameter("command");
		
		CommenUtil cuUtil = new CommenUtil();
		String host = cuUtil.getOneCardID(command.split(",")[0], command.split(",")[1]);
		
		HttpPost httpRequest = new HttpPost(host);
		List<BasicNameValuePair> Vaparams = new ArrayList<BasicNameValuePair>();
		
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(Vaparams, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				myres = retrieveInputStream(httpResponse.getEntity());
				System.out.println("返回:" + myres);
				
				if ( !myres.contains("ERR") ){
					myres = "success";
				}else {
					myres = "failure";
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		out.print(myres);
		out.flush();
	}
	
	
	
	//获取最高版本号
	public void theLastVersion() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("GBK");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("GBK");
		
		PrintWriter out = response.getWriter();
		
		CommenUtil cuUtil = new CommenUtil();
		int lastVersion = cuUtil.getTheLastVersion();
		
		out.print(lastVersion);
		out.flush();
	}
	
	
	//统计用户APP安装情况
	public void statistics() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("GBK");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("GBK");
		
		PrintWriter out = response.getWriter();
		String lables = request.getParameter("lables");
		
		CommenUtil cuUtil = new CommenUtil();
		String result = cuUtil.statistics_spy(lables);
		
		out.print(result);
		out.flush();
	}
	
	
	//得到一张一块钱的卡密
	public void get1yuan() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("GBK");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("GBK");
		
		PrintWriter out = response.getWriter();
		
		CommenUtil cuUtil = new CommenUtil();
		String kami = cuUtil.getKaMiOne();
		
		out.print(kami);
		out.flush();
	}
	
	//将卡密批量入库
	public void saveCardToDB() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setCharacterEncoding("GBK");
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("GBK");
		
		CommenUtil cuUtil = new CommenUtil();
		cuUtil.readTxtFile("d:\\2013-12-041386137918.txt");
		cuUtil.updateCardInfoToDB();
		
		String myres = "卡密已入库";
		PrintWriter out = response.getWriter();
		
		out.print(myres);
		out.flush();
	}
	
	
}


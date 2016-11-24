package cn;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ALiAPI {
	public static void testUrlRes(String lat,String lng) throws IOException{  
		//type 001 (100�����·��010����POI��001������ַ��111����ͬʱ��ʾǰ����)  
		String path="http://gc.ditu.aliyun.com/regeocoding?l="+lat+","+lng+"&type=100";  
		//����ֱ�Ӽ���url����  
		URL url=new URL(path);  
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();  
		conn.setRequestMethod("GET");  
		conn.setConnectTimeout(5000);  
		if(conn.getResponseCode()==200){                //200��ʾ����ɹ�  
			InputStream is=conn.getInputStream();       //������������ʽ����  
			//��������ת�����ַ���  
			ByteArrayOutputStream baos=new ByteArrayOutputStream();  
			byte [] buffer=new byte[1024];  
			int len=0;  
			while((len=is.read(buffer))!=-1){  
				baos.write(buffer, 0, len);  
			}  

			if(baos.size() < 1){  
				System.out.println("���������쳣.");  
				return;  
			}  
			//�ó��������귴����Ϣ  
			String jsonString=baos.toString();  
			System.out.println(jsonString);  

			baos.close();  
			is.close();  
			//ת����json���ݴ���  
			//{"queryLocation":[39.938133,116.395739],"addrList":[{"type":"doorPlate","status":1,"name":"�ذ������������ͬ1��","admCode":"110102","admName":"������,������,������,","addr":"","nearestPoint":[116.39546,39.93850],"distance":45.804}]}  

			JSONObject jsonObject = JSONObject.parseObject(jsonString);  
			String addrList =  jsonObject.getString("addrList");  
			System.out.println(addrList);  //��ַ��Ϣ  

			JSONArray jsonarry = JSONArray.parseArray(addrList);  
			for(int i = 0;i<jsonarry.size();i++){  
				JSONObject jsonObject2 = jsonarry.getJSONObject(i);  
				String roadName =  jsonObject2.getString("name"); //·�����������������Ҫ�ģ�  
				System.out.println(roadName);  
			}  
		}  
	}
	public static void main(String[] args) throws IOException {
		testUrlRes("30.111111", "120.111111");
	}
}

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
		//type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)  
		String path="http://gc.ditu.aliyun.com/regeocoding?l="+lat+","+lng+"&type=100";  
		//参数直接加载url后面  
		URL url=new URL(path);  
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();  
		conn.setRequestMethod("GET");  
		conn.setConnectTimeout(5000);  
		if(conn.getResponseCode()==200){                //200表示请求成功  
			InputStream is=conn.getInputStream();       //以输入流的形式返回  
			//将输入流转换成字符串  
			ByteArrayOutputStream baos=new ByteArrayOutputStream();  
			byte [] buffer=new byte[1024];  
			int len=0;  
			while((len=is.read(buffer))!=-1){  
				baos.write(buffer, 0, len);  
			}  

			if(baos.size() < 1){  
				System.out.println("坐标请求异常.");  
				return;  
			}  
			//得出整个坐标反馈信息  
			String jsonString=baos.toString();  
			System.out.println(jsonString);  

			baos.close();  
			is.close();  
			//转换成json数据处理  
			//{"queryLocation":[39.938133,116.395739],"addrList":[{"type":"doorPlate","status":1,"name":"地安门外大街万年胡同1号","admCode":"110102","admName":"北京市,北京市,西城区,","addr":"","nearestPoint":[116.39546,39.93850],"distance":45.804}]}  

			JSONObject jsonObject = JSONObject.parseObject(jsonString);  
			String addrList =  jsonObject.getString("addrList");  
			System.out.println(addrList);  //地址信息  

			JSONArray jsonarry = JSONArray.parseArray(addrList);  
			for(int i = 0;i<jsonarry.size();i++){  
				JSONObject jsonObject2 = jsonarry.getJSONObject(i);  
				String roadName =  jsonObject2.getString("name"); //路名（这才是我最终想要的）  
				System.out.println(roadName);  
			}  
		}  
	}
	public static void main(String[] args) throws IOException {
		testUrlRes("30.111111", "120.111111");
	}
}

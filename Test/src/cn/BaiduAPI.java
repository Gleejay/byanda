package cn;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.security.NoSuchAlgorithmException;



public class BaiduAPI {
	//private static String ak = "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG";

	public static Map<String, String> testPost(String x, String y) throws Exception {
		
		String ak = "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG";
        String sk = "0GpqAHtY6VY9ZSVCs3mymea8WYFULjab";
        // 计算sn跟参数对出现顺序有关，所以用LinkedHashMap保存<key,value>，此方法适用于get请求，如果是为发送post请求的url生成签名，请保证参数对按照key的字母顺序依次放入Map。以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
        Map<String, String> paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("ak", ak);
       // paramsMap.put("callback", "renderReverse");
        paramsMap.put("location", x+","+y);
        paramsMap.put("output", "json");
        //paramsMap.put("pois", "1");
		
        String paramsStr = toQueryString(paramsMap);
        
        String wholeStr = new String("/geocoder/v2/?" + paramsStr + sk);
//        System.out.println(wholeStr);
        
        
        String tempStr = URLEncoder.encode(wholeStr, "UTF-8");
        
 //       System.out.println(MD5(tempStr));
        
        
        
		
//		URL url = new URL("http://api.map.baidu.com/geocoder/v2/?ak=" + ak  +         //+ "=您的密钥"
//				"&callback=renderReverse&location=" + x
//				+ "," + y + "&output=json&pois=1&sn="+ MD5(tempStr));

		URL url = new URL("http://api.map.baidu.com/geocoder/v2/?ak=" + ak  +         //+ "=您的密钥"
				"&location=" + x + "," + y + "&output=json&sn="+ MD5(tempStr));
		//System.out.println("url: "+ url);
		
		String path="http://gc.ditu.aliyun.com/regeocoding?l=22.562048,113.939167&type=100";
		
		HttpClient client = new DefaultHttpClient();
//        HttpGet httpget = new HttpGet("http://api.map.baidu.com/geocoder/v2/?ak=" + ak  +         //+ "=您的密钥"
//				"&location=" + x + "," + y + "&output=json&sn="+ MD5(tempStr));
		HttpGet httpget = new HttpGet(path);
        HttpResponse response = client.execute(httpget);
        InputStream is = response.getEntity().getContent();
        String result = inStream2String(is);
        // 打印响应内容
        System.out.println(result);
		
		
		
//		URLConnection connection = url.openConnection();
//		/**
//		 * 然后把连接设为输出模式。URLConnection通常作为输入来使用，比如下载一个Web页。
//		 * 通过把URLConnection设为输出，你可以把数据向你个Web页传送。下面是如何做：
//		 */
//		
//		
//		connection.setDoOutput(true);
//		OutputStreamWriter out = new OutputStreamWriter(connection
//				.getOutputStream(), "utf-8");
//		//	        remember to clean up
//		out.flush();
//		out.close();
//		//	        一旦发送成功，用以下方法就可以得到服务器的回应：
//		String res;
//		InputStream l_urlStream;
//		l_urlStream = connection.getInputStream();
//		BufferedReader in = new BufferedReader(new InputStreamReader(
//				l_urlStream,"UTF-8"));
//		StringBuilder sb = new StringBuilder("");
//		while ((res = in.readLine()) != null) {
//			sb.append(res.trim());
//		}
//		String str = sb.toString();
//		System.out.println(str);
//		Map<String,String> map = null;
//		if(str!=null && str.trim()!="") {
//			int addStart = str.indexOf("formatted_address\":");
//			int addEnd = str.indexOf("\",\"business");
//			if(addStart > 0 && addEnd > 0) {
//				String address = str.substring(addStart+20, addEnd);
//				map = new HashMap<String,String>();
//				map.put("address", address);
//				return map;		
//			}
//		}

		return null;

	}

	
	private static String inStream2String(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return new String(baos.toByteArray(), "UTF-8");
	}
	
	// 对Map内所有value作utf8编码，拼接返回结果
    public static String toQueryString(Map<?, ?> data)
                    throws UnsupportedEncodingException {
            StringBuffer queryString = new StringBuffer();
            for (Entry<?, ?> pair : data.entrySet()) {
                    queryString.append(pair.getKey() + "=");
                    queryString.append(URLEncoder.encode((String) pair.getValue(),
                                    "UTF-8") + "&");
            }
            if (queryString.length() > 0) {
                    queryString.deleteCharAt(queryString.length() - 1);
            }
            return queryString.toString();
    }

    // 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    public static String MD5(String md5) {
            try {
                    java.security.MessageDigest md = java.security.MessageDigest
                                    .getInstance("MD5");
                    byte[] array = md.digest(md5.getBytes());
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < array.length; ++i) {
                            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                                            .substring(1, 3));
                    }
                    return sb.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
            }
            return null;
    }
	
	public static void main(String[] args){
		try {
			getAdressMsg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String getAdressMsg() throws Exception{
		String adressMsg = "";
		
		for(int i = 10; i<99 ; i++){
			
			String path="http://gc.ditu.aliyun.com/regeocoding?l=23.SS48,113.9167&type=11";
			String s = path.replace("SS", String.valueOf(i));
			//System.out.println(s);
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(s);
			HttpResponse response = client.execute(httpget);
			InputStream is = response.getEntity().getContent();
			String result = inStream2String(is);
			// 打印响应内容
			
			JSONObject jo = (JSONObject) JSONObject.parse(result);
			JSONArray ja = jo.getJSONArray("addrList");
			
			if(ja.size() != 2){
				return "没有详细地址信息";
			}
			
			StringBuffer sb = new StringBuffer();
			
			JSONObject jo1 = (JSONObject) ja.get(0);
			int status1 = jo1.getInteger("status");
			if(status1 == 1){
				sb.append(jo1.getString("admName")).append(",").append(jo1.getString("name"));
			}
			
			
			JSONObject jo2 = (JSONObject) ja.get(1);
			int status2 = jo2.getInteger("status");
			if(status2 == 1){
				if(sb != null && sb.length() != 0 ){
					sb.append(",").append(jo2.getString("name"));
				}else{
					sb.append(jo2.getString("admName")).append(jo2.getString("name"));
				}
			}
			
			
			System.out.println(sb.toString());
			//System.out.println("status1=" +  status1 + "  name1=" + name1  +"    "  + "  status2=" + status2 + "  name2=" + name2);
			System.out.println(result);
			//System.out.println(ja.size());
			
		}
		
		return adressMsg;
		
	}
	
	
}

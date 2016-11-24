package cn;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

//��Ҫhttpclient-x.x.jar��httpcore-x.x.jar��commons-logging-x.x.jar��commons-httpclient-x.x.jar��commons-codec-x.x.jar����http����
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class SnTest {
	public static void main(String[] args) throws Exception {
		SnTest snTest = new SnTest();
		snTest.testGet();
		snTest.testPost();
	}

	public void testGet() throws Exception {
		/**
		 * ��http://api.map.baidu.com/geocoder/v2/?address=�ٶȴ���&output=json&ak=yourakΪ��
		 * ak������snУ�鲻��ֱ��ʹ�ñ�����url�����snֵ��get�������sn��url�в����Գ���˳���йأ��谴�����paramsMap��
		 * post�����ǰ���ĸ����䣬�������testPost()
		 */
		Map paramsMap = new LinkedHashMap<String, String>();
		paramsMap.put("address", "�ٶȴ���");
		paramsMap.put("output", "json");
		paramsMap.put("ak", "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG");

		// ���������toQueryString��������paramsMap������value��utf8����
		String paramsStr = toQueryString(paramsMap);

		// ��paramsStrǰ��ƴ����/geocoder/v2/?������ֱ��ƴ��yoursk
		String wholeStr = new String("/geocoder/v2/?" + paramsStr + "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG");

		// ������wholeStr����utf8����
		String tempStr = URLEncoder.encode(wholeStr, "UTF-8");

		// ���������MD5�����õ�snǩ��ֵ
		String sn = MD5(tempStr);

		// ���sn����get����
		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(
				"http://api.map.baidu.com/geocoder/v2/?address=�ٶȴ���&output=json&ak=7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG&sn="
						+ sn);
		HttpResponse response = client.execute(httpget);
		InputStream is = response.getEntity().getContent();
		String result = inStream2String(is);
		// ��ӡ��Ӧ����
		System.out.println(result);
	}

	public void testPost() throws Exception {
		/**
		 * ��http://api.map.baidu.com/geodata/v3/geotable/create������Ϊ��
		 */
		LinkedHashMap<String, String> paramsMap = new LinkedHashMap<String, String>();
		paramsMap.put("geotype", "1");
		paramsMap.put("ak", "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG");
		paramsMap.put("name", "geotable80");
		paramsMap.put("is_published", "1");

		// post�����ǰ���ĸ����䣬�������paramsMap��key����ĸ������
		Map<String, String> treeMap = new TreeMap<String, String>(paramsMap);
		String paramsStr = toQueryString(treeMap);

		String wholeStr = new String("/geodata/v3/geotable/create?" + paramsStr
				+ "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG");
		String tempStr = URLEncoder.encode(wholeStr, "UTF-8");
		// ���������MD5�����õ�snǩ��ֵ
		String sn = MD5(tempStr);

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(
				"http://api.map.baidu.com/geodata/v3/geotable/create");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("geotype", "1"));
		params.add(new BasicNameValuePair("ak", "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG"));
		params.add(new BasicNameValuePair("name", "geotable80"));
		params.add(new BasicNameValuePair("is_published", "1"));
		params.add(new BasicNameValuePair("sn", sn));
		HttpEntity formEntity = new UrlEncodedFormEntity(params);
		post.setEntity(formEntity);
		HttpResponse response = client.execute(post);
		InputStream is = response.getEntity().getContent();
		String result = inStream2String(is);
		// ��ӡ��Ӧ����
		System.out.println(result);
	}

	// ��Map������value��utf8���룬ƴ�ӷ��ؽ��
	public String toQueryString(Map<?, ?> data)
			throws UnsupportedEncodingException, URIException {
		StringBuffer queryString = new StringBuffer();
		for (Entry<?, ?> pair : data.entrySet()) {
			queryString.append(pair.getKey() + "=");
			// queryString.append(URLEncoder.encode((String) pair.getValue(),
			// "UTF-8") + "&");
			queryString.append(URIUtil.encodeQuery((String) pair.getValue(),
					"UTF-8") + "&");
		}
		if (queryString.length() > 0) {
			queryString.deleteCharAt(queryString.length() - 1);
		}
		return queryString.toString();
	}

	// MD5���㷽����������MessageDigest�⺯��������byte������ת����16����
	public String MD5(String md5) {
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

	// ��������ת�����ַ���
	private static String inStream2String(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return new String(baos.toByteArray(), "UTF-8");
	}
}


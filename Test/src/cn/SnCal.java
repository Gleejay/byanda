package cn;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
 
//java版计算signature签名
public class SnCal {
        public static void main(String[] args) throws UnsupportedEncodingException,
                        NoSuchAlgorithmException {
                SnCal snCal = new SnCal();
                
                String ak = "7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG";
                String sk = "0GpqAHtY6VY9ZSVCs3mymea8WYFULjab";
                // 计算sn跟参数对出现顺序有关，所以用LinkedHashMap保存<key,value>，此方法适用于get请求，如果是为发送post请求的url生成签名，请保证参数对按照key的字母顺序依次放入Map。以get请求为例：http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=yourak，paramsMap中先放入address，再放output，然后放ak，放入顺序必须跟get请求中对应参数的出现顺序保持一致。
                Map paramsMap = new LinkedHashMap<String, String>();
//                paramsMap.put("address", "腾讯大厦");
//                paramsMap.put("output", "json");
//                paramsMap.put("ak", ak);
                
                
                paramsMap.put("ak", ak);
                paramsMap.put("callback", "renderReverse");
                paramsMap.put("location", "39.983424,116.322987");
                paramsMap.put("output", "xml");
                paramsMap.put("pois", "1");
                // 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
                String paramsStr = snCal.toQueryString(paramsMap);
                // 对paramsStr前面拼接上/geocoder/v2/?，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
                String wholeStr = new String("/geocoder/v2/?" + paramsStr + sk);
                // 对上面wholeStr再作utf8编码
                String tempStr = URLEncoder.encode(wholeStr, "UTF-8");
                // 调用下面的MD5方法得到最后的sn签名7de5a22212ffaa9e326444c75a58f9a0
                System.out.println(snCal.MD5(tempStr));
                
                
                //http://api.map.baidu.com/geocoder/v2/?ak=7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG&callback=renderReverse&location=39.983424,116.322987&output=xml&pois=1&sn=3e5c33840cd70abc1f358c08f5746c96
                
                
                //http://api.map.baidu.com/geocoder/v2/?location=22.546098752058403,113.94108811020104&output=xml&ak=7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG&sn=27b762b48416f8099eab3ea2724aec95
                
                
                //http://api.map.baidu.com/geocoder/v2/?address=腾讯大厦&output=json&ak=7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG&sn=1bbe2a33683544ef900602821dfc1a23
                //{"status":0,"result":{"location":{"lng":113.94108811020104,"lat":22.546098752058403},"precise":1,"confidence":80,"level":"商务大厦"}}
                //http://api.map.baidu.com/geocoder/v2/?address=百度大厦&output=json&ak=7Qf8yBCoc89ls1p0WoiyeKErCumQqCqG&sn=158a528590d6650d9ebbc142a9569754
                //{"status":0,"result":{"location":{"lng":116.30775539540982,"lat":40.05685561073758},"precise":1,"confidence":80,"level":"商务大厦"}}
        }
 
        // 对Map内所有value作utf8编码，拼接返回结果
        public String toQueryString(Map<?, ?> data)
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
}
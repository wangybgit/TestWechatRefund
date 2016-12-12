import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class Refund {
	public static void main(String[] args) throws Exception {
        SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
       parameters.put("appid", "xxxxxxxx");//公众号id
       parameters.put("mch_id", "xxxxxx");//商户号
       parameters.put("nonce_str", CreateNoncestr());
      //在notify_url中解析微信返回的信息获取到 transaction_id，此项不是必填，详细请看上图文档
     //  parameters.put("transaction_id", "微信支付订单中调用统一接口后微信返回的 transaction_id");//交易单号(微信产生的)，与out_trade_no二者选其一即可
       parameters.put("out_trade_no", "xxxxxxxx");//要退款的订单号(商户侧)

       parameters.put("out_refund_no", Ids.uuidAsHex());    //我们自己设定的退款申请号，约束为UK
       														//新生成的退款订单号
       parameters.put("total_fee", "1") ;     //订单总金额,单位为分
       parameters.put("refund_fee", "1");     //退款金额，单位为分
       parameters.put("op_user_id", "xxxxx");//操作员id，默认商户id
       String sign = createSign("utf-8", parameters);
       parameters.put("sign", sign);
       
       String reuqestXml = getRequestXml(parameters);
      KeyStore keyStore  = KeyStore.getInstance("PKCS12");
      FileInputStream instream = new FileInputStream(new File("D:/apiclient_cert.p12"));//放退款证书的路径
      try {
          keyStore.load(instream, "商户号".toCharArray());
      } finally {
          instream.close();
      }

      SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, "商户号".toCharArray()).build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
              sslcontext,
              new String[] { "TLSv1" },
              null,
              SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
      CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
      try {

          HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");//退款接口
          
          System.out.println("executing request" + httpPost.getRequestLine());
          StringEntity  reqEntity  = new StringEntity(reuqestXml);
          // 设置类型 
          reqEntity.setContentType("application/x-www-form-urlencoded"); 
          httpPost.setEntity(reqEntity);
          CloseableHttpResponse response = httpclient.execute(httpPost);
          try {
              HttpEntity entity = response.getEntity();

              System.out.println("----------------------------------------");
              System.out.println(response.getStatusLine());
              if (entity != null) {
                  System.out.println("Response content length: " + entity.getContentLength());
                  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
                  String text;
                  while ((text = bufferedReader.readLine()) != null) {
                      System.out.println(text);
                  }
                 
              }
              EntityUtils.consume(entity);
          } finally {
              response.close();
          }
      } finally {
          httpclient.close();
      }
}
public static String CreateNoncestr() {
      String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      String res = "";
      for (int i = 0; i < 16; i++) {
          Random rd = new Random();
          res += chars.charAt(rd.nextInt(chars.length() - 1));
      }
      return res;
}
public static String createSign(String charSet,SortedMap<Object,Object> parameters){
      StringBuffer sb = new StringBuffer();
      Set es = parameters.entrySet();
      Iterator it = es.iterator();
      while(it.hasNext()) {
          Map.Entry entry = (Map.Entry)it.next();
          String k = (String)entry.getKey();
          Object v = entry.getValue();
          if(null != v && !"".equals(v) 
                  && !"sign".equals(k) && !"key".equals(k)) {
              sb.append(k + "=" + v + "&");
          }
      }
      sb.append("key=" + "支付密钥");
      //String sign = MD5Util.MD5Encode(sb.toString(), charSet).toUpperCase();
      String sign=MD5Util.MD5(sb.toString()).toLowerCase();
      return sign;
}
public static String getRequestXml(SortedMap<Object,Object> parameters){
  StringBuffer sb = new StringBuffer();
  sb.append("<xml>");
  Set es = parameters.entrySet();
  Iterator it = es.iterator();
  while(it.hasNext()) {
      Map.Entry entry = (Map.Entry)it.next();
      String k = (String)entry.getKey();
      String v = (String)entry.getValue();
      if ("attach".equalsIgnoreCase(k)||"body".equalsIgnoreCase(k)||"sign".equalsIgnoreCase(k)) {
          sb.append("<"+k+">"+"<![CDATA["+v+"]]></"+k+">");
      }else {
          sb.append("<"+k+">"+v+"</"+k+">");
      }
  }
  sb.append("</xml>");
  return sb.toString();
}
}

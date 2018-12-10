
package com.FileUpdate;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;


/**
 * @author 辉涛
 * @ClassName: FileUpdate
 * @Description: 对压缩得到的文件进行传输
 *               1.向服务器发送请求 获得key与value
 *               2.用公钥对value加密
 *               3.发送文件，附加key和valueEnc
 * @Email theo_hui@163.com
 * @Date 2018/11/19 20:58
 */


public class FileUpdate {
    private String ipAddress;//服务器ip地址

    public String keyget=null;//服务器获得的key
    public String valueget=null;//服务器获得的value
    public byte[] valueEncrypt=null;//加密后的value


    //RSA公钥
    private static final String DEFAULT_PUBLIC_KEY=
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG3s6b9PfDlWubbFbM7rNcivLu"+"\r"+
            "YxpIVDq3h19DF06/OXlgAPox/fzI3y920wtkG3A3PVFZFLOq8vCn27iUlyaCpH0K"+"\r"+
            "QaV9iYcTcRUBhwxII5m8uCwl7HK75WCe/qEXAb1F/dD6zACjx1ALXoGIcq3se6ZP"+"\r"+
            "ueTynXSG7wxdnenkEQIDAQAB"+"\r";

    //RSA公钥
    private RSAPublicKey publicKey;


    /*构造函数*/
    public FileUpdate(String ipAddress) {
        //设置ip地址和端口
        this.ipAddress = ipAddress;

        //进行公钥加载
        try {
            loadPublicKey(DEFAULT_PUBLIC_KEY);
            System.out.println("加载公钥成功");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("加载公钥失败");
        }
    }

    /**
     * 从服务器端获得key和value
     *
     *
     *  得到的key和value写入keyget和valueget
     *
     */
    public void getKeyfromServe(){
        //Json数据对象
        JSONObject jsonObject = null;

        try{
            //http请求
            URL url = new URL(ipAddress+"/getkv");
            HttpURLConnection cnnt=(HttpURLConnection)url.openConnection();
            cnnt.connect();

            //数据读取
            BufferedReader bReader = new BufferedReader(
                    new InputStreamReader(cnnt.getInputStream(), "UTF-8"));

            // 对数据进行访问
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            // 关闭流
            bReader.close();
            // 关闭链接
            cnnt.disconnect();

            // 获取的json结果
            jsonObject = new JSONObject(stringBuilder.toString());

            System.out.println("\n从服务器请求到消息："+jsonObject);
            System.out.println();

            //解析Json
            String password = jsonObject.getString("password");
            String result = jsonObject.getString("result");
            String username = jsonObject.getString("username");

            //若请求成功 写入
            if (result.equals("ok")){
                this.keyget=username;//获得key
                this.valueget=password;//获得value
                System.out.println(password);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将文件附加key和加密后的value一起上传
     *
     * @param upFile 上传的文件
     *
     * @return void
     */
    public void updateFiletoServe(File upFile){
        //http客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{

            //创建post请求
            HttpPost httpPost = new HttpPost(ipAddress+"/getfile");
            //把文件转换成流对象FileBody
            FileBody bin = new FileBody(upFile);

            //附加 key 和加密后的value
            StringBody key = new StringBody(this.keyget, ContentType.create("text/plain", Consts.UTF_8));
            ByteArrayBody valueEnc = new ByteArrayBody(this.valueEncrypt,null);
            //StringBody valueEnc = new StringBody(this.valueEncrypt, ContentType.create("text/plain", Consts.UTF_8));

            //构造发送对象
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)//文件
                    .addPart("key", key)//key
                    .addPart("valueEnc", valueEnc)//加密后的value
                    .build();

            //发送
            httpPost.setEntity(reqEntity);

            System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
            System.out.println("加密后的value"+valueEncrypt);
            //发起请求   并返回请求的响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                System.out.println("+-------------------------------------------------+");
                //打印响应状态
                System.out.println("    "+response.getStatusLine());
                //获取响应对象
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    //打印响应长度
                    System.out.println("    Response content length: " + resEntity.getContentLength());
                    //打印响应内容
                    System.out.println("    "+EntityUtils.toString(resEntity,Charset.forName("UTF-8")));
                }
                //销毁
                EntityUtils.consume(resEntity);
                System.out.println("+----------------------------------------------------+");
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                }catch (Exception e2){
                    e2.printStackTrace();
                }

            }
        }catch (Exception e1){
            e1.printStackTrace();
        }finally{
            try {
                httpClient.close();
            }catch (Exception e3){
                e3.printStackTrace();
            }
        }
    }

    /**
     * 获取RSA公钥
     * @return 当前的公钥对象
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }


    /**
     * 从字符串中加载公钥
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public void loadPublicKey(String publicKeyStr) throws Exception{
        try {
            BASE64Decoder base64Decoder= new BASE64Decoder();
            byte[] buffer= base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);
            this.publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 对value进行加密
     *
     * 加密后的结果写入valueEnc
     *
     */
    public void encryptionRSA() throws Exception {
        if(publicKey== null){
            throw new Exception("加密公钥为空, 请设置");
        }
        Cipher cipher= null;
        try {
            //设置加密模式
            cipher= Cipher.getInstance("RSA/NONE/PKCS1Padding", new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE,  getPublicKey());

            byte[] output= cipher.doFinal(valueget.getBytes("utf-8"));//加密
            System.out.println(output.length);
            valueEncrypt=output;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws Exception {
//        FileUpdate test = new FileUpdate("http://139.59.221.246:45678");
//        test.getKeyfromServe();
//        test.encryptionRSA();
//        test.updateFiletoServe(new File("E:\\java" +
//                "\\自动备份工具1.0\\icons\\back.ico"));
//    }

}

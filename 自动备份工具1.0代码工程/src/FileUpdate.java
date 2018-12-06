
package com.FileUpdate;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * @author Theo_hui
 * @ClassName: FileUpdate
 * @Description: 对压缩得到的文件进行传输
 *               1.向服务器发送请求 获得key与userid
 *               2.对key进行加密签名
 *               3.发送文件，附加userid 与签名信息
 * @Email theo_hui@163.com
 * @Date 2018/11/19 20:58
 */


public class FileUpdate {
    private int port;//服务器端口
    private String ipAddress;//服务器ip地址
    public static final String KEY_RSA = "RSA";//定义加密方式
    private final static String KEY_RSA_SIGNATURE = "MD5withRSA";//定义签名算法
    private final static String prikeyRelaPath="\\prikey\\prikey.pem";//密钥签证相对路径
    public String keyget=null;//服务器获得的密钥
    public String userget=null;//服务器获得的用户标签

    private PrivateKey privateKey;//RSA密钥
    private String SIGNEDkey=null;//签名后的密钥

    /*构造函数*/
    public FileUpdate(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
    public FileUpdate(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * 从服务器端获得key和用户标签
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

            // 获取的结果
            jsonObject = new JSONObject(stringBuilder.toString());

            System.out.println("\n从服务器请求到消息："+jsonObject);
            System.out.println();

            //解析Json
            String password = jsonObject.getString("password");
            String result = jsonObject.getString("result");
            String username = jsonObject.getString("username");

            if (result.equals("ok")){
                this.keyget=password;
                this.userget=username;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将文件附加验证信息与用户标签一起上传到服务器
     *
     * @param upFile 上传的文件
     *
     * @return void
     */
    public void updateFiletoServe(File upFile){
        //
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            //要上传的文件的路径
            //String filePath = "E:\\java\\备份工具\\test.txt";
            //创建post请求
            HttpPost httpPost = new HttpPost(ipAddress+"/getfile");
            //把文件转换成流对象FileBody
            FileBody bin = new FileBody(upFile);

            //附加 username 和签名信息
            StringBody username = new StringBody(this.userget, ContentType.create("text/plain", Consts.UTF_8));
            StringBody password = new StringBody(this.SIGNEDkey, ContentType.create("text/plain", Consts.UTF_8));

            //构造
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)//相当于<input type="file" name="file"/>
                    .addPart("name", username)//相当于<input type="text" name="name" value=name>
                    .addPart("password", password)
                    .build();

            //发送
            httpPost.setEntity(reqEntity);

            System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
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
     * 从私钥证书加载私钥 对获得的key进行签名
     *
     */
    public void encryptionRSA(){
        Security.addProvider(new BouncyCastleProvider());

        try{
            //从pem证书加载密钥证书
            File f = new File(System.getProperty("user.dir")+prikeyRelaPath);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.length()];
            dis.readFully(keyBytes);
            dis.close();

            String temp = new String(keyBytes);
            String privKeyPEM = temp.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
            privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
            //System.out.println("Private key\n"+privKeyPEM);

            byte [] decoded = Base64.getMimeDecoder().decode(privKeyPEM );

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            //转化为密钥
            KeyFactory kf = KeyFactory.getInstance(this.KEY_RSA );
            this.privateKey= kf.generatePrivate(spec);
            System.out.println("----------------------------------------");
            System.out.println("加载密钥完毕");
//
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.DECRYPT_MODE, pk);
//            byte[] plainText = cipher.doFinal(src.getBytes());
//            System.out.println(plainText);

            //对keyget进行签名
            Signature signature = Signature.getInstance(this.KEY_RSA_SIGNATURE);
            signature.initSign(this.privateKey);
            signature.update(this.keyget.getBytes());
            byte[] result = signature.sign();

            System.out.println("对key加密结果"+result);
            System.out.println("----------------------------------------");

            this.SIGNEDkey= new String(result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


//    public static void main(String[] args){
//        FileUpdate test = new FileUpdate("http://159.65.136.29");
//        test.getKeyfromServe();
//        test.encryptionRSA();
//        test.updateFiletoServe(new File("E:\\java\\备份工具\\test.txt"));
//    }

}

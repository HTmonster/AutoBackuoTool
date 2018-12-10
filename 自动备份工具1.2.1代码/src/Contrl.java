
package com.contrl;

import java.io.*;
import java.util.*;

import com.DBoperator.DBoperator;
import com.filezip.filezip;
import com.FileUpdate.FileUpdate;

/**
 * @ClassName: Contrl
 * @Description: 后台总线控制类 每隔一段时间 上传文件
 * @author Theo_hui
 * @Email theo_hui@163.com
 * @Date 2018/11/21 16:15
 */


public class Contrl {
    private static Integer cacheTime = 1000*60;//间隔运行时间 60s
    private static Integer delay = 1000;//延迟时间，1s

    //压缩文件缓存目录
    final static String fileBoxPath= System.getProperty("user.dir")+"\\fileBox\\";

    /*构造函数*/
    public static void run_contrl(){

        //定时器
        Timer timer = new Timer();
        //数据库对象
        DBoperator DBoper=new DBoperator();
        //数据压缩对象
        filezip fzip=new filezip();
        //文件上传对象
        FileUpdate Fup=new FileUpdate("http://139.59.221.246:45678");

        //定时器运行
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //获取要更新的文件
                List<String> updateFile= DBoper.getFilepath_of_filetoUpdate();

                //清空压缩文件目录fileBox
                fzip.CleanDir();

                //压缩文件到压缩文件存储目录
                for(String value:updateFile){
                    //压缩文件
                    fzip.DozipFiles(new File(value));
                    //更新下次更新时间
                    DBoper.update_time(value);
                }

                //服务器获取key和value并加密
                Fup.getKeyfromServe();
                try{
                    Fup.encryptionRSA();
                }catch (Exception e){
                    e.printStackTrace();
                }

                //把压缩目录中的文件上传到服务器
                File[] upFiles = new File(fileBoxPath).listFiles();
                for(File upf:upFiles){

                    System.out.println("正在上传文件："+upf);

                    Fup.updateFiletoServe(upf);
                }

                System.out.println("[CTRL is runing......]");

            }
        }, delay,cacheTime);
    }

}

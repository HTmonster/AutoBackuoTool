
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
    private static Integer cacheTime = 1000*60;
    //延迟时间，时间单位为毫秒
    private static Integer delay = 1000;
    //压缩文件缓存仓库
    final static String fileBoxPath= System.getProperty("user.dir")+"\\fileBox\\";

    public static void run_contrl(){

        //定时器
        Timer timer = new Timer();
        //数据库对象
        DBoperator DBoper=new DBoperator();
        //数据压缩对象
        filezip fzip=new filezip();
        //文件上传对象
        FileUpdate Fup=new FileUpdate("http://159.65.136.29");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> updateFile= DBoper.getFilepath_of_filetoUpdate();
                filezip fzip=new filezip();

                //清空压缩文件仓库fileBox
                fzip.CleanDir();

                //压缩文件到压缩文件仓库fileBox
                for(String value:updateFile){
                    System.out.println(value);

                    fzip.DozipFiles(new File(value));
                    //更新下次更新时间
                    DBoper.update_time(value);
                }

                //服务器签名处理
                Fup.getKeyfromServe();
                Fup.encryptionRSA();

                //把压缩工厂中的文件上传到服务器
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


package com.filezip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Theo_hui
 * @ClassName: filezip
 * @Description: 压缩文件 以便传输
 * @Email theo_hui@163.com
 * @Date 2018/11/19 19:55
 */


public class filezip {
    private File tgtFile;//备份成的文件
    final static String fileBoxPath= System.getProperty("user.dir")+"\\fileBox\\";//上传缓存仓库路径

    public filezip(){

        //不存在缓存空间则创建
        File fileBox = new File(fileBoxPath);
        if(!fileBox.exists()){
            fileBox.mkdir();
        }
    }
    /**
     * 对输入的文件进行压缩
     */
    public void DozipFiles(File srcFlie) {

        String tgtdir=fileBoxPath+srcFlie.getName()+".zip";
        this.tgtFile = new File(tgtdir);

        ZipOutputStream zipout = null;

        try {
            zipout =
                    new ZipOutputStream(new FileOutputStream(tgtFile));
            //压缩文件
            zipselect(srcFlie,zipout,"");

            System.out.println("文件压缩完毕"+srcFlie);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (zipout!=null) zipout.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 依据文件还是文件夹选择压缩的方法
     *
     * @param file    要压缩的文件（文件夹）
     *        out     zip输出的流
     *        basedir 压缩的路径名
     */
    public void zipselect(File file,ZipOutputStream out,String basedir){

        if(file.isDirectory()){
            this.zipDirectory(file,out,basedir);
        }else {
            this.zipFile(file,out,basedir);
        }
    }

    /**
     *压缩单独文件
     *
     * @param  file 要压缩的文件
     *         out     zip输出的流
     *         basedir 压缩的路径名
     */
    public void zipFile(File file,ZipOutputStream out,String basedir){

        if(!file.exists()){
            return;
        }

        byte[] buf=new byte[1024];
        FileInputStream fin=null;

        try {
            int len;
            fin=new FileInputStream(file);
            out.putNextEntry(new ZipEntry(basedir+file.getName()));

            while ((len= fin.read(buf))>0){
                out.write(buf,0,len);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {

            try {
                 if(out!=null){
                     out.closeEntry();
                 }
                 if(fin!=null){
                     fin.close();
                 }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩文件夹
     *
     * @para  file    要压缩的文件夹
     *        out     zip输出的流
     *        basedir 压缩的路径名
     */
    public void zipDirectory(File dir,ZipOutputStream out,String basedir){

        if(!dir.exists()){
            return;
        }

        File[] files=dir.listFiles();

        for(int i=0;i<files.length;i++){

            /*递归压缩*/
            zipselect(files[i],out,basedir+dir.getName()+"/");
        }
    }

    /**
     * 清空压缩文件仓库中的所有压缩文件
     *
     * @return boolean 删除的结果
     */
    public static boolean CleanDir(){
        File file = new File(fileBoxPath);

        if(!file.exists()){//判断是否待删除目录是否存在
            System.err.println("The dir are not exists!");
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件
        for(String name : content){
            File temp = new File(fileBoxPath, name);

            System.out.println("正在清除压缩文件仓库");
            if(!temp.delete()){//直接删除文件
                System.err.println("Failed to delete " + name);
            }
        }
        return true;
    }

//    public static void main(String[] args){
//        File srcFile=new File("E:/java/备份工具/icons");
//        //File tgtFile=new File("E:/java/备份工具/test"+".zip");
//
//        filezip fzip=new filezip();
//        //fzip.CleanDir();
//
//        fzip.DozipFiles(srcFile);
//    }
}

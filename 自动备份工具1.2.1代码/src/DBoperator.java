
package com.DBoperator;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName: DBoperator
 * @Description: 数据库操作
 * @author Theo_hui
 * @Email theo_hui@163.com
 * @Date 2018/11/22 14:03
 */


public class DBoperator {
    //时间格式
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // JDBC 驱动名
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    //远程数据库
    static final String DB_URL = "jdbc:mysql://139.59.221.246:3306/test?useSSL=false&useUnicode=true&characterEncoding=UTF-8";
    static final String USER = "backup";
    static final String PASS = "password";


    /**
     * 获得已经设置好的<文件路径,更新频率>
     *
     * @return <文件路径,更新频率>
     */
    public Map<String, String>getFile_FreSeted(){
        Connection conn = null;
        Statement stmt = null;

        //要返回的已经设置文件路径 更新频率
        Map<String,String> retFile_Fre =new HashMap<String, String>();

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println("查询所有并获得文件路径集合");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT Hash,filePath, freqence, updateTime FROM backuptable;";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int hashcode =rs.getInt("Hash");
                String filePath  = rs.getString("filePath");
                String freqence = rs.getString("freqence");
                Timestamp t= rs.getTimestamp("updateTime");

                // 输出数据
                System.out.print("["+hashcode+"]");
                System.out.print("PATH:" + filePath);
                System.out.print(", 频率 " + freqence);
                System.out.print(", 要更新的时间: " + t);
                System.out.print("\n");

                retFile_Fre.put(filePath,freqence);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("===========================查询结束================================");

        return retFile_Fre;
    }


    /**
     * 插入一项到数据库
     *
     * @param filePath 文件路径
     *        freqence     更新频率
     *
     * @return boolean 是否成功
     */
    public boolean insertOneItem(String filePath,String freqence){

        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println("<数据库>插入到数据库");
            stmt = conn.createStatement();
            String sql = "INSERT INTO backuptable(Hash,filePath,freqence,updateTime) VALUES (?,?,?,?)";
            PreparedStatement pstmt;


            try {
                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                pstmt.setInt(1, filePath.hashCode());
                pstmt.setString(2, filePath);
                pstmt.setString(3,freqence);

                //获取当前时间
                Calendar now=Calendar.getInstance();

                //得到下次要更新的时间
                switch (freqence){
                    case "F_MINTE":now.add(Calendar.MINUTE,1); break;
                    case "F_HOUR": now.add(Calendar.HOUR_OF_DAY,1);break;
                    case "F_DAY": now.add(Calendar.DAY_OF_MONTH,1);break;
                    case "F_WEEK":now.add(Calendar.WEEK_OF_MONTH,1);break;
                    case "F_MONTH":now.add(Calendar.MONTH,1);break;
                }

                String dateStr=sdf.format(now.getTimeInMillis());

                pstmt.setString(4, dateStr);
                pstmt.executeUpdate();

                pstmt.close();
                conn.close();

                System.out.println("===========================插入完毕======================");

                return true;

            } catch (SQLException e11) {
                e11.printStackTrace();
            }

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e111){
            // 处理 Class.forName 错误
            e111.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 删除一条记录
     *
     * @param filepath 该文件的路径字段
     *
     */
    public void deloneItem(String filepath){
        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            System.out.println("<数据库>删除文件文件");

            // 执行删除
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM backuptable WHERE Hash ="+filepath.hashCode()+";";
            System.out.println("sql语句："+sql);
            stmt.executeUpdate(sql);



            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        System.out.println("===========================删除=====================");

    }

    /**
     * 更新一条记录的频率值
     *
     * @param filepath 文件路径字段 要更新成的频率
     *
     */
    public void updateOneItem(String filepath,String freqence){
        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            System.out.println("<数据库>更新文件频率");
            System.out.println(filepath+"要更新为"+freqence);

            // 执行更新
            stmt = conn.createStatement();
            String sql;
            sql = "UPDATE backuptable SET freqence = " +"'"+freqence+"'WHERE Hash ="+filepath.hashCode()+";";
            System.out.println("sql语句："+sql);
            stmt.executeUpdate(sql);

            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        System.out.println("===========================更新=====================");
    }

    /**
     * 获得现在要更新的文件
     *
     *   获得updateTimez字段为现在时间的记录
     *
     * @return 要更新的文件路径集合
     */
    public List<String> getFilepath_of_filetoUpdate(){
        List<String> retFilePath=new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //查询条件 当前时间
            System.out.println("<数据库>查询要更新的文件");
            Calendar now=Calendar.getInstance();
            String dateStr=sdf.format(now.getTimeInMillis());
            System.out.println("要查询的时间"+dateStr);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT filePath FROM backuptable WHERE unix_timestamp(updateTime)<=unix_timestamp("+"'"+dateStr+"'"+");";
            System.out.println("sql语句："+sql);
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String filePath  = rs.getString("filePath");

                retFilePath.add(filePath);
                System.out.println("找到记录"+filePath);
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        System.out.println("===========================查找完毕=====================");

        return retFilePath;
    }

    /**
     * 更新某文件的下次更新的时间
     *
     * @param filepath 要更新的文件路径字段
     *
     */
    public void update_time(String filepath){
        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //查询条件 当前时间
            System.out.println("<数据库>更新的时间");


            //查询该条记录的频率值
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT freqence FROM backuptable WHERE Hash="+filepath.hashCode()+";";
            System.out.println("sql语句："+sql);
            ResultSet rs = stmt.executeQuery(sql);

            //获得频率值
            String freqence="";
            if(rs.next()) freqence = rs.getString("freqence");
            System.out.println(freqence);

            //依据频率更新时间
            Calendar now=Calendar.getInstance();
            now.add(Calendar.MINUTE,-1);//时间为更新的时间 即上一分钟的时间
            //根据频率值更新下次更新的时间
            switch (freqence){
                case "F_MINTE":now.add(Calendar.MINUTE,1); break;
                case "F_HOUR": now.add(Calendar.HOUR_OF_DAY,1);break;
                case "F_DAY": now.add(Calendar.DAY_OF_MONTH,1);break;
                case "F_WEEK":now.add(Calendar.WEEK_OF_MONTH,1);break;
                case "F_MONTH":now.add(Calendar.MONTH,1);break;
            }
            String dateStr=sdf.format(now.getTimeInMillis());
            System.out.println("要更新的时间"+dateStr);

            //更新时间
            sql = "UPDATE backuptable SET updateTime="+"'"+dateStr+"'"+" WHERE Hash="+filepath.hashCode()+";";
            System.out.println("sql语句："+sql);
            stmt.executeUpdate(sql);

            // 完成后关闭
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        System.out.println("====================更新完成=========================");
    }

//    public static void main(String[] args) {
//        DBoperator op= new DBoperator();
//
//        System.out.println(op.getFile_FreSeted());
//         //op.updateOneItem("D:\\壁纸\\brema-elamkovan-1155019" + "-unsplash.jpg","F_WEEK");
//////        op.deloneItem(
//////                "D:\\壁纸\\28ae32d179eec443cf21ba86439f28ea.jpg");
////        op.update_time("E:\\java\\自动备份工具1.0\\icons\\icon1" +
////                ".png");
//          op.getFilepath_of_filetoUpdate();
////
////        //op.insertOneItem("aaaa","hhhhh","2018-11-22 11:54:48");
//    }
}

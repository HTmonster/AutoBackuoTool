
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

    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/backupdb?useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "BackupUser";
    static final String PASS = "1234";

    /**
     * 获得已经设置好的文件
     *
     * @return 文件路径的集合
     */
    public List<String> getFilePath(){
        Connection conn = null;
        Statement stmt = null;

        List <String> retFilePath= new ArrayList<>();

        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println("查询所有并获得文件路径集合");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT Hash,filePath, freq, lastUpdateTime FROM BackupTable;";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int hashcode =rs.getInt("Hash");
                String filePath  = rs.getString("filePath");
                String freq = rs.getString("freq");
                Timestamp t= rs.getTimestamp("lastUpdateTime");

                // 输出数据
                System.out.print("["+hashcode+"]");
                System.out.print("PATH:" + filePath);
                System.out.print(", 频率 " + freq);
                System.out.print(", 要更新的时间: " + t);
                System.out.print("\n");

                retFilePath.add(filePath);
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

        return retFilePath;
    }

    /**
     * 插入一项到数据库
     *
     * @param filePath 文件路径
     *        freq     更新频率
     *
     * @return boolean 是否成功
     */
    public boolean insertOneItem(String filePath,String freq){

        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            System.out.println("<数据库>插入到数据库");
            stmt = conn.createStatement();
            String sql = "INSERT INTO BackupTable(Hash,filePath,freq,lastUpdateTime) VALUES (?,?,?,?)";
            PreparedStatement pstmt;


            try {
                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                pstmt.setInt(1, filePath.hashCode());
                pstmt.setString(2, filePath);
                pstmt.setString(3,freq);

                //获取当前时间
                Calendar now=Calendar.getInstance();

                //得到下次要更新的时间
                switch (freq){
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
     * 获得现在要更新的文件
     *
     * @return 要更新的文件路径集合
     */
    public List<String> getFilepath_of_filetoUpdate(){
        List<String> retFilePath=new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

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
            sql = "SELECT filePath FROM BackupTable WHERE unix_timestamp(lastUpdateTime)<=unix_timestamp("+"'"+dateStr+"'"+");";
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
     * 更新某文件的下次更新时间为现在时间
     *
     * @param filepath 要更新的文件路径
     *
     */
    public void update_time(String filepath){
        Connection conn = null;
        Statement stmt = null;

        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.jdbc.Driver");

            // 打开链接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //查询条件 当前时间
            System.out.println("<数据库>更新的时间");
            Calendar now=Calendar.getInstance();
            String dateStr=sdf.format(now.getTimeInMillis());
            System.out.println("要更新的时间"+dateStr);
            // 执行查询

            stmt = conn.createStatement();
            String sql;
            sql = "UPDATE backuptable SET lastUpdateTime="+"'"+dateStr+"'"+" WHERE Hash="+filepath.hashCode()+";";
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
//        System.out.println(op.getFilePath());
//        op.update_time("E:\\java\\备份工具\\icons");
//        op.getFilepath_of_filetoUpdate();
//
//        //op.insertOneItem("aaaa","hhhhh","2018-11-22 11:54:48");
//    }
}

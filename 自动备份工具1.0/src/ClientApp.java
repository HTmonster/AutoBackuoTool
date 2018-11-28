
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.DBoperator.DBoperator;
import com.contrl.Contrl;


/**
 * @ClassName: ClientApp
 * @Description: 软件的主体
 *               包括登录界面 和设置界面
 * @author Theo_hui
 * @Email theo_hui@163.com
 * @Date 2018/11/19 21:43
 */


public class ClientApp {

    public static void main(String[] args){
        //运行主体框架
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                //登录界面
                onloadFrame onload =new onloadFrame();
            }
        });
    }
}


/****************************************************
 * 设置界面 主界面
 *
 *  设置要自动更新的文件 更新频率
 *  退出 后台继续运行
 */
class setFrame extends JFrame {
    //频率文字 与 选择框选择号的 映射
    final static List<String> Frenquence= new ArrayList<>(Arrays.asList("F_MINTE","F_HOUR","F_DAY","F_WEEK","F_MONTH"));

    //日期处理
    Calendar calendar= Calendar.getInstance();
    SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, String> setUpdateInfo = new HashMap<String, String>();//设置的文件更新信息  <文件路径,更新频率>
    List<String> slctFile = new ArrayList<>();//选择的文件
    List<String> DBFile ;//已经设置了的文件
    String FartherPath=System.getProperty("user.dir");//父级主目录

    //数据库操作对象
    DBoperator DBoper=new DBoperator();
    //后台控制总线对象
    Contrl ctr=new Contrl();

    public setFrame(){

        /*打开设置界面的时候 同时开启后台中线*/
        ctr.run_contrl();

        //获得已经设置过的文件
        DBFile=DBoper.getFilePath();
        System.out.println(DBFile);

        //默认工具集 并获得屏幕大小
        Toolkit kit =Toolkit.getDefaultToolkit();
        Dimension screenSize =kit.getScreenSize();

        //设置大小和位置
        this.setSize(600,500);
        this.setLocation((int)(screenSize.getHeight()/2),(int)(screenSize.getHeight()/8));

        //设置图标
        Image icon=new ImageIcon(FartherPath+"/icons/"+"icon2.png").getImage();
        this.setIconImage(icon);

        //设置风格
        try{
            String lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            //String lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            //String lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            UIManager.setLookAndFeel(lookAndFeel);
        }catch (Exception e){
            e.printStackTrace();
        }

        //设置关闭后主线继续运行
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //设置标题和布局
        this.setTitle("文件备份");
        this.setLayout(new BorderLayout());

        //总面板
        JPanel ctxPanel = new JPanel();
        setContentPane(ctxPanel);
        ctxPanel.setLayout(new BorderLayout());

        //信息面板
        JPanel showPanel =new JPanel();
        showPanel.setLayout(new FlowLayout());
        ctxPanel.add(showPanel,BorderLayout.NORTH);

        //信息面板————标签
        JLabel label2=new JLabel("已选：");
        showPanel.add(label2);

        //信息面板————文字区域
        JTextArea infoTextArea = new JTextArea(20,50);
        infoTextArea .setLineWrap(true);
        showPanel.add(infoTextArea);

        //选择面板
        JPanel slctPanel = new JPanel();
        ctxPanel.setLayout(new FlowLayout());
        ctxPanel.add(slctPanel,BorderLayout.CENTER);

        //选择面板————文字区域
        JTextArea msgTextArea = new JTextArea(1,30);
        msgTextArea.setSize(200,20);
        msgTextArea.setLineWrap(true);
        slctPanel.add(msgTextArea);

        //选择面板————选择文件按钮
        JButton openBtn = new JButton("打开");
        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileOpenDialog(msgTextArea,DBFile);
            }
        });
        slctPanel.add(openBtn);

        //选择面板————标签
        JLabel label=new JLabel("选择时间:");
        slctPanel.add(label);

        //选择面板————选择列表
        JComboBox freComboBox =new JComboBox();
        freComboBox.addItem("一分钟一次");
        freComboBox.addItem("一小时一次");
        freComboBox.addItem("一天一次");
        freComboBox.addItem("一周一次");
        freComboBox.addItem("一月一次");
        slctPanel.add(freComboBox);

        //选择面板————确认按钮
        JButton conBtn = new JButton("确定");
        slctPanel.add(conBtn);

        //确认按钮监听事件
        conBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //如果设置信息合法 更新设置信息
                if(confirmInfo(msgTextArea,freComboBox,infoTextArea,slctFile)){
                    String filepath=msgTextArea.getText();
                    String freType=Frenquence.get(freComboBox.getSelectedIndex());

                    slctFile.add(filepath);
                    setUpdateInfo.put(filepath,freType);
                }

            }
        });

        //设置确认面板
        JPanel finConPanel = new JPanel();
        finConPanel.setLayout(new FlowLayout());
        ctxPanel.add(finConPanel,BorderLayout.SOUTH);

        //确认面板————确认按钮
        JButton finConBtn = new JButton("确认设置");
        finConPanel.add(finConBtn);

        //确认按钮监听事件
        finConBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //把更新信息写入数据库
                if(setUpdateInfo.size()==0){
                    JOptionPane.showMessageDialog(null, "错误：未设置文件或重复设置");
                }else{
                    Iterator iter = setUpdateInfo.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();

                        DBoper.insertOneItem((String)entry.getKey(),(String)entry.getValue());
                        setUpdateInfo.remove(entry.getKey());
                    }

                    JOptionPane.showMessageDialog(null, "成功：写入"+slctFile.size()+"条数据");
                }
            }
        });

        this.setContentPane(ctxPanel);
        this.setVisible(true);
    }

    //选择文件（夹）
    private static void showFileOpenDialog(JTextArea msgTextArea,List DBFile) {
        JFileChooser fileChooser = new JFileChooser();

        //默认文件夹
        fileChooser.setCurrentDirectory(new File("."));
        //可以选择文件 和 文件夹
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        //不允许多选
        fileChooser.setMultiSelectionEnabled(false);

        //打开文件选择框
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();
            String AbslutPath=file.getAbsolutePath();
            if(DBFile.contains(AbslutPath)){
                JOptionPane.showMessageDialog(null, "错误 该文件已经设置过");
            }else{
                msgTextArea.setText(AbslutPath);
            }
        }

    }

    //确认信息
    private  static boolean confirmInfo(JTextArea msgTextArea,JComboBox freComboBox,JTextArea infoTextArea,List slctFile){

        if (msgTextArea.getText().length()!=0){
            if (slctFile.contains(msgTextArea.getText())) {
                JOptionPane.showMessageDialog(null, "文件重复");
                return false;

            } else {

                infoTextArea.append(msgTextArea.getText()+"\t"+freComboBox.getSelectedItem()+"\n");

                return true;
            }

        }else{
            JOptionPane.showMessageDialog(null, "文件为空");
            return false;
        }
    }

}

/****************************************************
 * 登录界面 框架
 *
 *  目前只允许 默认用户 root 1234
 *  注册功能为完善
 *  退出后直接退出 无后台
 */

class onloadFrame extends JFrame implements ActionListener{

    //定义登录界面的组件
    JButton btn1,btn2,btn3=null;
    JPanel jp1,jp2,jp3=null;
    JTextField username=null;
    JLabel jlb1,jlb2=null;
    JPasswordField password=null;


    public onloadFrame()
    {
        //创建组件
        //创建组件
        btn1=new JButton("登录");
        btn2=new JButton("注册");
        btn3=new JButton("退出");
        //设置监听
        btn1.addActionListener(this);
        btn2.addActionListener(this);
        btn3.addActionListener(this);

        jlb1=new JLabel("用户名：");
        jlb2=new JLabel("密    码：");

        username=new JTextField(10);
        password=new JPasswordField(10);

        jp1=new JPanel();
        jp2=new JPanel();
        jp3=new JPanel();

        jp1.add(jlb1);
        jp1.add(username);

        jp2.add(jlb2);
        jp2.add(password);

        jp3.add(btn1);
        jp3.add(btn2);
        jp3.add(btn3);
        this.add(jp1);
        this.add(jp2);
        this.add(jp3);

        //默认工具集 并获得屏幕大小
        Toolkit kit =Toolkit.getDefaultToolkit();
        Dimension screenSize =kit.getScreenSize();


        //设置图标
        Image icon=new ImageIcon(System.getProperty("user.dir")+"/icons/"+"icon2.png").getImage();
        setIconImage(icon);

        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("注册登录界面");
        this.setLayout(new GridLayout(3,1));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300,200);
        this.setLocation((int)(screenSize.getWidth()/2)-150,(int)(screenSize.getHeight()/2)-100);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //监听各个按钮
        if(e.getActionCommand()=="退出")
        {
            System.exit(0);
        }else if(e.getActionCommand()=="登录")
        {
            //调用登录方法
            this.login();
        }else if(e.getActionCommand()=="注册")
        {
            //调用注册方法
            this.Regis();
        }
    }

    //注册方法   未完善
    public void Regis() {
        JOptionPane.showMessageDialog(null, "抱歉，暂时不提供注册");
    }

    //登录方法
    public void login() {
        String user=username.getText();
        char[] pwd=password.getPassword();
        String pwdstr=String.valueOf(pwd);

        if(user.equals("root")&&pwdstr.equals("1234")) {
            System.out.println(username.getText());
            System.out.println(password.getPassword());
            this.dispose();
            new setFrame();
        }
        else{
            JOptionPane.showMessageDialog(null, "用户名/密码不匹配");
        }

    }

}


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.DBoperator.DBoperator;
import com.contrl.Contrl;
import javafx.scene.control.Tab;


/**
 * @ClassName: ClientApp
 * @Description: 软件的主体
 *               包括登录界面 和添加设置界面
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


class mainFrame extends JFrame{

    //频率文字 与 选择框选择号的 映射
    final static List<String> Frenquence= new ArrayList<>(Arrays.asList("F_MINTE","F_HOUR","F_DAY","F_WEEK","F_MONTH"));
    private final int COLUMN = 2;//表格的列数
    private final List<String> TITLES = Arrays.asList("文件","更新频率");//表格表头
    private Vector<Vector<String>> dataModel = new Vector<Vector<String>>();

    String FartherPath=System.getProperty("user.dir");//父级主目录
    //日期处理
    Calendar calendar= Calendar.getInstance();
    SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, String> setUpdateInfo = new HashMap<String, String>();//新添加设置的文件更新信息  <文件路径,更新频率>
    Map<String, String> oldInfo = new HashMap<>();//原有设置的

    List<String> slctFile = new ArrayList<>();//选择的文件
    Set<String> DBFile ;//已经设置了的文件


    //数据库操作对象
    DBoperator DBoper=new DBoperator();

    //后台总线
    Contrl ctl = new Contrl();

    public mainFrame(){

        //后台总线开始运行
        ctl.run_contrl();


        //获得已经设置过的文件信息
        oldInfo=DBoper.getFile_FreSeted();
        DBFile=oldInfo.keySet();//DBfile提取其中的文件信息
        System.out.println(oldInfo);
        System.out.println(DBFile);

        /*frame初始化*/
        init();

        /*总面板*/
        JPanel AllPanel = new JPanel();
        //总体采用卡片布局 可以切换add界面和set界面
        CardLayout card =new CardLayout();
        AllPanel.setLayout(card);
        this.setContentPane(AllPanel);

        /*添加界面*/
        JPanel addPanel = new JPanel();
        //addPanel.setBackground(Color.red);
        addPanel.setLayout(new FlowLayout());

        //信息区域
        JPanel infoPanel = new JPanel();
        addPanel.add(infoPanel);
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        //信息区域--标签
        JLabel label2=new JLabel();
        label2.setIcon(new ImageIcon(FartherPath+"/icons/"+"add-folder .png"));
        infoPanel.add(label2);

        //信息区域--文字框
        JTextArea infoTextArea = new JTextArea(20,55);
        infoTextArea.setEditable(false);
        infoTextArea .setLineWrap(true);
        infoPanel.add(infoTextArea);

        //选择面板
        JPanel slctPanel = new JPanel();
        slctPanel.setLayout(new FlowLayout());
        addPanel.add(slctPanel);

        //选择区域————文字框
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
                //更新设置信息
                confirmInfo(msgTextArea,freComboBox,infoTextArea,slctFile);
            }
        });

        //设置确认及转换面板
        JPanel finConPanel = new JPanel();
        finConPanel.setLayout(new FlowLayout());
        addPanel.add(finConPanel);

        //设置确认及转换面板————确认按钮
        JButton finConBtn = new JButton("确认添加");
        finConPanel.add(finConBtn);

        //确认按钮监听事件
        finConBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addinfo2DB(msgTextArea,infoTextArea);
            }
        });

        //设置确认及转换面板————转换按钮
        JButton changeBtn =new JButton("设置已有的");
        finConPanel.add(changeBtn);
        changeBtn.setIcon(new ImageIcon(FartherPath+"/icons/"+"set_16.png"));

        changeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //更新信息
                flush_Map2Vector();
                //跳转页面
                card.show(AllPanel,"set");
            }
        });


        /*设置界面*/
        JPanel setPanel = new JPanel();
        setPanel.setLayout(new BorderLayout());

        //返回按钮及刷新界面
        JPanel backBtnPanel = new JPanel();
        backBtnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        setPanel.add(backBtnPanel,BorderLayout.NORTH);


        //返回按钮及刷新界面————返回按钮
        JButton backBtn = new JButton("返回添加界面");
        backBtn.setIcon(new ImageIcon(FartherPath+"/icons/"+"back_16.png"));
        backBtn.setContentAreaFilled(false);//取消填充
        backBtnPanel.add(backBtn);

        //返回按钮监听事件————切换为添加界面
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //转换页面
                card.show(AllPanel,"add");
            }
        });
        //返回按钮及刷新界面————刷新按钮
        JButton flushBtn = new JButton("刷新");
        backBtnPanel.add(flushBtn);


        //信息表面板
        JPanel showPanel = new JPanel();
        showPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        setPanel.add(showPanel,BorderLayout.CENTER);

        //信息表面板————标签
        JLabel label3=new JLabel();
        label3.setIcon(new ImageIcon(FartherPath+"/icons/"+"list100.png"));
        showPanel.add(label3);

        //信息表面板————表格
        Vector<String> titles = new Vector<String>(TITLES);
        flush_Map2Vector();//把map信息转化为Vector信息以显示
        JTable table = new JTable(dataModel, titles){public boolean isCellEditable(int row, int column) { return false; }};//创建table并设置不可写
        table.getColumnModel().getColumn(0).setPreferredWidth(300);//设置列宽
        table.getColumnModel().getColumn(1).setPreferredWidth(20);
        JScrollPane scrollPane = new JScrollPane(table);//把table装进可滚动容器内
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        showPanel.add(scrollPane);


        //上一层 刷新按钮
        flushBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flush_Map2Vector();
                table.validate();
                table.updateUI();
            }
        });

        //修改和删除按钮
        JPanel fixDelPanel=new JPanel();
        fixDelPanel.setLayout(new FlowLayout());
        setPanel.add(fixDelPanel,BorderLayout.SOUTH);

        //修改和删除按钮————显示框
        JTextArea dealTextArea = new JTextArea(1,30);
        dealTextArea .setSize(200,20);
        dealTextArea .setLineWrap(true);
        dealTextArea.setEditable(false);
        fixDelPanel.add(dealTextArea );

        //修改和删除按钮————删除按钮
        JButton delBtn = new JButton("删除");
        fixDelPanel.add(delBtn);



        //修改和删除按钮————修改提示标签
        JLabel label4 = new JLabel("修改为:");
        fixDelPanel.add(label4);


        //修改和删除按钮————修改复选框
        JComboBox fixComboBox =new JComboBox();
        fixComboBox.addItem("一分钟一次");
        fixComboBox.addItem("一小时一次");
        fixComboBox.addItem("一天一次");
        fixComboBox.addItem("一周一次");
        fixComboBox.addItem("一月一次");
        fixDelPanel.add(fixComboBox);

        //修改和删除按钮————修改确认框
        JButton fixBtn = new JButton("确认修改");
        fixDelPanel.add(fixBtn);


        //设置table的鼠标事件
        table.addMouseListener(new MouseAdapter() {    //鼠标事件
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow(); //获得选中行索引
                dealTextArea.setText(table.getValueAt(selectedRow,0).toString());
            }
        });

        //删除按钮监听事件
        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DelItem(dealTextArea,table);
            }
        });

        //修改按钮监听事件
        fixBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fixItem(dealTextArea,fixComboBox,table);
            }
        });

        /*添加两个界面到card布局中*/
        AllPanel.add("add",addPanel);
        AllPanel.add("set",setPanel);
    }

    public void init(){
        //默认工具集 并获得屏幕大小
        Toolkit kit =Toolkit.getDefaultToolkit();
        Dimension screenSize =kit.getScreenSize();

        //设置大小和位置
        this.setSize(600,520);
        this.setLocation((int)(screenSize.getWidth()/2-300),(int)(screenSize.getHeight()/2-260));

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

        this.setResizable(false);

        //设置标题和布局
        this.setTitle("文件备份");
        this.setVisible(true);

        //关闭窗口提醒
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                JOptionPane.showMessageDialog(null, "注意！关闭后台程序继续运行");
            }
        });
    }

    //数据结构转换并更新数据
    public void flush_Map2Vector(){
        //更新设置了的信息
        oldInfo.clear();
        oldInfo=DBoper.getFile_FreSeted();
        DBFile=oldInfo.keySet();

        //转换并更新table显示的数据
        dataModel.removeAllElements();
        Iterator iter = oldInfo.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            dataModel.add(new Vector<String>(Arrays.asList((String) entry.getKey(),(String) entry.getValue())));
        }
    }


    /*监听事件函数*/

    //选择文件（夹）
    private static void showFileOpenDialog(JTextArea msgTextArea,Set DBFile) {
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

    //确认添加文件信息
    private   boolean confirmInfo(JTextArea msgTextArea,JComboBox freComboBox,JTextArea infoTextArea,List slctFile){

        if (msgTextArea.getText().length()!=0){
            if (slctFile.contains(msgTextArea.getText())) {
                JOptionPane.showMessageDialog(null, "文件重复");
                return false;

            } else {

                infoTextArea.append(msgTextArea.getText()+"\t"+freComboBox.getSelectedItem()+"\n");

                String filepath=msgTextArea.getText();
                String freType=Frenquence.get(freComboBox.getSelectedIndex());

                slctFile.add(filepath);
                setUpdateInfo.put(filepath,freType);
                return true;
            }

        }else{
            JOptionPane.showMessageDialog(null, "文件为空");
            return false;
        }
    }

    //把添加的信息写入到数据库
    public void addinfo2DB(JTextArea msgTextArea,JTextArea infoTextArea){
        if(setUpdateInfo.size()==0){
            JOptionPane.showMessageDialog(null, "错误：未设置文件或重复设置");
        }else{
            Iterator iter = setUpdateInfo.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();

                //插入到数据库
                DBoper.insertOneItem((String)entry.getKey(),(String)entry.getValue());

                //更新显示

                msgTextArea.setText("");
                infoTextArea.setText("");
                setUpdateInfo.remove(entry.getKey());
            }

            JOptionPane.showMessageDialog(null, "成功：写入"+slctFile.size()+"条数据");
            slctFile.clear();
        }
    }

    //删除已设置文件
    private boolean DelItem(JTextArea dealTextArea,JTable table){
        String delFile=dealTextArea.getText();
        if(delFile.length()!=0){
            int n = JOptionPane.showConfirmDialog(null,"确认删除？","确认对话框", JOptionPane.YES_NO_OPTION);
            if(n==JOptionPane.YES_OPTION){
                oldInfo.remove(delFile);

                //数据库删除
                DBoper.deloneItem(delFile);

                //更新数据
                flush_Map2Vector();
                table.validate();
                table.updateUI();

                dealTextArea.setText("");
                return true;
            }
            else if(n==JOptionPane.NO_OPTION){
                return false;
            }
        }else{
            JOptionPane.showMessageDialog(null, "无内容");
            return false;
        }

        return false;
    }

    //修改已设置文件的更新频率
    private boolean fixItem(JTextArea dealTextArea,JComboBox fixComboBox,JTable table){
        String fixFile=dealTextArea.getText();
        int setFreq=fixComboBox.getSelectedIndex();
        String setFreqStr = Frenquence.get(setFreq);

        if(fixFile.length()!=0){
            int n = JOptionPane.showConfirmDialog(null,"确认修改为？"+setFreqStr,"确认对话框", JOptionPane.YES_NO_OPTION);
            if(n==JOptionPane.YES_OPTION){
                oldInfo.put(fixFile,setFreqStr);

                //数据库修改
                DBoper.updateOneItem(fixFile,setFreqStr);

                //更新数据
                flush_Map2Vector();
                table.validate();
                table.updateUI();

                dealTextArea.setText("");
                return true;
            }
            else if(n==JOptionPane.NO_OPTION){
                return false;
            }
        }else{
            JOptionPane.showMessageDialog(null, "无内容");
            return false;
        }

        return false;
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

    private final String uname="root";
    private final String upwd="1234";

    //定义登录界面的组件
    JButton btn1,btn2=null;
    JPanel jp1,jp2,jp3=null;
    JTextField username=null;
    JLabel jlb1,jlb2=null;
    JPasswordField password=null;


    public onloadFrame()
    {
        //初始化
        init();

        //创建组件
        btn1=new JButton("登录");
        btn2=new JButton("退出");
        //设置监听
        btn1.addActionListener(this);
        btn2.addActionListener(this);

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
        this.add(jp1);
        this.add(jp2);
        this.add(jp3);


    }
    public void init(){
        //默认工具集 并获得屏幕大小
        Toolkit kit =Toolkit.getDefaultToolkit();
        Dimension screenSize =kit.getScreenSize();


        //设置图标
        Image icon=new ImageIcon(System.getProperty("user.dir")+"/icons/"+"icon2.png").getImage();
        setIconImage(icon);

        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("欢迎登录自动备份工具");
        this.setLayout(new GridLayout(3,1));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(340,220);
        this.setLocation((int)(screenSize.getWidth()/2)-170,(int)(screenSize.getHeight()/2)-110);
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
        }
    }

    //登录方法
    public void login() {
        String user=username.getText();
        char[] pwd=password.getPassword();
        String pwdstr=String.valueOf(pwd);

        if(user.equals(uname)&&pwdstr.equals(upwd)) {
            System.out.println(username.getText());
            System.out.println(password.getPassword());
            this.dispose();
            new mainFrame();
        }
        else{
            JOptionPane.showMessageDialog(null, "用户名/密码不匹配");
        }

    }
}


package views;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.util.ImageUtils;
import reUI.MyComboBoxUI;

/**
 * 重写下拉菜单方法
 * @author CuiQinPro
 *
 */
class SelectedListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus){
        Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        if(isSelected){
            c.setBackground(Color.GRAY);
            c.setForeground(Color.WHITE);
        }
        return c;
    }
}

public class Window extends JFrame{
    private final Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
    //TODO 获取所有摄像头设备的名称并提供给下拉菜单，实现摄像头切换
    private List<Webcam> webcams;
    private VideoCam webcam;
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {//使事件派发线程上的可运行对象排队。当可运行对象排在事件派发队列的队首时，就调用其run方法。其效果是允许事件派发线程调用另一个线程中的任意一个代码块。只有从事件派发线程才能更新组件。
            public void run() {
                try {
                    List<Webcam> webcams = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS);
                    new Window(webcams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Window(List<Webcam> webcams) {
        this.webcam = new VideoCam(webcams); //默认设备
        this.setTitle("趣味哈哈镜 vbeta:0.1 by Derek");
        int x=100,y=120,width=1186,height=600;
        this.setBounds(x,y,width,height);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Color.white);
        this.setResizable(false);
        this.setLayout(null);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e)//关闭时的操作，windowClosed是关闭后的操作
            {
                dispose();
                System.out.println("退出程序");
            }
        });
        setOpenAndHelp(this);
        JPanel jPanel = setOriginVideo(this);
        JPanel jPanel1 = updateVideo(this);
        basicMagic(this);
        hahaMagic(this);
        cutImage(this);
        makeVideo(this);
        chooseDeviceLabel(this,jPanel,jPanel1);
        setOptions(this);
        this.setVisible(true);
    }

    private void setOptions(Frame frame) {
        String setIcoPath = "set.png";
        JButton setBtn = new JButton();
        setBtn.setBounds(1100,500,60,60);
        ImageIcon setIco = new ImageIcon(setIcoPath);
        setIco.setImage(setIco.getImage().getScaledInstance(30,30,0));
        setBtn.setIcon(setIco);
        setBtn.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        setBtn.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        setBtn.setBorderPainted(false);//不打印边框
        setBtn.setBorder(null);//除去边框
        setBtn.setText(null);//除去按钮的默认名称
        setBtn.setFocusPainted(false);//除去焦点的框
        setBtn.setContentAreaFilled(false);//除去默认的背景填充
        setBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击视频录制按钮，执行功能");
                defaultFrame("设置");
            }
        });
        frame.add(setBtn);
    }

    private void chooseDeviceLabel(final Frame frame,final JPanel jPanel, final JPanel jPanel2) {
        JLabel jL = new JLabel("请选择摄像头设备：");
        jL.setBounds(600, 520, 120, 20);
        frame.add(jL);

        final String[] deviceNames = this.webcam.getDevice_names();
        final JComboBox jComboBox = new JComboBox(deviceNames);
        jComboBox.setUI(new MyComboBoxUI());
        jComboBox.setBounds(730, 520, 120, 20);
        jComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == e.SELECTED){
                    webcam.getWebcam().close(); //关闭摄像头
                    System.out.println("复选框选择:"+jComboBox.getSelectedItem());
                    String choosed = (String)jComboBox.getSelectedItem();
                    webcam.setWebcam(webcam.getWebcamByName(choosed)); // 重新设置摄像头
                    setOriginVideo(frame);
                    updateVideo(frame);
                    frame.setVisible(true);
                }
            }
        });
        frame.add(jComboBox);
    }

    private void makeVideo(Frame frame) {
        String stopIcoPath = "stop.png";
        String startIcoPath = "start.png";
        final boolean[] playStatus = {true};
        final JButton videoBtn = new JButton();
        videoBtn.setBounds(470,500,60,60);
        final ImageIcon stopIco = new ImageIcon(stopIcoPath);
        final ImageIcon startIco = new ImageIcon(startIcoPath);
        stopIco.setImage(stopIco.getImage().getScaledInstance(50,50,0));
        startIco.setImage(startIco.getImage().getScaledInstance(50,50,0));
        videoBtn.setIcon(startIco);
        videoBtn.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        videoBtn.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        videoBtn.setBorderPainted(false);//不打印边框
        videoBtn.setBorder(null);//除去边框
        videoBtn.setText(null);//除去按钮的默认名称
        videoBtn.setFocusPainted(false);//除去焦点的框
        videoBtn.setContentAreaFilled(false);//除去默认的背景填充
        videoBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击视频录制按钮，执行功能");
                System.out.println("第一次执行按钮"+playStatus[0]);
                if(playStatus[0]) {//播放状态
                    videoBtn.setIcon(stopIco);
                    playStatus[0] = false;
                } else {
                    videoBtn.setIcon(startIco);
                    playStatus[0] = true;
                }

            }
        });
        frame.add(videoBtn);
    }

    private void cutImage(Frame frame) {
        String cutIcoPath = "cut.png";
        final JButton cutBtn = new JButton();
        cutBtn.setBounds(100,500,60,60);
        ImageIcon cutIco = new ImageIcon(cutIcoPath);
        cutIco.setImage(cutIco.getImage().getScaledInstance(30,30,0));
        cutBtn.setIcon(cutIco);
        cutBtn.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        cutBtn.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        cutBtn.setBorderPainted(false);//不打印边框
        cutBtn.setBorder(null);//除去边框
        cutBtn.setText(null);//除去按钮的默认名称
        cutBtn.setFocusPainted(false);//除去焦点的框
        cutBtn.setContentAreaFilled(false);//除去默认的背景填充
        cutBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击视截图功能按钮，执行功能");
                cutBtn.setEnabled(false);
                final String formatter = ImageUtils.FORMAT_PNG;
                String configSavePath = "D://"; //从程序中获取
                final String imgSavePath = configSavePath + "test";
                final BufferedImage img = webcam.getWebcam().getImage();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        cutImageFrame("截图",img,imgSavePath,formatter);
                        cutBtn.setEnabled(true);
                    }
                });
            }
        });
        frame.add(cutBtn);
    }

    private void hahaMagic(Frame frame) {
        /***********哈哈镜特效**************/
        JPanel jpNew = new JPanel();
        jpNew.setLayout(null);
        jpNew.setBounds(1000, 300, 150, 200);
        jpNew.setBackground(Color.white);
        jpNew.setBorder(BorderFactory.createTitledBorder("哈哈镜特效："));

        String[] showNew = {"鬼影特效","中心内凹特效","中心外凸特效","浮雕特效","美颜特效"};
        @SuppressWarnings("unchecked")
        final JList jShowList2 = new JList(showNew);
        jShowList2.setCellRenderer(new SelectedListCellRenderer());
        jShowList2.setBounds(5, 15, 140, 100);
        jShowList2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("选择了"+jShowList2.getSelectedValue()+"功能");
            }
        });
        jpNew.add(jShowList2);
        frame.add(jpNew);
    }

    private void basicMagic(Frame frame) {
        /***********基本特效**************/
        JPanel jpBasic = new JPanel();
        jpBasic.setLayout(null);
        jpBasic.setBounds(1000, 20, 150, 250);
        jpBasic.setBackground(Color.white);
        jpBasic.setBorder(BorderFactory.createTitledBorder("视频基本特效："));

        String[] showBasic = {"渐入","黑白","锐化","反锐化","渐晕","模糊处理","水平翻转","垂直翻转"};

        @SuppressWarnings("unchecked")
        final JList jShowList1 = new JList(showBasic);
        jShowList1.setCellRenderer(new SelectedListCellRenderer());
        jShowList1.setBounds(5, 15, 140, 190);
        jShowList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("选择了"+jShowList1.getSelectedValue()+"功能");
            }
        });

        jpBasic.add(jShowList1);
        frame.add(jpBasic);
    }

    private JPanel updateVideo(Frame frame) {
        JPanel jpNewVideo = new JPanel();
        jpNewVideo.setLayout(null);
        jpNewVideo.setBounds(500, 20, 480, 480);
        jpNewVideo.setBackground(Color.white);
        jpNewVideo.setBorder(BorderFactory.createTitledBorder("变换视频："));

        /*****特效视频*****/
        JPanel newVideo = new JPanel();
        newVideo.setBounds(5, 15, 470, 460);
        newVideo.add(getWebCamJustPanel());
        jpNewVideo.add(newVideo);
        frame.add(jpNewVideo);
        return jpNewVideo;
    }

    private void setOpenAndHelp(Frame frame) {
        String[] normailBtns = {"Open File","Need Help","Take me a coffee☕"};
        for(int i=0;i<normailBtns.length;i++) {
            frame.add(getNormailBtns(i*150,0,150,20,normailBtns[i]));
        }
    }

    private WebcamPanel getWebCamPanel(Dimension dimension) {
        webcam.getWebcam().setViewSize(dimension);
        return new WebcamPanel(webcam.getWebcam());
    }

    private WebcamPanel getWebCamJustPanel() {
        return new WebcamPanel(webcam.getWebcam());
    }

    private JPanel setOriginVideo(Frame frame) {
        /****************原始视频****************/
        JPanel jpOldVideo = new JPanel();
        jpOldVideo.setLayout(null);
        jpOldVideo.setBounds(20, 20, 480, 480);//长宽450，下面那个x就需要加上这个宽度
        jpOldVideo.setBackground(Color.white);
        jpOldVideo.setBorder(BorderFactory.createTitledBorder("原始视频："));

        /*****原始视频*****/
        JPanel oriVideo = new JPanel();
        oriVideo.setBounds(5, 15, 470, 460);
        oriVideo.setBackground(Color.white);

        oriVideo.add(getWebCamPanel(new Dimension(640,480)));
        jpOldVideo.add(oriVideo);
        frame.add(jpOldVideo);
        return jpOldVideo;
    }

    private JButton getNormailBtns(int i, int j, int k, int l, final String str) {
        JButton JBtn = new JButton(str);
        JBtn.setBounds(i,j,k,l);
        JBtn.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        JBtn.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        JBtn.setBorderPainted(false);//不打印边框
        JBtn.setBorder(null);//除去边框
        JBtn.setFocusPainted(false);//除去焦点的框
        JBtn.setContentAreaFilled(false);//除去默认的背景填充
        JBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击调用");
                defaultFrame(str);
            }
        });
        return JBtn;
    }

    /**
     * 点击按钮展示的新窗口 + 图片
     * @param name
     */
    public void cutImageFrame(String name, BufferedImage img, final String savePath, final String formatter) {
        ImageIcon imageIcon = new ImageIcon(img);
        final BufferedImage saveImage = img;

        JButton saveBtn = new JButton("保存这张图片   √");
        saveBtn.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        saveBtn.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        saveBtn.setBorderPainted(false);//不打印边框
        saveBtn.setBorder(null);//除去边框
        saveBtn.setFocusPainted(false);//除去焦点的框
        saveBtn.setContentAreaFilled(false);//除去默认的背景填充
        Font font=new Font("微软雅黑",Font.BOLD,32);//根据指定字体名称、样式和磅值大小,创建一个新 Font。
        saveBtn.setFont(font);

        JFrame jF = new JFrame(name);
        jF.add(new JLabel(imageIcon));
        jF.add(saveBtn, BorderLayout.SOUTH);
        jF.pack();
        Dimension framesize = jF.getSize();
        int x = (int)screensize.getWidth()/2 - (int)framesize.getWidth()/2;
        int y = (int)screensize.getHeight()/2 - (int)framesize.getHeight()/2;
        jF.setLocation(x,y);
        jF.setVisible(true);

        saveBtn.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent e)
            {
                //保存照片
                try{
                    String path = savePath;
                    String ext = "." + formatter.toLowerCase();
                    if (!path.endsWith(ext)) {
                        path = savePath + ext;
                    }
                    ImageIO.write(saveImage,formatter,new File(path));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        JOptionPane.showMessageDialog(null, "保存成功");
                    }
                });
            }
        });
    }

    /**
     * 点击按钮展示的新窗口
     * @param name
     */
    public void defaultFrame(String name) {
        //创建新的窗口作为下拉框展示
        JFrame jF = new JFrame(name);
        jF.setLocation(500,300);
        jF.setSize(500,350);
        jF.setLayout(null);
        jF.setVisible(true);
    }
}

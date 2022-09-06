package views;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import demos.ObjectFile;
import entity.Status;
import event.StatusEvent;
import listener.StatusListener;
import net.sf.jasperreports.engine.util.JRBoxUtil;
import reUI.MyComboBoxUI;
import transformers.MakeVideo;
import transformers.TransformerUtils;
import webcapTest.FacePainterExample;

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
    //获取所有摄像头设备的名称并提供给下拉菜单，实现摄像头切换
    private List<Webcam> webcams;
    private VideoCam webcam;
    private String[] supportImgFormat = {"gif","png","jpg","bmp","wbmp"};
    private String[] supportVdoFormat = {"ts","mp4","mov","avi","flv","mkv"};
    private ICodec.ID[] videoCodec = {ICodec.ID.CODEC_ID_H264,ICodec.ID.CODEC_ID_MPEG4,ICodec.ID.CODEC_ID_FLV1,};
    private String saveFilePath = "file.dat";
    // 以下都是在下一次打开时需要读取的
    private int videoTypeIndex = 0;
    private int imgTypeIndex = 0;
    // 为当前设置，下一次打开时需要
    private String currentImgType = "png";
    private String currentImgPath = "D:\\";
    private String currentVdoType = "mp4";
    private String currentVdoPath = "D:\\";

    private Status curStatus;

    public static void main(String[] args) throws IOException, ClassNotFoundException, TimeoutException {
        List<Webcam> webcams = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS);
        new Window(webcams);
//        EventQueue.invokeLater(new Runnable() {//使事件派发线程上的可运行对象排队。当可运行对象排在事件派发队列的队首时，就调用其run方法。其效果是允许事件派发线程调用另一个线程中的任意一个代码块。只有从事件派发线程才能更新组件。
//            public void run() {
//                try {
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private Window(List<Webcam> webcams) throws IOException, ClassNotFoundException{
        //TODO last：整合人脸识别，美颜相机的功能，添加帮助文档、赞赏方法和产品相关信息
        //TODO fin：调整界面UI设计，书写文档
        curStatus = new Status(false, null,false, true, false);  // 当前状态
        //读取配置
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        ObjectFile serializable = new ObjectFile();
        File file = new File(saveFilePath);
        if(file.exists()) {
            Object[] saveOption = (Object[]) serializable.load(new FileInputStream(file));
            currentImgPath = (String)saveOption[0];
            currentImgType = (String)saveOption[1];
            currentVdoPath = (String)saveOption[2];
            currentVdoType = (String)saveOption[3];
        }

        construction(webcams);
        try {
            startVideoRecording(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造界面
     * @param webcams
     * @return
     */
    private Webcam construction(List<Webcam> webcams) {
        this.webcam = new VideoCam(webcams); //默认设备
        this.setTitle("趣味哈哈镜 v1.0 by CQ");
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
        setVideo(this);
        basicMagic(this);
        hahaMagic(this);
        cutImage(this);
        makeVideo(this);
        checkFace(this);
        adjustStatus(this);
        chooseDeviceLabel(this);
        setOptions(this);
        this.setVisible(true);
        webcam.getWebcam().open();
        return this.webcam.getWebcam();
    }

    /**
     * 设置窗口
     * @param frame
     */
    private void setOptions(JFrame frame) {
        JButton setBtn = new JButton();
        setBtn.setBounds(1100,500,60,60);
        btnStyle(setBtn,"set.png");
        setBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                optionFrame("设置");
            }
        });
        frame.add(setBtn);
    }

    /**
     * 选择设备
     * @param frame
     */
    private void chooseDeviceLabel(final JFrame frame) {
        JLabel jL = new JLabel("请选择摄像头设备：");
        jL.setBounds(600, 520, 120, 20);
        frame.add(jL);

        final String[] deviceNames = this.webcam.getDevice_names();
        final JComboBox jComboBox = new JComboBox(deviceNames);
        MyComboBoxUI ui = new MyComboBoxUI();
        jComboBox.setUI(ui);
        jComboBox.setBounds(730, 520, 120, 20);
        jComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == e.SELECTED){
                    webcam.getWebcam().close(); //关闭摄像头
                    String choosed = (String)jComboBox.getSelectedItem();
                    webcam.setWebcam(webcam.getWebcamByName(choosed)); // 重新设置摄像头
                    setVideo(frame);
                    frame.setVisible(true);
                }
            }
        });
        frame.add(jComboBox);
    }


    private final JButton videoBtn = new JButton();
    private String stopIcoPath = "stop.png";
    private String startIcoPath = "start.png";
    private String stopIcoPath2 = "stop2.png";
    private final ImageIcon stopIco = new ImageIcon(stopIcoPath);
    private final ImageIcon startIco = new ImageIcon(startIcoPath);
    private final ImageIcon stopIco2 = new ImageIcon(stopIcoPath2);
    private final boolean[] playStatus = {false,false};

    /**
     * 调整按钮状态
     * @param frame
     */
    private void adjustStatus(JFrame frame) {
        videoBtn.setBounds(470,500,60,60);
        stopIco.setImage(stopIco.getImage().getScaledInstance(50,50,0));
        startIco.setImage(startIco.getImage().getScaledInstance(50,50,0));
        stopIco2.setImage(stopIco2.getImage().getScaledInstance(50, 50, 0));
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
                if(playStatus[1]) {
                    //录制视频
                    playStatus[1] = false;
                    statusListener.updateEvent(new StatusEvent(this, playStatus[1]));
                    videoBtn.setIcon(startIco);
                    makeBtn.setEnabled(true);  // 恢复录制视频按钮
                    return;
                }
                if(!playStatus[0] && !curStatus.isCurStatus()) {
                    //未选择特效
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            JOptionPane.showMessageDialog(null, "没有选择特效!");
                        }
                    });
                    videoBtn.setIcon(startIco);
                    playStatus[0] = false;
                } else {
                    if (!playStatus[0]) { // 哈哈镜的触发状态
                        //恢复哈哈镜的特效
                        System.out.println(curStatus.getCurEffect());
                        new TransformerUtils().changeTransformer(
                                webcam.getWebcam(),
                                curStatus.getCurEffect(),
                                curStatus.isTest(),  // 在录制视频时置为false
                                curStatus.isHaha()
                        );
                        videoBtn.setIcon(stopIco);
                        playStatus[0] = true;
                    } else {  // 哈哈镜子的原始状态
                        //清空当前的特效状态
                        webcam.getWebcam().setImageTransformer(null);
                        videoBtn.setIcon(startIco);
                        playStatus[0] = false;
                    }
                }

            }
        });
        frame.add(videoBtn);
    }

    /**
     * 截图
     * @param frame
     */
    private void cutImage(JFrame frame) {
        final JButton cutBtn = new JButton();
        cutBtn.setBounds(100,500,60,60);
        btnStyle(cutBtn,"cut.png");
        cutBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击视截图功能按钮，执行功能");
                cutBtn.setEnabled(false);
                final BufferedImage img = webcam.getWebcam().getImage();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        cutImageFrame("截图",img);
                        cutBtn.setEnabled(true);
                    }
                });
            }
        });
        frame.add(cutBtn);
    }


    private boolean isgoing = false;
    /**
     * 监听录制按钮
     */
    private StatusListener statusListener = new StatusListener() {
        @Override
        public void updateEvent(StatusEvent dm) {
            System.out.println("监听:"+playStatus[1]);
            boolean status = playStatus[1];
            if(status) {
                System.out.println("开始录制");
                curStatus.setTest(false);
                isgoing = true;
            } else {
                System.out.println("停止录制");
                curStatus.setTest(true);
                isgoing = false;
            }
        }
    };

    /**
     * 制作视频
     * @param frame
     */
    private final JButton makeBtn = new JButton();
    private long start = System.currentTimeMillis();
    private void makeVideo(JFrame frame) {
        makeBtn.setBounds(200,495,70,70);
        btnStyle(makeBtn,"MakeVideo.png");
        makeBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击视频录制按钮，执行功能");
                makeBtn.setEnabled(false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        videoBtn.setIcon(stopIco2);
                        playStatus[1] = true;
                        curStatus.setTest(false);
                        new TransformerUtils().changeTransformer(
                                webcam.getWebcam(),
                                curStatus.getCurEffect(),
                                curStatus.isTest(),  // 在录制视频时置为false
                                curStatus.isHaha()
                        );
                        statusListener.updateEvent(new StatusEvent(this, playStatus[1]));
                        makeBtn.setEnabled(false);
                    }
                });
            }
        });
        frame.add(makeBtn);
    }

    /**
     * 人脸识别
     * @param frame
     */
    private boolean face = false;
    private FacePainterExample example;
    private void checkFace(JFrame frame) {
        JButton faceBtn = new JButton();
        faceBtn.setBounds(300,495,70,70);
        btnStyle(faceBtn,"face1.png");
        faceBtn.addActionListener(new ActionListener() { //监听事件
            public void actionPerformed(ActionEvent e) {
                System.out.println("用户点击人脸检测，执行功能");
                if(face) {
                    //代表关闭这个功能
                    btnStyle(faceBtn,"face1.png");
                    face = false;
                    example.shutdown();
                    webcamPanel.stop();
                    webcam.getWebcam().close();
                    webcam.getWebcam().open();
                    webcamPanel.start();
                } else {
                    //代表打开这个功能
                    btnStyle(faceBtn,"face2.png");
                    face = true;
                    try {
                        webcam.getWebcam().close();
                        webcamPanel.stop();
                        example = new FacePainterExample(webcam.getWebcam(), webcamPanel);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        frame.add(faceBtn);
    }

    /**
     * 哈哈镜特效选择
     * @param frame
     */
    private void hahaMagic(JFrame frame) {
        /***********哈哈镜特效**************/
        JPanel jpNew = new JPanel();
        jpNew.setLayout(null);
        jpNew.setBounds(1000, 350, 150, 150);
        jpNew.setBackground(Color.white);
        jpNew.setBorder(BorderFactory.createTitledBorder("哈哈镜特效："));

        String[] showNew = {"中心内凹","纵向外凸","横向外凸","横向外凸（并行）","中心螺旋","中心外凹","复合特效"};
        @SuppressWarnings("unchecked")
        final JList jShowList2 = new JList(showNew);
        jShowList2.setCellRenderer(new SelectedListCellRenderer());
        jShowList2.setBounds(5, 15, 140, 120);
        jShowList2.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("选择了"+jShowList2.getSelectedValue()+"功能");
                // 如果按钮没有被点击,那么就点击按钮
                if(!playStatus[0]) {
                    videoBtn.setIcon(stopIco);
                    playStatus[0] = true;
                }
                curStatus.setCurEffect((String)jShowList2.getSelectedValue());
                curStatus.setCurStatus(true);
                curStatus.setHaha(true);
                new TransformerUtils().changeTransformer(
                        webcam.getWebcam(),
                        jShowList2.getSelectedValue(),
                        curStatus.isTest(),  // 在录制视频时置为false
                        curStatus.isHaha()
                );
            }
        });
        jpNew.add(jShowList2);
        frame.add(jpNew);
    }

    /**
     * 基本特效切换
     * @param frame
     */
    private void basicMagic(JFrame frame) {
        /***********基本特效**************/
        //点击切换特效
        JPanel jpBasic = new JPanel();
        jpBasic.setLayout(null);
        jpBasic.setBounds(1000, 20, 150, 320);
        jpBasic.setBackground(Color.white);
        jpBasic.setBorder(BorderFactory.createTitledBorder("视频基本特效："));

        String[] showBasic = {"水平翻转","垂直翻转","黑白相机","灰度1",
                "灰度2","高斯模糊","怀旧照片","珠纹化","油画","熔炉滤镜",
                "红色物体检测","马赛克","连环画","阴间滤镜","鬼影滤镜","美白"};

        @SuppressWarnings("unchecked")
        final JList jShowList1 = new JList(showBasic);
        jShowList1.setCellRenderer(new SelectedListCellRenderer());
        jShowList1.setBounds(5, 15, 140, 300);
        jShowList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("选择了"+jShowList1.getSelectedValue()+"功能");
                if(!playStatus[0]) {
                    videoBtn.setIcon(stopIco);
                    playStatus[0] = true;
                }
                curStatus.setCurEffect((String)jShowList1.getSelectedValue());
                curStatus.setCurStatus(true);
                curStatus.setHaha(false);
                new TransformerUtils().changeTransformer(
                        webcam.getWebcam(),
                        jShowList1.getSelectedValue(),
                        curStatus.isTest(),  // 在录制视频时置为false
                        curStatus.isHaha()
                );
            }
        });

        jpBasic.add(jShowList1);
        frame.add(jpBasic);
    }

    /**
     * 原始视频窗口
     * @param frame
     * @return
     */
    private WebcamPanel webcamPanel = null;
    private JPanel setVideo(JFrame frame) {
        /****************原始视频****************/
        JPanel jpOldVideo = new JPanel();
        jpOldVideo.setLayout(null);
        jpOldVideo.setBounds(20, 20, 980, 480);//长宽450，下面那个x就需要加上这个宽度
        jpOldVideo.setBackground(Color.white);
//        jpOldVideo.setBorder(BorderFactory.createTitledBorder("原始视频："));

        /*****原始视频*****/
        JPanel oriVideo = new JPanel();
        oriVideo.setBounds(5, 15, 970, 460);
        oriVideo.setBackground(Color.white);


        Dimension[] placeHolder = webcam.getWebcam().getViewSizes();   // 选择最大的尺寸作为输入

        webcamPanel = getWebCamPanelOrigin(placeHolder[placeHolder.length-1]);
        oriVideo.add(webcamPanel);
        oriVideo.setLayout(new GridLayout(1,1));
        jpOldVideo.add(oriVideo);
        frame.add(jpOldVideo);
        return jpOldVideo;
    }

    /**
     * 设置帮助窗口
     * @param frame
     */
    private void setOpenAndHelp(JFrame frame) {
        String[] normailBtns = {"Open File","Need Help","Take me a coffee☕"};
        for(int i=0;i<normailBtns.length;i++) {
            frame.add(getNormailBtns(i*150,0,150,20,normailBtns[i]));
        }
    }

    /**
     * 视频展示的webcampanel
     * @param dimension
     * @return
     */
    private WebcamPanel getWebCamPanelOrigin(Dimension dimension) {
        webcam.getWebcam().setCustomViewSizes(dimension);
        return new WebcamPanel(webcam.getWebcam());
    }

    /**
     * 创建按钮的统一样式
     * @param i
     * @param j
     * @param k
     * @param l
     * @param str
     * @return
     */
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
    private void cutImageFrame(String name, final BufferedImage img) {
        ImageIcon imageIcon = new ImageIcon(img);
        final BufferedImage saveImage = img;

        JButton saveBtn = new JButton("保存这张图片");
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
                File tmp = new File(currentImgPath);
                if(!tmp.exists()) tmp.mkdirs();
                JFileChooser fileChooser = new JFileChooser(currentImgPath);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                String out = new SimpleDateFormat("yyMMddhhmmss").format(new Date());
                fileChooser.setSelectedFile(new File("图片" +out+ "."+currentImgType));
                int returnVal = fileChooser.showOpenDialog(fileChooser);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    String filePath= fileChooser.getSelectedFile().getAbsolutePath();//这个就是选择的文件夹的路径
                    try{
                        File file = new File(filePath);
                        String choosedType = getImageType(file.getName());
                        if(choosedType==null) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(null, "不支持的文件类型！");
                                }
                            });
                            return;
                        }
                        ImageIO.write(saveImage,choosedType,new File(filePath));
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                JOptionPane.showMessageDialog(null, "保存成功");
                            }
                        });
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            JOptionPane.showMessageDialog(null, "请选择保存的文件地址");
                        }
                    });
                }
            }
        });
    }

    /**
     * 判断是否支持指定的格式
     * @param name
     * @return
     */
    private String getImageType(String name) {
        String t = name.substring(name.lastIndexOf(".")+1,name.length());
        System.out.println(t);
        for(String type:supportImgFormat) {
            if(t.equals(type))
                return type;
        }
        return null;
    }

    /**
     * 获取指定的视频编码格式
     * @param type
     * @return
     */
    public ICodec.ID getVideoTypeCode(String type) {
        switch (type) {
            case "ts":
            case "mp4":
            case "mov":
                return videoCodec[0];
            case "avi":
                return videoCodec[1];
            case "flv":
                return videoCodec[2];
            default:
                System.out.println("code error");
                return null;
        }
    }

    /**
     * 设置窗口
     * @param name
     */
    private void optionFrame(String name) {
        //创建新的窗口作为下拉框展示
        final JFrame jF = new JFrame(name);
        jF.getContentPane().setBackground(Color.white);
        jF.setResizable(false);
        jF.setLayout(null);
        jF.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jF.setLocation(450,280);
        jF.setSize(650,450);
        jF.setLayout(null);

        final JTextField imgText = imgSaveOption(jF);
        final JTextField vdoText = vdoSaveOption(jF);
        final JComboBox choosedImgType = imgSaveType(jF);
        final JComboBox choosedVdoType = vdoSaveType(jF);

        //继续设置
        final JButton saveBtn = new JButton("确定");
        saveBtn.setBounds(300, 300, 100, 100);
        btnStyle(saveBtn,"save.png");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentImgPath = imgText.getText();//图片保存路径
                currentImgType = (String)choosedImgType.getSelectedItem();//图片保存格式
                currentVdoPath = vdoText.getText();//视频保存路径
                currentVdoType = (String)choosedVdoType.getSelectedItem();//视频保存格式
                String[] saveOption = {currentImgPath,currentImgType,currentVdoPath,currentVdoType};
                //在此处实现持久化
                try {
                    ObjectFile serializable = new ObjectFile();
                    File file = new File(saveFilePath);
                    serializable.store(saveOption, new FileOutputStream(file));
                    System.out.println(serializable.load(new FileInputStream(file)));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
                jF.dispose();
            }
        });

        jF.add(saveBtn);
        jF.setVisible(true);
    }

    /**
     * 截图保存信息
     * @param frame
     * @return
     */
    private JTextField imgSaveOption(JFrame frame) {
        JButton imgBtn = new JButton();
        imgBtn.setBounds(100,40,90,30);
        btnStyle(imgBtn,"imgDir.png");

        final JLabel imageLabel = new JLabel("存储截图目录");
        imageLabel.setBounds(200,5,100,30);

        final JTextField imgText = new JTextField(currentImgPath);
        imgText.setBounds(200,40,240,30);
        imgText.setFocusable(false);
        imgText.setBackground(null);//与框架颜色一致
        imgText.setOpaque(false);//设为透明

        JButton chooseImgDir = new JButton("选择目录");
        chooseImgDir.setBounds(450,40,90,30);
        btnStyle(chooseImgDir,"dir.png");
        chooseImgDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(currentImgPath);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String out = new SimpleDateFormat("yyMMddhhmmss").format(new Date());
                fileChooser.setSelectedFile(new File("图片" +out+ ".png"));
                int returnVal = fileChooser.showOpenDialog(fileChooser);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    String filePath= fileChooser.getSelectedFile().getAbsolutePath();//这个就是选择的文件夹的路径
                    imgText.setText(filePath);
                }
                fileChooser.cancelSelection();
            }
        });
        //添加到窗体
        frame.add(imgBtn);frame.add(imageLabel);frame.add(imgText);frame.add(chooseImgDir);
        return imgText;
    }

    /**
     * 视频保存目录
     * @param frame
     * @return
     */
    private JTextField vdoSaveOption(final JFrame frame) {
        JButton vdoBtn = new JButton();
        vdoBtn.setBounds(100,110,90,30);
        btnStyle(vdoBtn,"videoDir.png");

        JLabel videoDirLabel = new JLabel("存储视频目录：");
        videoDirLabel.setBounds(200, 75, 100, 30);
        System.out.println("存储方法："+currentVdoPath);
        final JTextField videoDirText = new JTextField(currentVdoPath);
        videoDirText.setBounds(200, 110, 240, 30);
        videoDirText.setFocusable(false);
        videoDirText.setBackground(null);//与框架颜色一致
        videoDirText.setOpaque(false);//设为透明
        JButton chooseVideoDir = new JButton("选择目录");
        chooseVideoDir.setBounds(450, 110, 90, 30);
        btnStyle(chooseVideoDir,"dir.png");
        chooseVideoDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(currentVdoPath);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String out = new SimpleDateFormat("yyMMddhhmmss").format(new Date());
                fileChooser.setSelectedFile(new File("视频" +out+ ".mp4"));
                int returnVal = fileChooser.showOpenDialog(fileChooser);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    String filePath= fileChooser.getSelectedFile().getAbsolutePath();//这个就是选择的文件夹的路径
                    videoDirText.setText(filePath);
                }
            }
        });

        //添加到窗体
        frame.add(vdoBtn);frame.add(videoDirLabel);frame.add(videoDirText);frame.add(chooseVideoDir);
        return videoDirText;
    }

    /**
     * 按钮风格设置
     * @param button
     * @param icoPath
     */
    private void btnStyle(JButton button,String icoPath) {
        ImageIcon imgDirIco = new ImageIcon(icoPath);
        imgDirIco.setImage(imgDirIco.getImage().getScaledInstance(30,30,0));
        button.setIcon(imgDirIco);
        button.setMargin(new Insets(0,0,0,0));//将边框外的上下左右空间设置为0
        button.setIconTextGap(0);//将标签中显示的文本和图标之间的间隔量设置为0
        button.setBorderPainted(false);//不打印边框
        button.setBorder(null);//除去边框
        button.setText(null);//除去按钮的默认名称
        button.setFocusPainted(false);//除去焦点的框
        button.setContentAreaFilled(false);//除去默认的背景填充
    }

    /**
     * 图片保存类别
     * @param frame
     * @return
     */
    private JComboBox imgSaveType(Frame frame) {
        JLabel saveImgType = new JLabel("截图保存格式：");
        saveImgType.setBounds(200, 180, 130, 30);

        final JComboBox imgTypeLabel = new JComboBox(supportImgFormat);
        imgTypeLabel.setUI(new MyComboBoxUI());
        imgTypeLabel.setBounds(310, 180, 130, 30);

        //默认选择的属性
        for (int i = 0; i < supportImgFormat.length; i++) {
            if (supportImgFormat[i].equals(currentImgType)) {
                imgTypeIndex = i;
            }
        }
        imgTypeLabel.setSelectedIndex(imgTypeIndex);

        //添加到窗体
        frame.add(saveImgType);frame.add(imgTypeLabel);
        return imgTypeLabel;
    }

    /**
     * 视频保存格式
     * @param frame
     * @return
     */
    private JComboBox vdoSaveType(JFrame frame) {
        JLabel saveVdoType = new JLabel("视频保存格式：");
        saveVdoType.setBounds(200, 240, 130, 30);

        final JComboBox videoTypeLabel = new JComboBox(supportVdoFormat);
        videoTypeLabel.setUI(new MyComboBoxUI());
        videoTypeLabel.setBounds(310, 240, 130, 30);
        videoTypeLabel.setBorder(BorderFactory.createRaisedBevelBorder());

        //默认选择的属性
        for (int i = 0; i < supportVdoFormat.length; i++) {
            if (supportVdoFormat[i].equals(currentVdoType)) {
                videoTypeIndex = i;
            }
        }
        videoTypeLabel.setSelectedIndex(videoTypeIndex);

        //添加到窗体
        frame.add(saveVdoType);frame.add(videoTypeLabel);

        return videoTypeLabel;
    }

    /**
     * 默认面板格式
     * @param name
     */
    private void defaultFrame(String name) {
        //创建新的窗口作为下拉框展示
        JFrame jF = new JFrame(name);
        jF.setLocation(500,300);
        jF.setSize(500,350);
        jF.setLayout(null);
        jF.setVisible(true);
    }

    /**
     * 每被调用一次，就开始录制，直到特定条件停止，弹出窗口提示保存成功
     * @throws InterruptedException
     */
    private JLabel timeLabel = new JLabel("00:00:00");
    private void startVideoRecording(JFrame f) throws InterruptedException {
        while(true) {
            Dimension size = webcam.getWebcam().getViewSize();
            Webcam cam = webcam.getWebcam();
            if (isgoing) {
                System.out.println("进入录制程序");
                String filename = new SimpleDateFormat("yyMMddhhmmss").format(new Date());
                File tmp = new File(currentVdoPath);
                if(!tmp.exists()) tmp.mkdirs();
                String filepath = currentVdoPath+"\\视频"+filename+"."+currentVdoType;
                File savefile = new File(filepath);
                IMediaWriter writer = ToolFactory.makeWriter(savefile.getAbsolutePath());
                writer.addVideoStream(0, 0, getVideoTypeCode(currentVdoType), size.width, size.height);
                long start = System.currentTimeMillis();
                for (int i = 0; playStatus[1]; i++) {
                    BufferedImage image = null;
                    try {
                        image = ConverterFactory.convertToType(cam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
                    } catch (NullPointerException ne) {
                        break;
                    }

                    IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);

                    IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
                    frame.setKeyFrame(i == 0);
                    frame.setQuality(100);

                    writer.encodeVideo(0, frame);
                    Thread.sleep(20);
                }
                writer.close();
                //弹出提示
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        JOptionPane.showMessageDialog(null, "文件已保存至"+savefile.getAbsolutePath());
                    }
                });
            }

            while (!isgoing) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

//class ShowTime implements ActionListener, ChangeListener {
//    private JButton b;
//    private JButton s;
//    private JFrame frame;
//    private Timer timer;
//    private long passtime = 0;
//    private JLabel timeLabel;
//    private String hours, mins, secs;
//
//    public ShowTime(JFrame frame, JButton startB, JButton stopB) {
//        Container contentPane = frame.getContentPane();
//        timeLabel = new JLabel();
//        timeLabel.setBounds300,490,80,80);s
//        this.b = startB;
//        this.s = stopB;
//        b.addActionListener(this);
//        s.addActionListener(this);
//        this.frame = frame;
//        timer = new Timer(10, this);
//        contentPane.add(timeLabel);
//    }
//
//    /**
//     * Invoked when an action occurs.
//     *
//     * @param e
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        System.out.println("监听器能起作用吗");
//        if (e.getSource() == b)
//            timer.start();
//        if (e.getSource() == timer) {
//            passtime++;
//            //获取小时数
//            long hour = passtime / 3600;
//            //获取分钟数
//            long min = passtime / 60 - hour * 60;
//            //获取秒数
//            long sec = passtime - 3600 * hour - 60 * min;
//            if(sec/10<1) secs = "0" + String.valueOf(sec);
//            else secs = String.valueOf(sec);
//            if(min/10<1) mins = "0" + String.valueOf(min);
//            else mins = String.valueOf(min);
//            if(hour/10<1) hours = "0" + String.valueOf(hour);
//            else hours = String.valueOf(hour);
//        }
//        if (e.getSource() == s)
//            timer.stop();
//    }
//
//    /**
//     * Invoked when the target of the listener has changed its state.
//     *
//     * @param e a ChangeEvent object
//     */
//    @Override
//    public void stateChanged(ChangeEvent e) {
//        if(e.getSource() == timeLabel)
//            timeLabel.setText(hours+":"+mins+":"+secs);
//    }
//}




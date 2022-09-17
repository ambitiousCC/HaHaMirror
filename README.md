# 哈哈镜项目

## 视频捕获技术资料

遇到了很多困难，主要是JMF不支持64位Java JDK，同时捕获到的视频流有各种问题，如出现只能移动到屏幕左边才能够显示，同时需要安装第三方视频流捕获软件才能获取本机摄像头，因此本项目采用了开源框架WebCame。

> webcam和jfm等java播放框架的最大不同之处在于其底层封装了可以直接获取本机所有摄像头设备和图像展示的接口，可以很方便的获取视频流并添加各种特效（主要通过处理视频流中生成的缓存流图片（格式为BufferedImage）实现对视频效果的转换）
>
> 最大的优势在于，具有很多优质的开源代码，开发者可以用很少的代码完成jmf等处理的逻辑。与此同时，框架支持主流驱动，如jmf、opencv等，更多的信息可以参考[官方文档](https://github.com/ambitiousCC/webcam-capture/tree/master/webcam-capture/src/example/java)

获取多种摄像头的方法：

```java
String name = "";
try {
    //获取所有的摄像头设备
    List<Webcam> webcams = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS);
    name = webcams.get(1).getName();
} catch (TimeoutException e) {
    e.printStackTrace();
}
Webcam webcam = Webcam.getWebcamByName(name);
```

实现视频流展示的主要实现代码：

```java
Webcam webcam = Webcam.getDefault(); 				// 获取默认的摄像头设备
Dimension size = WebcamResolution.VGA.getSize(); 	// 获取VGA格式的视频窗口大小
webcam.setViewSize(size); 							// 设置展示窗口

WebcamPanel panel = new WebcamPanel(webcam);  		// 视频展示的panel，可以直接加入容器中展示

JFrame window = new JFrame("webcam panel");			// JFrame容器
window.add(panel);									// 添加视频容器
window.setResizable(true);							// 允许调整大小
window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
window.pack();
window.setVisible(true);
```

### 实现软件设置功能

软件设置主要有缓存变量和实例化缓存两种方式，根据具体需要，既要实现变量缓存动态更新保存目录，又要实现下一次打开软件可以自动加载上次保存

#### 变量动态缓存

实现的思路比较容易，具体配置的内容修改主要是通过全局变量实现：

设定全局的变量保存设置的相关配置，当用户进行操作时，通过改变全局变量实现动态更新，同时支持将全局变量当前状态保存到具体的实例化文件中，具体操作见【实例化缓存】

#### 实例化缓存

图形化交互：

* 用户点击按钮摊开可供选择的文件窗口：

  1. 通过`JFileChooser`类实现打开窗口，获取用户选择的目录，写入全局变量的同时写入实例化文件。这里通过实现一个`ObjectFile`实现，类代码如下：

  ```java
  import java.beans.XMLDecoder;
  import java.beans.XMLEncoder;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.InputStream;
  import java.io.ObjectInputStream;
  import java.io.ObjectOutputStream;
  import java.io.OutputStream;
  
  public class ObjectFile
  {
      /**
       * 持久化为XML对象
       *
       * @param obj
       * @param out
       */
      public void storeXML(Object obj, OutputStream out)
      {
          XMLEncoder encoder = new XMLEncoder(out);
          encoder.writeObject(obj);
          encoder.flush();
          encoder.close();
      }
  
      /**
       * 从XML中加载对象
       *
       * @param in
       * @return
       */
      public Object loadXML(InputStream in)
      {
          XMLDecoder decoder = new XMLDecoder(in);
          Object obj = decoder.readObject();
          decoder.close();
          return obj;
      }
  
      /**
       * 持久化对象
       *
       * @param obj
       * @param out
       * @throws IOException
       */
      public void store(Object obj, OutputStream out) throws IOException
      {
          ObjectOutputStream outputStream = new ObjectOutputStream(out);
          outputStream.writeObject(obj);
          outputStream.flush();
          outputStream.close();
      }
  
      /**
       * 加载对象
       *
       * @param in
       * @return
       * @throws IOException
       * @throws ClassNotFoundException
       */
      public Object load(InputStream in) throws IOException,
              ClassNotFoundException
      {
          ObjectInputStream inputStream = new ObjectInputStream(in);
          Object obj = inputStream.readObject();
          inputStream.close();
          return obj;
      }
  
      public static void main(String[] args) throws Exception
      {
          String storeName = "java object";
  
          //xml文件形式存储
          File xmlFile = new File("xmlFile.dat");
          ObjectFile serializable = new ObjectFile();
          serializable.storeXML(storeName, new FileOutputStream(xmlFile));
          System.out.println(serializable.loadXML(new FileInputStream(xmlFile)));
  
          //文件形式存储
          File file = new File("file.dat");
          serializable.store(storeName, new FileOutputStream(file));
          System.out.println(serializable.load(new FileInputStream(file)));
      }
  }
  ```

  2. 引入实例化类，实现这个对象，将用户选择的设置写入缓存，这里监听用户提交的按钮，每次提交之后执行，具体代码如下：

  ```java
  saveBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          currentImgPath = imgText.getText();//图片保存路径，全局变量，声明在类内，private String xxxpath;
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
          jF.dispose();  //退出窗口
      }
  });
  ```

  

## 视频格式和图片格式转换材料

### 截图并保存为多种格式图片文件

各种图片实质上的缓冲流一致，各个图片格式的保存方式一致，但是支持的图片保存格式有限，需要先判断用户输入的格式是否支持，支持的图片格式有：png、jpg、gif、bmp、wbmp

具体代码如下：

```java
private String getImageType(String name) {
    String t = name.substring(name.lastIndexOf(".")+1,name.length());  //从路径中获取图片的格式
    for(String type:supportImgFormat) {  // 判断是否支持
        if(t.equals(type))
            return type;
    }
    return null;
}
```



实现保存图片的基本方法如下：

```java
try{
    File file = new File(filePath);  //根据用户选择的路径创建新的图片对象
    String choosedType = getImageType(file.getName());  //判断用户选择的类型是否支持
    if(choosedType==null) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                JOptionPane.showMessageDialog(null, "不支持的文件类型！");
            }
        });
        return;
    }
    ImageIO.write(saveImage,choosedType,new File(filePath));  //写入图像，需要输入BufferedImage、类型、文件对象
    SwingUtilities.invokeLater(new Runnable() {
        public void run()
        {
            JOptionPane.showMessageDialog(null, "保存成功");
        }
    });
} catch (IOException exception) {
    exception.printStackTrace();
}
```



### 保存视频流为多种格式视频文件

保存视频的方法实现方法比较困难，因为要做到在预览视频的同时录制对应视频，同时允许用户直接通过点击按钮确定录制视频工作的开始和结束，因此必须使用多线程的方法，在展示视频的同时对录制的缓冲区图像进行处理。



#### 处理视频

使用`ToolFactory.makeWriter(file.getName());`获得`IMediaWriter`对象，实例化媒体写入工具类，根据用户选择的保存的视频格式确定所能够使用的视频编码格式，具体的对应关系如下：

> 支持保存的视频格式有：ts、mp4、avi、mov、flv五种，可以使用如下的视频编码格式
>
> 1. ts/mp4/mov均可以使用编码效率最高，压缩率最高同时效果比较好的H.264进行编码
> 2. avi不能使用H.264，可以使用MPEG4进行编码，由于avi视频没有对图片进行压缩，所以产生的视频会很大
> 3. flv可以使用FLV1进行编码

视频流保存示例代码如下：

```java
// 1. 创建输出文件对象
String filepath = "output.ts";
File file = new File(filepath);

// 2. 构建视频流处理方法
IMediaWriter writer = ToolFactory.makeWriter(file.getName());

// 3. 确定视频输入流的大小
Dimension size = WebcamResolution.QVGA.getSize();  // 这里是VGA的size: 640×480

// 4. 处理方法中输入视频流和对应编码格式
writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, size.width, size.height);  // 需要输入视频的长和高

// 5. 获取默认视频设备，并设置大小和开启设备
Webcam webcam = Webcam.getDefault();
webcam.setViewSize(size);
webcam.open(true);

// 6. 记录开始录制的时间
long start = System.currentTimeMillis();

// 7. 这里50代表1s，计算方法为 录制视频时间 = 循环次数 x 每处理一张图片需要的时间
for (int i = 0; i < 50; i++) {
    // 从视频流中获取缓冲图像，格式为TYPE_3BYTE_BGR，方便处理三个通道的颜色像素，为特效转化做准备
    // TYPE_3BYTE_BGR表示一个具有8位rgb颜色分量的图像，具有用3字节存储的 blue、green和red三种颜色。不存在alpha。
    BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
    // 缓冲图像转换，以YUV420P格式存储
    IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);
	// 处理缓冲流，需要确定处理的时间将每次获取的图像数据存入Video流
    IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
    frame.setKeyFrame(i == 0);
    frame.setQuality(0);

    // 视频编码
    writer.encodeVideo(0, frame);

    // 每处理一张线程停止20ms的时间，也就是每秒钟处理50张图片，具有很高的流畅度
    Thread.sleep(20);
}
// 8. 写入流关闭
writer.close();

```

然而，上面的方法只能实现录制指定时间的视频长度，要实现与用户交互的功能，需要监听用户点击的按钮，当用户点击按钮触发时关闭视频流，增加条件判断语句实现视频的保存。监听功能需要配合`EventListener`和`EventObject`等方法

## 图像几何变换材料

由于程序默认是对缓冲图像进行处理，因此很方便的对每一张缓冲图片进行转换。

利用Webcam中的特效设置方法`Webcam.setImageTransformer(WebcamImageTransformer webcamImageTransformer)`对所有缓冲图片一一进行处理，将问题转换为对图片数据的转换

#### 基本视频特效

只对每个像素的色彩进行转换，主要实现了"水平翻转","垂直翻转","黑白相机","灰度1","灰度2","高斯模糊","怀旧照片","珠纹化","油画","熔炉滤镜","红色物体检测","马赛克","连环画","阴间滤镜","鬼影滤镜"特效，特效实现方法简要概括如下：

* 水平翻转：对像素点的位置按照竖直中心轴对称翻转
* 垂直翻转：对像素点的位置按照垂直中心轴对称翻转
* 黑白相机：获取每一个像素点的RGB值并进行判断，计算其R,G,B的平均值，平均值大于80设置为黑色，小于80设置为白色（其他方案亦可）
* 灰度1：同一处理像素点R,G,B值均赋值为三者平均值即可
* 高斯模糊：9x9卷积核卷积最大即可
* 怀旧照片：网上算法，具体计算原理不详
* 马赛克：每隔相同距离的像素点进行处理，将某一个像素点的像素值赋值给一个固定大小区域内的像素点
* 珠纹化：相对于马赛克，其相同像素所占区域更小
* 油画：产生随机数，对一个像素点的扩展方向（向右和向下）进行马赛克处理，出现笔刷的特效效果，效果不太好。
* 熔炉滤镜
* 红色物体检测：像素的G值小于52不变，大于52的返回黑色的RGB
* 连环画
* 阴间滤镜
* 鬼影滤镜：反转像素值

#### 哈哈镜特效

哈哈镜特效实现需要对像素的位置进行调整，才能实现画面的扭曲，主要实现了"中心内凹","纵向外凸","横向外凸","中心螺旋","中心外凹","复合特效"特效，具体实现方设计三角函数的具体计算，其具体计算思路不做提供，书上都有。

* 复合特效：对一个图像的不同区域进行不同的特效处理

## 并发程序设计资料

并未实现对所有特效进行转换，只对"横向外凸"特效进行并行程序处理，并测试执行时间，与顺序执行下的执行时间进行比较。

#### 具体实现过程

设置并发特效类`taskFilter`继承`JHFilter`类，并重写`BufferedImage filter(BufferedImage src, BufferedImage dest)`方法

因此对原来的特效实现中各个部分进行分离（主要是图像内容和图像计算两个部分），构成能够进行独立运行的线程；主要将原图像按照像素的行进行划分，每一行作为独立线程进行运算，最终结合每一行的结果构成最终的像素矩阵。主要代码如下：

1. taskFilter类，继承JHFilter类，实现线程和线程池的创立和提交

```java
public class taskFilter extends JHFilter {
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        return taskTransformer(src, dest);
    }

    private BufferedImage taskTransformer(BufferedImage src, BufferedImage dest) {
        BufferedImage[] input = {src, dest};
        int[] outPixels = new int[src.getWidth()*src.getHeight()];
        //输入初始矩阵，构建线程池
        ForkJoinTask task = new taskEffect(input , outPixels, 0, src.getHeight()-1);
        ForkJoinPool pool = new ForkJoinPool(5);
        //并行执行
        pool.submit(task);
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return input[1];  // 对象是地址引用， 所以直接返回就可以了
    }
}
```

2. taskEffect类，继承RecursiveAction，成立独立线程

```java
class taskEffect extends RecursiveAction {
    //...各个变量的声明和初始化

    public taskEffect(BufferedImage[] input, int[] outPixels, int start, int end) {
        super();
        this.src = input[0];
        this.width = src.getWidth();
        this.height = src.getHeight();
        this.start = start;
        this.end = end;

        if (input[1] == null)
            input[1] = new taskFilter().createCompatibleDestImage( src, null );
        this.dest = input[1];

        inPixels = new int[width*height];
        this.outPixels = outPixels;
        new taskFilter().getRGB( src, 0, 0, width, height, inPixels );
        index = 0;
        outIndex = 0;
        centerX = width/2;
        centerY = height/2;
        radius = Math.max(centerX, centerY);
        offsetX = 0;
        offsetY = 0;
    }

    /**
     * 任务的主要方法，对每一部分进行计算
     */
    @Override
    protected void compute() {
        if (end - start < MAX) {
            for(int step = this.start; step <= this.end; step++) {
                transform(step);
            }
            new taskFilter().setRGB( dest, 0, 0, src.getWidth(), src.getHeight(), outPixels);
        } else {
            BufferedImage[] input = {src, dest};
            invokeAll(new taskEffect(input, outPixels,  start, (start+end)/2),
                    new taskEffect(input, outPixels,  (start+end)/2+1, end));
        }
    }

    /**
     * 特效处理方法
     */
    private void transform(int row) {
        //...特效处理方法，对像素的转换
    }
}
```

【总结】并发程序的思路就是同时处理图像的每一行（或者其他细分方法），在性能上要优于逐行处理，当然进行线程上下文切换的过程中也会造成资源损耗，是否优化还得具体情况具体分析。

## 开发日志

### 开发工具

开发环境：windows10操作系统

编程环境：64位Java JDK

开发工具：IDEA

### 具体过程

#### 1. 设计并完成图形化界面

【实现思路】参考了主流的视频播放软件，界面最终确定为单窗口设定，即

![基本框架设计](https://github.com/ambitiousCC/HaHaMirror/blob/master/png/%E5%9F%BA%E6%9C%AC%E6%A1%86%E6%9E%B6%E8%AE%BE%E8%AE%A1.png)

主要界面布局：

1. 中间大框为视频流展示窗口
2. 上下左右分别分布如图所示的按钮，包括了
   * 切换特效的选择文本框（右部两个功能栏）
   * 下方为功能按钮，从左到右依次是：
     * 截图
     * 录制
     * 人脸识别
     * 播放
     * 选择设备
     * 设置
   * 上方为附加按钮，主要有一些基本信息的提示，如帮助文档和资助方法的二维码等

【设计实现】利用Java的awt包，根据图形界面分布完成各个组件的分布和大小调整。并将所有UI自定义，最终的效果图如图所示：

![image-20210518150114237](https://github.com/ambitiousCC/HaHaMirror/blob/master/png/%E6%9C%80%E7%BB%88%E5%AE%9E%E7%8E%B0.png)

其中，选择的界面修改成为了根据运行环境选择最合适的UI，实现代码如下：

```java
try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (Exception e1) {
    e1.printStackTrace();
}
```

#### 2. 实现视频捕获

按照资料的思路，解决了视频捕获的问题。遇到的问题放在了底下。视频捕获技术主要参考了官方文档，最终实现了实时视频流处理，能够在预览视频的同时控制视频录制按钮。

但是出现了不足，就是无法实时获取录制视频的时长，曾经尝试过通过Timer类进行展示，但是效果非常不好，时间无法同步，索性直接删除了这个功能。希望有机会能够补上，主要的思路还是开多线程对录制时间和当前时间求时间差计算，由于开线程并没有想象中的那么方便，所以暂时搁置一边，实现一些不是那么细节的功能。

遇到的最大的问题就是按钮切换状态：

【问题描述】对于当前的状态，需要使用按钮进行调控：

1. 未录制视频时，处于测试阶段，此时屏幕会左右分屏，展示特效的预览效果
   1. 播放状态时，展示特效
   2. 暂停状态时，清空所有特效
2. 录制视频时，处于录制状态，取消分屏预览效果而是展示全部的特效
   1. 播放状态时，处于录制状态，此时录制视频的同时可以切换特效
   2. 点击暂停，停止录制，此时会清空录制视频的状态，转换为未录制视频的状态

【实现思路】定义一个类`curStatus`专门用来表示当前的状态，当按钮被触发时，修改这个变量存储的所有状态即可

#### 3. 实现视频格式和图片格式转换和保存

这一部分主要是对录制的视频流进行处理，根据用户选择的视频格式选择对应的编码方法，具体的实现见《视频格式和图片格式转换资料》，主要是搜集资料学习图像格式和图像编码以及视频编码格式和视频格式的对应关系以及图像技术相关的发展历程。

#### 4. 图像几何变换

这部分需要学习cv的相关知识，比如各种滤波、RGB转换关系，这些网上都有现成的资料可以学习。主要是创新吧，对像素点的位置变换造就了各种特效。

普通特效，不涉及对像素点位置的变换，只改变了像素点的像素值。如最简单的美白效果，就是先根据算法检测当前像素点是否未肌肤，如果是就增大其RGB值，达到亮白的效果，如果不是就不进行处理。

哈哈镜特效：不涉及对像素值的改变，只根据设计的算法对像素点位置也进行了变换，可以考虑对像素值也进行变换。由于三角函数周期性的规律，可以设置定时函数或者产生随机数，造成定期波动

#### 5. 并发程序设计&优化效果对比

并发程序的效果并没有预想中的那么优秀，反而是比原来略逊了一筹，出现的可能原因已经放到了下面的问题中讨论。开发过程中觉得这部分设计并不是那么难，主要还是分清特效转换的各个部分的作用，比如哪些部分是初始化定义，哪些部分是真正的计算过程，有的部分不能割裂。并发程序的设计思路无非是对输入的视频流的每一行进行并发计算处理。

### 遇到的问题&解决方案

1. 在开发录制视频的功能时，出现只能录制视频无法展示实时效果或出现只能展示实时效果而无法录制视频

   【原因】解决线程冲突问题，录制视频时，如果开单线程，视频的输入流被占用，此时是无法在视频窗口进行视频流展示；如果在组件未构建完毕就声明视频录制方法，录制方法占用了资源，会导致无法构建剩下的组件。

   【解决方案】在初始化窗口类，其他静态组件初始化结束时就进行录制视频方法的声明，而不是放在构造的中间，使用监听类对录制按钮进行监听，如果状态发生改变，触发相应的事件机制。

2. 下拉菜单的自定义UI中，第一次无异常，第二次就会出现下拉框中的选项字体消失

   【原因】UI设置中焦点获取事件中字体的颜色设置为和背景色相同的颜色

   【解决方案】修改UI类中获取焦点以后的颜色设置

3. 使用多线程的方法反而没有原始方法的效率高，平均处理一张图片的时间高于原始方法同时出现明显的延迟

   【原因】Webcam底层对获取视频流处理进行了优化，而自定义的多进程优化的效果远远低于官方的优化

   【解决方案】无

4. 如何解决存储视频文件名冲突的问题

   【解决方案】可以以时间戳进行命名，区分不同时间录制视频的文件

package transformers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.sun.org.apache.xml.internal.security.Init;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;


import event.StatusEvent;
import listener.StatusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class MakeVideo {
    public static void main(String[] args) {
        Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
        new MakeVideo(webcam);
    }

    private Webcam webcam;
    public MakeVideo(Webcam webcam) {
        this.webcam = webcam;
        try {
            System.out.println("尝试打开录制窗口");
            this.startVideoRecording();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private StatusListener statusListener = new StatusListener() {
        @Override
        public void updateEvent(StatusEvent dm) {
            System.out.println("判断status状态决定操作");
            if(status) {
                System.out.println("开始录制");
                isgoing = true;
            } else {
                System.out.println("停止录制");
                isgoing = false;
            }
        }
    };
    private boolean isgoing = false;  // 判断是否录制
    //将监听器添加给该变量
    private boolean status = false;  // 被动态坚挺的状态，由此状态决定是否录制

    private void startVideoRecording() throws InterruptedException{
        Webcam webcam = openWebcam();
        Dimension size = webcam.getViewSize();
        while(true) {
            if(isgoing) {
                File savefile = new File("saved.mp4");
                IMediaWriter writer = ToolFactory.makeWriter(savefile.getName());
                writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, size.width, size.height);
                long start = System.currentTimeMillis();
                for (int i = 0; ; i++) {
                    BufferedImage image = null;
                    try {
                        image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
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
                System.out.println("文件写入默认文件夹");
            }
            while (!isgoing) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("改变条件" + isgoing);
        }
    }

    private Webcam openWebcam() {

        final JFrame window = new JFrame("Test webcam panel");
        Dimension size = WebcamResolution.DVGA.getSize();
        WebcamPanel panel = new WebcamPanel(webcam, size, false);

        final String play = "PLAY";
        final String stop = "STOP";

        final JButton button = new JButton();
        button.setAction(new AbstractAction(play) {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (panel.isStarted()) {
                    panel.stop();
                    button.setText(play);
                    status = false;
                } else {
                    panel.start();
                    button.setText(stop);
                    status = true;
                }
                statusListener.updateEvent(new StatusEvent(this,status));
            }
        });

        button.setBounds(getButtonBounds(size));

        panel.setLayout(null);
        panel.add(button);
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);

        webcam.open();

        return webcam;
    }

    private static Rectangle getButtonBounds(Dimension size) {
        final int x = (int) (size.width * 0.1);
        final int y = (int) (size.height * 0.8);
        final int w = (int) (size.width * 0.8);
        final int h = (int) (size.height * 0.1);
        return new Rectangle(x, y, w, h);
    }

}
package  webcapTest;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;



public class WecamTutorial
{
    private static int    num    = 0;

    public static void main(String[] args) throws IOException, TimeoutException
    {
        final Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
//        获取相机有哪些
        String test = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS).get(0).toString();
        System.out.println("Webcams: "+test);
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        //Webcam.setDriver(new NativeWebcamDriver());
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        final JFrame window = new JFrame("摄像头");

        window.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e)//关闭时的操作，windowClosed是关闭后的操作
            {
                webcam.close();
                window.dispose();
            }
        });
//        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//使用 System exit 方法退出应用程序
        final JButton button = new JButton("截图");
        window.add(panel, BorderLayout.CENTER);
        window.add(button, BorderLayout.SOUTH);
        window.setResizable(true);
        window.pack();
        window.setVisible(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                button.setEnabled(false);
                String fileName = "D://" + num;
                WebcamUtils.capture(webcam, fileName, ImageUtils.FORMAT_PNG);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run()
                    {
                        JOptionPane.showMessageDialog(null, "截图成功");
                        button.setEnabled(true);
                        num++;
                        return;
                    }
                });
            }
        });
    }
}

package webcapTest;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class simpleCutImg {
    public static void main(String[] args) {
        Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
//        cutOrigin(webcam,"JPG","D://test.jpg");
        //获取所有格式
        for(Dimension supportedSize: webcam.getViewSizes()) {
            System.out.println(supportedSize.toString());
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        cutOrigin(webcam,"PNG","D://test.png");
    }

    public static void cutOrigin(Webcam webcam,String formater,String path){
        webcam.open();
        try {
            ImageIO.write(webcam.getImage(),formater,new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

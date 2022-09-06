package demos;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.jh.JHFlipFilter;


/**
 * This example demonstrates how to use {@link WebcamImageTransformer} to rotate the image from
 * camera by using {@link JHFlipFilter} from Jerry Huxtable filters package.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ImageTransformer2 implements WebcamImageTransformer {

    /**
     * This is filter from JH Labs which flips buffered image 90 degrees clockwise. For more details
     * please follow to the <a href="http://www.jhlabs.com/ip/filters/index.html">JH Labs Filters<a>
     * home page (filters source code can be found
     * <a href="https://github.com/axet/jhlabs">here</a>).
     */
    private final BufferedImageOp filter = new JHFlipFilter(JHFlipFilter.FLIP_180);

    public ImageTransformer2() {

        // use VGA resolution

        Dimension size = WebcamResolution.VGA.getSize();

        // get default webcam and set image transformer to this (transformer will modify image after
        // it's received from webcam, in this case it will rotate it)

        String name = "";
        try {
            List<Webcam> webcams = Webcam.getDiscoveryService().getWebcams(10, TimeUnit.SECONDS);
            name = webcams.get(1).getName();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Webcam webcam = Webcam.getWebcamByName(name);
        webcam.setViewSize(size);
        webcam.setImageTransformer(this);
        webcam.open();

        // create window

        JFrame window = new JFrame("Test Rotation");

        // and webcam panel

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDrawMode(DrawMode.FIT);

        // add panel to window

        window.add(panel);
        window.pack();
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public BufferedImage transform(BufferedImage image) {

        // this will do rotation on image

        return filter.filter(image, null);
    }

    public static void main(String[] args) {
        new ImageTransformer2();
    }
}
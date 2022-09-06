//package webcapTest;
//
//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.Stroke;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Iterator;
//import java.util.List;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//import javax.imageio.ImageIO;
//import javax.swing.JFrame;
//
//import com.github.sarxos.webcam.Webcam;
//import com.github.sarxos.webcam.WebcamPanel;
//import com.github.sarxos.webcam.WebcamResolution;
//import org.openimaj.image.ImageUtilities;
//import org.openimaj.image.processing.face.detection.DetectedFace;
//import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
//import org.openimaj.math.geometry.shape.Rectangle;
//
//
///**
// * Paint troll smile on all detected faces.
// *
// * @author Bartosz Firyn (SarXos)
// */
//public class FacePainterExample implements Runnable, WebcamPanel.Painter {
//
//    private static final long serialVersionUID = 1L;
//
//    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
//    private static final HaarCascadeDetector detector = new HaarCascadeDetector();
//    private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);
//
//    private Webcam webcam = null;
//    private WebcamPanel.Painter painter = null;
//    private List<DetectedFace> faces = null;
//    private BufferedImage troll = null;
//
//    public FacePainterExample(Webcam webcam, WebcamPanel panel) throws IOException {
//        super();
//        this.webcam = webcam;
//        File f;
//        f = new File("mask1.png");
//        InputStream is = new FileInputStream(f);
//        System.out.println(is);
//        troll = ImageIO.read(is);
//        panel.setFPSDisplayed(true);
//        panel.setFPSLimited(true);
//        panel.setFPSLimit(20);
//        panel.setPainter(this);
//        webcam.open(true);
//        panel.start();
//        System.out.println("打开摄像头");
//
//        painter = panel.getDefaultPainter();
//
//        EXECUTOR.execute(this);
//    }
//
//    public void shutdown() {
//        going = false;
//    }
//
//    public void run() {
//        while (true) {
//            if (!webcam.isOpen()) {
//                return;
//            }
//            faces = detector.detectFaces(ImageUtilities.createFImage(webcam.getImage()));
//        }
//    }
//
//    public void paintPanel(WebcamPanel panel, Graphics2D g2) {
//        if (painter != null) {
//            painter.paintPanel(panel, g2);
//        }
//    }
//
//    private boolean going = true;
//    public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {
//
//        if (painter != null) {
//            painter.paintImage(panel, image, g2);
//        }
//
//        if (faces == null) {
//            return;
//        }
//
//        Iterator<DetectedFace> dfi = faces.iterator();
//        while (dfi.hasNext() && going) {
//
//            DetectedFace face = dfi.next();
//            face.setBounds(new Rectangle(face.getBounds().x+50, face.getBounds().y+20,face.getBounds().width,
//                    face.getBounds().height));
//            Rectangle bounds = face.getBounds();
//            System.out.println(bounds.toString());
//            System.out.println("识别准确率："+face.getConfidence());
//            int dx = (int) (0.1 * bounds.width);
//            int dy = (int) (0.2 * bounds.height);
//            int x = (int) bounds.x - dx;
//            int y = (int) bounds.y - dy;
//            int w = (int) bounds.width + 2 * dx;
//            int h = (int) bounds.height + dy;
//
//            g2.drawImage(troll, x, y, w, h, null);
//            g2.setStroke(STROKE);
//            g2.setColor(Color.RED);
////            g2.drawRect(x, y, w, h);
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
////        new FacePainterExample();
//    }
//}

//
//

package webcapTest;

        import java.awt.BasicStroke;
        import java.awt.Color;
        import java.awt.Graphics2D;
        import java.awt.Stroke;
        import java.awt.image.BufferedImage;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.util.Iterator;
        import java.util.List;
        import java.util.concurrent.Executor;
        import java.util.concurrent.Executors;

        import javax.imageio.ImageIO;
        import javax.swing.JFrame;

        import com.github.sarxos.webcam.Webcam;
        import com.github.sarxos.webcam.WebcamPanel;
        import com.github.sarxos.webcam.WebcamResolution;
        import org.openimaj.image.ImageUtilities;
        import org.openimaj.image.processing.face.detection.DetectedFace;
        import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
        import org.openimaj.math.geometry.shape.Rectangle;


/**
 * Paint troll smile on all detected faces.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class FacePainterExample implements Runnable, WebcamPanel.Painter {

    private static final long serialVersionUID = 1L;

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final HaarCascadeDetector detector = new HaarCascadeDetector();
    private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[]{1.0f}, 0.0f);

    private WebcamPanel.Painter painter = null;
    private List<DetectedFace> faces = null;
    private BufferedImage troll = null;
    private Webcam webcam;
    public FacePainterExample(Webcam webcam, WebcamPanel panel) throws IOException {
        super();
        this.webcam = webcam;
        File f;
        f = new File("mask1.png");
        InputStream is = new FileInputStream(f);
        System.out.println(is);
        troll = ImageIO.read(is);

        webcam = Webcam.getWebcamByName("HD Camera 1");
        webcam.open(true);

//        panel.setPreferredSize(WebcamResolution.VGA.getSize());
//        panel.setPainter(this);
//        panel.setFPSDisplayed(true);
//        panel.setFPSLimited(true);
//        panel.setFPSLimit(20);
        panel.setPainter(this);
        panel.start();

//        frame.add(panel);
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);

        painter = panel.getDefaultPainter();

        EXECUTOR.execute(this);
    }

    public void run() {
        while (true) {
            if (!webcam.isOpen()) {
                return;
            }
            faces = detector.detectFaces(ImageUtilities.createFImage(webcam.getImage()));
        }
    }

    public void paintPanel(WebcamPanel panel, Graphics2D g2) {
        if (painter != null) {
            painter.paintPanel(panel, g2);
        }
    }

    private boolean going = true;
    public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {

        if (painter != null) {
            painter.paintImage(panel, image, g2);
        }

        if (faces == null) {
            return;
        }

        Iterator<DetectedFace> dfi = faces.iterator();
        while (dfi.hasNext() && going) {

            DetectedFace face = dfi.next();
            Rectangle bounds = face.getBounds();

            int dx = (int) (0.1 * bounds.width);
            int dy = (int) (0.2 * bounds.height);
            int x = (int) bounds.x - dx;
            int y = (int) bounds.y - dy;
            int w = (int) bounds.width + 2 * dx;
            int h = (int) bounds.height + dy;

            g2.drawImage(troll, x+180, y+80, w, h, null);
            g2.setStroke(STROKE);
            g2.setColor(Color.RED);
//            g2.drawRect(x, y, w, h);
        }
    }

    public void shutdown() {
        going = false;
    }

    public static void main(String[] args) throws IOException {
        Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
        WebcamPanel panel = new WebcamPanel(webcam, false);
        JFrame frame = new JFrame();
        frame.add(panel);

        frame.setTitle("Face Detector Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        new FacePainterExample(frame, webcam, panel);
    }
}
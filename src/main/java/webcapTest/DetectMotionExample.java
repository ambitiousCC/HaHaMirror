package webcapTest;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamPanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


/**
 * Detect motion.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class DetectMotionExample extends JFrame implements Runnable {

	private static final long serialVersionUID = -585739158170333370L;

	private static final int INTERVAL = 100; // ms

	private ImageIcon motion = null;
	private ImageIcon nothing = null;
	private JLabel label = null;

	private Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
	private int threshold = 25;
	private int inertia = 100; // how long motion is valid

	public DetectMotionExample() {

		try {
			motion = new ImageIcon(ImageIO.read(new File("me-gusta.png")));
			nothing = new ImageIcon(ImageIO.read(new File("forever-alone.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		label = new JLabel(nothing);

		Thread updater = new Thread(this, "updater-thread");
		updater.setDaemon(true);
		updater.start();

		setTitle("Rage Motion Detector");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());

		webcam.setViewSize(new Dimension(320, 240));

		WebcamPanel panel = new WebcamPanel(webcam);

		add(panel);
		add(label);

		pack();
		setVisible(true);
	}

	public static void main(String[] args) throws InterruptedException {
		new DetectMotionExample();
	}

	@Override
	public void run() {

		WebcamMotionDetector detector = new WebcamMotionDetector(webcam, threshold, inertia);
		detector.setInterval(INTERVAL);
		detector.start();

		while (true) {

			Icon icon = label.getIcon();
			if (detector.isMotion()) {
				if (icon != motion) {
					label.setIcon(motion);
				}
			} else {
				if (icon != nothing) {
					label.setIcon(nothing);
				}
			}

			try {
				Thread.sleep(INTERVAL * 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
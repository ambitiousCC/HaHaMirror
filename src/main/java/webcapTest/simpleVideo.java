package webcapTest;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;

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

public class simpleVideo extends JFrame{
    public Webcam webcam = Webcam.getWebcamByName("HD Camera 1");
    public static void main(String[] args) {
        new simpleVideo();
    }
    public simpleVideo() {
        //1
        this.setTitle("趣味哈哈镜 vbeta:0.1 by Derek");
        int x=100,y=120,width=1186,height=600;
        this.setBounds(x,y,width,height);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//        this.setLayout(null);
        this.getContentPane().setBackground(Color.white);

        //2
        this.setResizable(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//        this.pack();
        this.setLocationRelativeTo(null);
        webcam.setViewSize(new Dimension(640,480));

        WebcamPanel panel = new WebcamPanel(webcam);

        JPanel jpOldVideo = new JPanel();
        jpOldVideo.setLayout(null);
        jpOldVideo.setBounds(20, 20, 480, 480);//长宽450，下面那个x就需要加上这个宽度
//		jpOldVideo.setBackground(Color.white);
        jpOldVideo.setBorder(BorderFactory.createTitledBorder("原始视频："));

        /*****原始视频*****/
        JPanel oriVideo = new JPanel();
//        oriVideo.setLayout(null);
        oriVideo.setBounds(5, 15, 470, 460);
        oriVideo.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        oriVideo.add(panel);
        jpOldVideo.add(oriVideo);
        this.add(jpOldVideo);
        this.setVisible(true);

    }
}

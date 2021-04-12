package reUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Created by SongFei on 2017/11/1.
 */
public class MyButtonUI extends BasicButtonUI implements SwingConstants {

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JButton button = (JButton) c;
        button.setContentAreaFilled(false);//父类不用绘制内容
        button.setFocusPainted(false);//父类不用绘制焦点
        button.setBorderPainted(false);//父类不用绘制边框
    }

}
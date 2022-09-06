package demos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class FileChooserFrame {

    public static void main(String[] args) {

        JFrame frame = new JFrame("这里测试FileDialog");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        final FileDialog d1 = new FileDialog(frame, "选择需要加载的文件", FileDialog.LOAD);
        final FileDialog d2 = new FileDialog(frame, "选择需要保存的文件", FileDialog.SAVE);

        JButton b1 = new JButton("打开文件");
        JButton b2 = new JButton("保存文件");

        //给按钮添加事件
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d1.setVisible(true);
                //打印用户选择的文件路径和名称
                System.out.println("用户选择的文件路径:"+d1.getDirectory());
                System.out.println("用户选择的文件名称:"+d1.getFile());
            }
        });

        System.out.println("-------------------------------");
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                d2.setVisible(true);
                //打印用户选择的文件路径和名称
                System.out.println("用户选择的文件路径:"+d2.getDirectory());
                System.out.println("用户选择的文件名称:"+d2.getFile().split("\\.")[1]);
            }
        });

        //添加按钮到frame中

        frame.add(b1);
        frame.add(b2,BorderLayout.SOUTH);

        //设置frame最佳大小并可见
        frame.pack();
        frame.setVisible(true);
    }
}

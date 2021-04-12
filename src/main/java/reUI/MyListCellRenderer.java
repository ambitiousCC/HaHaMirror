package reUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by SongFei on 2017/11/2.
 */
public class MyListCellRenderer implements ListCellRenderer {

    private DefaultListCellRenderer defaultCellRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        // 每一行，都转换成jlabel来处理
        JLabel renderer = (JLabel) defaultCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // 每一行的jlabel的颜色
        if (isSelected) {
            renderer.setBackground(Color.GRAY);
            renderer.setForeground(Color.WHITE);
        } else {
            renderer.setBackground(null);
        }

        // 字体靠左
        renderer.setHorizontalAlignment(JLabel.LEFT);

        // 左侧padding
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // render的宽高
        renderer.setPreferredSize(new Dimension(100, 25));

        // list背景色，也就是向下的按钮左边儿那一块儿
        list.setSelectionBackground(null);
        list.setBorder(null);

        return renderer;
    }

}
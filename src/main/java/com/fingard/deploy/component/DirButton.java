package com.fingard.deploy.component;

import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author zhangym
 * @version 1.0  2018/5/10
 */
public class DirButton extends JButton {
    private JTextField dirPath;
    public DirButton bind(JTextField dirPath){
        this.dirPath = dirPath;
        return this;
    }

    public DirButton(final JFrame container, final File startPath, String name,
                     final String title, final int flag){
        super(name);
        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String path = FileUtils.getSelectedDirPath(container, startPath,
                        title, flag);
                if(path == null){
                    return;
                }
                dirPath.setText(path);
            }
        });
    }
}

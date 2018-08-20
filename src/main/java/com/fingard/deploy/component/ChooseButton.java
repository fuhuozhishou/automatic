package com.fingard.deploy.component;

import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author zhangym
 * @version 1.0  2018/4/27
 */
public class ChooseButton extends JButton{
    private CustomComboBox selectComboBox = null;
    private final JFrame main;
    public ChooseButton bind(CustomComboBox multiSelectComboBox){
        this.selectComboBox = multiSelectComboBox;
        return this;
    }
    public ChooseButton(final JFrame main, final File startPath,
                        final String title, final boolean needFilter, final int flag){
        super("选择");
        this.main = main;
        this.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String path = FileUtils.getSelectedDirPath(main, startPath, title, flag);
                if(path == null){
                    return;
                }
                System.out.println("选择的项目路径：" + path);
                String projectName = (needFilter)? path.substring(path.lastIndexOf("\\") + 1) : path;
                if(selectComboBox != null){
                    for(int i = 0; i < selectComboBox.getItemCount(); i++){
                        if(selectComboBox.getItemAt(i).equals(projectName)){
                            selectComboBox.setSelectedItem(projectName);
                            return;
                        }
                    }
                    selectComboBox.addItem(projectName);
                    selectComboBox.setSelectedItem(projectName);

                }
            }
        });
    }
}

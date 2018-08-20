package com.fingard.deploy.component;


import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author zhangym
 * @version 1.0  2018/5/10
 */
public class LoadUI extends JFrame{
    private CustomComboBox ccb_loadParameter;
    private HashMap<String, String> pFilePathHash = new HashMap<>();

    public void init(){
        JPanel jPanel = new JPanel();
        ccbInit();
        jPanel.add(chooseButtonInit());
        jPanel.add(ccb_loadParameter);
        jPanel.add(sureButtonInit());
        this.setLayout(new FlowLayout());
        this.add(jPanel);
        this.setTitle("自动化版本发布 v3.0");// 窗体标签
        this.setSize(500, 100);// 窗体大小
        this.setLocationRelativeTo(null);// 在屏幕中间显示(居中显示)
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 退出关闭JFrame
        this.setVisible(true);// 显示窗体
        // 锁定窗体
        this.setResizable(false);
    }

    private ChooseButton chooseButtonInit(){
        ChooseButton chooseButton = new ChooseButton(this, FileSystemView.getFileSystemView().getHomeDirectory(),
                  "请选择加载的配置文件",false, JFileChooser.FILES_ONLY).bind(ccb_loadParameter);
        return chooseButton;
    }
    private void ccbInit(){
        ccb_loadParameter = new CustomComboBox();
        ccb_loadParameter.setEditable(true);
        ccb_loadParameter.setPreferredSize(new Dimension(300,25));
        Vector<String> pFilePaths = FileUtils.getParameterFiles("d:\\config");
        if(pFilePaths != null && !pFilePaths.isEmpty()){
            for(String pFilePath : pFilePaths){
                String fileName = pFilePath.substring(pFilePath.lastIndexOf("\\") + 1);
                String projectName = fileName.substring(0, fileName.lastIndexOf("-"));
                ccb_loadParameter.addItem(projectName);
                pFilePathHash.put(projectName, pFilePath);
            }
        }
    }
    private JButton sureButtonInit(){
        JButton jb_sure = new JButton("确认");
        jb_sure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String pFileName = ((JTextField)ccb_loadParameter.getEditor().getEditorComponent()).getText();
                String pFilePath = (pFilePathHash.get(pFileName) == null) ? pFileName : pFilePathHash.get(pFileName);
                String[] parameterFiles = new String[]{pFilePath};
                //setProLocalPath(pFilePath);
                new MainUI().init(parameterFiles);
                LoadUI.this.dispose();
            }
        });
        return jb_sure;
    }


    public static void main(String[] args){
        new LoadUI().init();
    }

}

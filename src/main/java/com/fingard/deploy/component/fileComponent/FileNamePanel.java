package com.fingard.deploy.component.fileComponent;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by F on 2018/5/8.
 */
public class FileNamePanel extends JPanel {
    private IFileListOption option;
    private boolean isNeededSave = false;
    private int width;
    private String filePath = "";
    private String fileContent = null;
    private FileNamePanel next = null;
    private FileNamePanel pre = null;

    public FileNamePanel(final String filePath, final IFileListOption option){
        super();
        this.filePath = filePath;
        this.option = option;
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 5 , 0));
        this.setBackground(Color.LIGHT_GRAY);
        this.add(initFileNameLabel());
        this.add(initCloseButton());
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {

            }
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                option.showContent(FileNamePanel.this);
            }
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {}
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {}
            @Override
            public void mouseExited(MouseEvent mouseEvent) {}
        });
        this.setPreferredSize(new Dimension(width,30));
    }

    private JLabel initFileNameLabel(){
        String fn = filePath.substring(filePath.lastIndexOf("\\") + 1);
        final JLabel fileName = new JLabel(fn);
        fileName.setFont(new Font("Times New Roman", Font.BOLD, 14));
        fileName.setPreferredSize(new Dimension(calculateWidth(fn.length()),30));
        return fileName;
    }
    private JLabel initCloseButton(){
        final JLabel jl_close = new JLabel("×");
        jl_close.setFont(new Font("Times New Roman", Font.BOLD, 20));
        jl_close.setForeground(Color.gray);
        jl_close.setPreferredSize(new Dimension(25,25));
        jl_close.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(fileContent.equals(option.getCurrentFileContent()) && !isNeededSave)
                    option.removeFile(FileNamePanel.this);
                else{
                    int result = JOptionPane.showConfirmDialog(FileNamePanel.this,
                            "当前文件已修改，是否保存？");
                    if(JOptionPane.CANCEL_OPTION  == result) {
                        return;
                    }else {
                        if(JOptionPane.YES_OPTION == result){
                            option.save(FileNamePanel.this);
                        }
                        option.removeFile(FileNamePanel.this);
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent mouseEvent) {}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
            }
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                jl_close.setForeground(Color.black);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                jl_close.setForeground(Color.gray);
            }
        });
        return jl_close;
    }

    public boolean hasPre(){
        return (pre != null);
    }
    public boolean hasNext(){
        return (next != null);
    }

    /**
     * 设置容器总宽度以及文件名标签宽度
     * @param size 文件名字节数
     * @return 返回文件名标签最小宽度
     */
    private int calculateWidth(int size){
        width = size*7 + 35;
        return width - 35;
    }
    public boolean isContentEmpty(){
        return fileContent == null;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public FileNamePanel getNext() {
        return next;
    }

    public void setNext(FileNamePanel next) {
        this.next = next;
    }

    public FileNamePanel getPre() {
        return pre;
    }

    public void setPre(FileNamePanel pre) {
        this.pre = pre;
    }

    public boolean isNeededSave() {
        return isNeededSave;
    }

    public void setNeededSave(boolean neededSave) {
        isNeededSave = neededSave;
    }
}

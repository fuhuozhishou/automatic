package com.fingard.deploy.component.fileComponent;

import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 用双向链表实现文件名显示标签栏
 * @author zhangym
 * @version 1.0  2018/5/8
 */
public class FileListPanel extends JPanel implements IFileListOption {
    private int size = 0;
    private FileNamePanel head;
    private FileNamePanel tail;
    private FileNamePanel selected;
    private FileContentShow fileContent;
    private JLabel filePath;

    public FileListPanel(){
        super();
        this.setPreferredSize(new Dimension(900,30));
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0 , 0));
        this.setBorder(new EmptyBorder(0,0,0,0));
        this.setBackground(Color.LIGHT_GRAY);

    }
    //绑定JTextArea
    public FileListPanel bind(FileContentShow fileContent, JLabel filePath){
        this.fileContent = fileContent;
        this.filePath = filePath;
        return this;
    }

    //删除操作，如果被删除组件处于选中状态，那么优先显示后一个组件
    public void removeFile(FileNamePanel fileNamePanel){
        if(fileNamePanel.hasPre()){
            if(fileNamePanel.hasNext()){
                FileNamePanel fnp = fileNamePanel.getNext();
                if(selected == fileNamePanel){
                    showContent(fnp);
                }
                fileNamePanel.getPre().setNext(fnp);
                fnp.setPre(fileNamePanel.getPre());
            }else{
                if(selected == fileNamePanel){
                    showContent(fileNamePanel.getPre());
                }
                fileNamePanel.getPre().setNext(null);
                tail = fileNamePanel.getPre();
            }
        } else {
            if(fileNamePanel.hasNext()){
                if(selected == fileNamePanel){
                    showContent(fileNamePanel.getNext());
                }
                fileNamePanel.getNext().setPre(null);
                head = fileNamePanel.getNext();
            }else{
                head = null;
                tail = null;
                selected = null;
                fileContent.setText("");
                filePath.setText("文件路径：");
            }
        }
        this.remove(fileNamePanel);
        size--;
        this.updateUI();
        this.repaint();
    }

    /**
     * 往尾部添加新的编辑文件（带去重）
     * @param filePath
     */
    /*public void addFile(String filePath){
        if(isRepeat(filePath)){
            System.out.println(filePath + "  该路径对应的文件已打开。");
            return;
        }
        FileNamePanel fileNamePanel = new FileNamePanel(filePath, this);;
        if(size == 0){
            head = fileNamePanel;
            tail = fileNamePanel;
        }else {
            tail.setNext(fileNamePanel);
            fileNamePanel.setPre(tail);
            tail = fileNamePanel;
        }
        showContent(fileNamePanel);
        size++;
        this.add(fileNamePanel);
        this.updateUI();
        this.repaint();
    }*/

    /**
     * 往头部添加新的编辑文件
     * @param filePath
     */
    public void addFile(String filePath){
        if(isRepeat(filePath)){
            System.out.println(filePath + "  该路径对应的文件已打开。");
            return;
        }
        FileNamePanel fileNamePanel = new FileNamePanel(filePath, this);;
        if(size == 0){
            head = fileNamePanel;
            tail = fileNamePanel;
        }else {
            head.setPre(fileNamePanel);
            fileNamePanel.setNext(head);
            head = fileNamePanel;
        }
        showContent(fileNamePanel);
        size++;
        this.add(fileNamePanel,0);
        this.updateUI();
        this.repaint();
    }

    private boolean isRepeat(String filePath){
        FileNamePanel traverse = head;
        while(traverse != null){
            if(filePath.equals(traverse.getFilePath())){
                return true;
            }
            traverse = traverse.getNext();
        }
        return false;
    }

    public FileNamePanel getSelected(){
        return selected;
    }
    public FileNamePanel getHead(){
        return head;
    }
    /**
     * 设置选中操作
     * @param s
     */
    public void setSelected(FileNamePanel s){
        if(selected == s){
            return;
        }
        if(selected != null){
            if(fileContent.isModified()){
                System.out.println("当前文件已改变！");
                selected.setFileContent(fileContent.getText());
                selected.setNeededSave(true);
            }
            selected.setBackground(Color.LIGHT_GRAY);
        }
        selected = s;
        selected.setBackground(Color.white);
    }
    public void showContent(FileNamePanel fileNamePanel){
        setSelected(fileNamePanel);
        String fc = fileNamePanel.getFileContent();
        //若fileContent为空，说明为第一次显示，需要从文件中读取
        if(fc == null){
            fc = FileUtils.readFile(fileNamePanel.getFilePath());
            fileNamePanel.setFileContent(fc);
            fileNamePanel.setNeededSave(false);
        }
        fileContent.setText(fc);
        fileContent.setModified(false);
        filePath.setText("文件路径：" + fileNamePanel.getFilePath());
    }
    public String getCurrentFileContent(){
        return fileContent.getText();
    }
    //保存操作。
    public void save(FileNamePanel fileNamePanel){
        FileUtils.textAreaToFile(fileContent,fileNamePanel.getFilePath());
        fileNamePanel.setFileContent(fileContent.getText());
        fileContent.setModified(false);
        fileNamePanel.setNeededSave(false);
    }
}

package com.fingard.deploy.component.fileComponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author zhangym
 * @version 1.0  2018/5/9
 */
public class FileContentShow extends JTextArea {
    private boolean isModified = false;
    public FileContentShow(){
        super(28, 80);
        //文件内容设置
        this.setEditable(true);
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                isModified = true;
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                isModified = true;
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                isModified = true;
            }
        });
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }
}

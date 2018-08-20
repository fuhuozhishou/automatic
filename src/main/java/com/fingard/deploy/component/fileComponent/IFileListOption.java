package com.fingard.deploy.component.fileComponent;

/**
 * Created by F on 2018/5/8.
 */
public interface IFileListOption {
    void addFile(String filePath);
    void removeFile(FileNamePanel fileNamePanel);
    void showContent(FileNamePanel fileNamePanel);
    void setSelected(FileNamePanel fileNamePanel);
    //获取当前JTextArea上的内容
    String getCurrentFileContent();
    void save(FileNamePanel fileNamePanel);
}

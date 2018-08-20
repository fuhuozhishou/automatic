package com.fingard.deploy.component;

import com.fingard.deploy.component.fileComponent.FileContentShow;
import com.fingard.deploy.component.fileComponent.FileListPanel;
import com.fingard.deploy.component.fileComponent.FileNamePanel;
import com.fingard.deploy.utils.KeyWordHelper;
import com.fingard.deploy.utils.SearchFile;
import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * @author zhangym
 * @version 1.0  2018/4/23
 */
public class ConfigUI extends JFrame{

    private KeyWordHelper dirKeyWordHelper = new KeyWordHelper("d:\\config\\dirKeyWords.properties");
    private KeyWordHelper fileKeyWordHelper = new KeyWordHelper("d:\\config\\fileKeyWords.properties");

    private JPanel jp_search, jp_select, jp_edit;
    private MultiSelectComboBox multiSelectComboBox;
    private CustomComboBox fileKeyWord;
    private Thread searchThread;

    private JTextField dirKeyWord;
    private FileContentShow fileContent;

    private Vector<String> filePaths;
    private FileListPanel fileListPanel;


    public void init(){

        initSearch();
        initSelect();
        initEdit();

        JPanel jPanel4 = new JPanel();
        final JButton save = new JButton("保存");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileNamePanel current = fileListPanel.getSelected();
                if(current == null)
                    return;
                if(!fileContent.isModified() && !current.isNeededSave()){
                    System.out.println("该文件没有变化，无需保存。");
                } else{
                    fileListPanel.save(current);
                }
            }
        });
        jPanel4.add(save);

        this.setLayout(new FlowLayout());
        this.add(jp_search);
        this.add(jp_select);
        this.add(jp_edit);
        this.add(jPanel4);

        this.setTitle("配置文件搜索 v2.0");// 窗体标签
        this.setSize(1000, 900);// 窗体大小
        this.setLocationRelativeTo(null);// 在屏幕中间显示(居中显示)
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                FileNamePanel traverse = fileListPanel.getHead();
                FileNamePanel select = fileListPanel.getSelected();
                while(traverse != null){
                    if(traverse == select){
                        FileUtils.textAreaToFile(fileContent, traverse.getFilePath());
                    }else if(traverse.isNeededSave()){
                        FileUtils.stringToFile(traverse.getFileContent(), traverse.getFilePath());
                    }
                    traverse = traverse.getNext();
                }
                ConfigUI.this.dispose();
            }
        });
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// 退出关闭JFrame
        this.setVisible(true);// 显示窗体
        // 锁定窗体
        this.setResizable(false);

    }

    public static void main(String[] args){
        new ConfigUI().init();
    }


    //初始化搜索栏
    private void initSearch(){

        jp_search = new JPanel();
        dirKeyWord = new JTextField();
        dirKeyWord.setPreferredSize(new Dimension(400, 26));
        final DirButton jb_dir = new DirButton(ConfigUI.this, new File(MainUI.PROJECT_ROOTPATH),
                "目录","请选择搜索的目录",JFileChooser.FILES_AND_DIRECTORIES ).bind(dirKeyWord);
        final JLabel jLabel2 = new JLabel("文件");
        initSingleSelectBox();
        jp_search.add(jb_dir);
        jp_search.add(dirKeyWord);
        jp_search.add(jLabel2);
        jp_search.add(fileKeyWord);
        jp_search.add(search());
    }
    //初始化选择栏
    private void initSelect(){
        jp_select = new JPanel();
        initSelectBox();
        jp_select.add(multiSelectComboBox);
        jp_select.add(replace());
        jp_select.add(edit());
    }
    //初始化编辑栏
    private void initEdit(){
        jp_edit = new JPanel();
        jp_edit.setLayout(new FlowLayout(FlowLayout.CENTER,25,0));
        jp_edit.setPreferredSize(new Dimension(950,520));
        JLabel fileName = new JLabel("文件路径:", JLabel.LEFT);
        fileName.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        fileName.setPreferredSize(new Dimension(new Dimension(900,25)));
        fileContent = new FileContentShow();
        fileListPanel = new FileListPanel().bind(fileContent,fileName);

        JScrollPane jslpFC = new JScrollPane(fileContent); //给file加滚动条
        //竖直滚动条总是显示
        jslpFC.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //水平滚动条总是显示
        jslpFC.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jp_edit.add(fileListPanel);
        jp_edit.add(fileName);
        jp_edit.add(jslpFC);
    }

    //搜索按钮
    private JButton search(){
        final JButton search = new JButton("搜索");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                if(searchThread == null || !searchThread.isAlive()){
                    searchThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            multiSelectComboBox.setEnabled(false);
                            search.setText("搜索中");
                            multiSelectComboBox.clearSelectedIndexs();
                            multiSelectComboBox.removeAllItems();
                            String dirKey = dirKeyWord.getText();
                            String fileKey = ((JTextField)fileKeyWord.getEditor().getEditorComponent()).getText();
                            dirKeyWordHelper.addKeyWord(dirKey);
                            fileKeyWordHelper.addKeyWord(fileKey);
                            fileKeyWord.addItem(fileKey);
                            filePaths = SearchFile.getFiles(dirKey, fileKey);
                            for(String filePath : filePaths){
                                multiSelectComboBox.addItem(filePath);
                            }
                            search.setText("搜索");
                            multiSelectComboBox.setEnabled(true);
                        }
                    });
                    searchThread.start();
                }
            }
        });
        return search;
    }
    //替换按钮
    private JButton replace(){
        final JButton replace = new JButton("全部替换");
        replace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final String replaceFilePath = FileUtils.getSelectedFilepath(ConfigUI.this,
                        "请选择替换的文件");
                new Thread(new Runnable(){
                    public void run(){
                        Set<Integer> selectItems = multiSelectComboBox.getSelectedIndexs();
                        Iterator iterator = selectItems.iterator();
                        final int filesSum = selectItems.size();
                        int count = 0;
                        while(iterator.hasNext()){
                            try{
                                FileUtils.copyFileUsingFileChannels(replaceFilePath, filePaths.get((Integer)iterator.next()));
                                //Thread.sleep(2000);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            count++;
                            final String str = "完成" + count + "/" + filesSum;
                            SwingUtilities.invokeLater(new Runnable(){
                                public void run(){
                                    //update the GUI
                                    replace.setText(str);
                                }
                            });
                        }
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                //update the GUI
                                replace.setText("全部替换");
                            }
                        });

                    }

                }).start();
            }
        });
        return replace;
    }
    //编辑按钮
    private JButton edit(){
        final JButton edit = new JButton("编辑");
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Set<Integer> selectItems = multiSelectComboBox.getSelectedIndexs();
                Iterator iterator = selectItems.iterator();
                while(iterator.hasNext()){
                    fileListPanel.addFile(filePaths.get((Integer) iterator.next()));
                }
            }
        });
        return edit;
    }

    @SuppressWarnings("unchecked")
    private void initSelectBox(){
        multiSelectComboBox = new MultiSelectComboBox();
        multiSelectComboBox.setPreferredSize(new Dimension(750,26));
        multiSelectComboBox.setForegroundAndToPopup(Color.RED);
        multiSelectComboBox.setPopupBorder(BorderFactory.createLineBorder(Color.RED));
        multiSelectComboBox.setPopupBackground(Color.DARK_GRAY);
        multiSelectComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                System.out.println("选择的值："+ multiSelectComboBox.getSelectedItemsString());
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }
    //初始化单选下拉框
    @SuppressWarnings("unchecked")
    private void initSingleSelectBox(){
        fileKeyWord = new CustomComboBox(fileKeyWordHelper.getKeyWords());
        fileKeyWord.setEditable(true);
        fileKeyWord.setPreferredSize(new Dimension(200,26));
    }


}

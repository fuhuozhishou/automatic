package com.fingard.deploy.component;

import com.fingard.deploy.command.*;
import com.fingard.deploy.commandQueue.CommandQueue;
import com.fingard.deploy.utils.KeyWordHelper;
import com.fingard.deploy.utils.MyProperties;
import com.fingard.deploy.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

/**
 * @author zhangym
 * @version 1.0  2018/4/26
 */
public class MainUI extends JFrame implements MessageShow{
    public static String PROJECT_ROOTPATH ;
    private String[] PROJECT_NAMES;
    private KeyWordHelper keyWordHelper = new KeyWordHelper("d:\\config\\deployPathKeyWords.properties");
    public static final int ERROR_MESSAGE = -1;
    public static final int DOWNLOAD_MESSAGE = 1;
    public static final int COMPLETE_FLAG = 2;
    private JTextArea downloadIfo, errorMessage;

    private JPanel jp_selectDir, jp_update, jp_pack, jp_upload, jp_message;

    private MultiSelectComboBox mscb_pack;
    private CustomComboBox ccb_deployPath, ccb_upload;

    private volatile boolean isUpdating = false;
    private volatile boolean isPacking = false;
    private volatile boolean isUploading = false;
    private volatile boolean isDeploying = false;

    private JLabel jl_message;
    private JTextField inputVersion;

    private CommandQueue svnCommandQueue, packCommandQueue, uploadCommandQueue, deployCommandQueue;

    private Map<String, String> defaultLocalPaths;
    private Map<String, String> defaultDeployPaths;

    public void init(String[] parameterFilePath){
        String pNames = MyProperties.getMyPropertiesInstance(parameterFilePath).getProperty("local.project.name");
        PROJECT_ROOTPATH = MyProperties.getMyPropertiesInstance().getProperty("svn.local.address");
        defaultLocalPaths = FileUtils.getParamsByHead(parameterFilePath[0], "local.path");
        defaultDeployPaths = FileUtils.getParamsByHead(parameterFilePath[0],"deploy.path");

        PROJECT_NAMES = pNames == null ? null : pNames.split(",");

        updateInit();
        packInit();
        uploadInit();
        messageInit();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.add(jp_selectDir);
        jPanel.add(jp_update);
        jPanel.add(jp_pack);
        jPanel.add(jp_upload);
        jPanel.add(jp_message);
        this.setLayout(new FlowLayout());
        this.add(jPanel);

        this.setTitle("自动化版本发布 v3.0");// 窗体标签
        this.setSize(1000, 650);// 窗体大小
        this.setLocationRelativeTo(null);// 在屏幕中间显示(居中显示)
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 退出关闭JFrame
        this.setVisible(true);// 显示窗体
        // 锁定窗体
        this.setResizable(false);

    }
    //更新栏初始化
    public void updateInit(){
        final JLabel jLabel = new JLabel("选择svn下载目录");
        final JTextField jtf_proDir = new JTextField();
        jtf_proDir.setPreferredSize(new Dimension(250, 25));
        final DirButton project_dir = new DirButton(MainUI.this, new File("c:\\"),
                "选择","请选择svn下载目录", JFileChooser.DIRECTORIES_ONLY).bind(jtf_proDir);
        jp_selectDir = new JPanel();
        jp_selectDir.add(jLabel);
        jp_selectDir.add(jtf_proDir);
        jp_selectDir.add(project_dir);

        final JButton config = new JButton("配置");
        config.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ConfigUI().init();
                    }
                }).start();
            }
        });
        jp_update = new JPanel();
        final JLabel jl_inVersion = new JLabel("请输入版本号：");
        inputVersion = new JTextField();
        inputVersion.setPreferredSize(new Dimension(100,25));
        final JButton update = new JButton("更新");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                errorMessage.setText("");
                if(svnCommandQueue == null){
                    svnCommandQueue = new CommandQueue(MainUI.this, "更新");
                }
                if(!isUpdating){
                    final String version = inputVersion.getText();
                    if(version == null){
                        showMessage("版本号不得为空！", -1);
                        return;
                    }
                    try{
                        String proPath = jtf_proDir.getText();
                        Long l = Long.parseLong(version);
                        if(proPath == null || proPath.equals("")){
                            if(PROJECT_ROOTPATH == null || PROJECT_ROOTPATH.equals(" ")){
                                errorMessage.append("默认项目路径不存在，请配置或选择新的目录");
                                return;
                            }
                            svnCommandQueue.addCommand(new SvnUpdateCommand(MainUI.this,
                                    l, PROJECT_ROOTPATH ));
                        }else{
                            if(!(new File(proPath).isDirectory())){
                                errorMessage.append("目录有误，请重新选择或输入" + "\r\n");
                                return;
                            }
                            svnCommandQueue.addCommand(new SvnUpdateCommand(MainUI.this,
                                    l, jtf_proDir.getText()));
                            mscb_pack.removeAllItems();
                        }
                        new Thread(svnCommandQueue).start();
                        isUpdating = true;
                    }catch (NumberFormatException e){
                        showMessage("版本号输入有误，请重新输入。", -1);
                    }
                }
            }
        });
        final JButton updateNewest = new JButton("最新");
        updateNewest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                errorMessage.setText("");
                if(!isUpdating){
                    String proPath = jtf_proDir.getText();
                    if(svnCommandQueue == null){
                        svnCommandQueue = new CommandQueue(MainUI.this, "更新");
                    }
                    if(proPath == null || proPath.equals("")){
                        if(PROJECT_ROOTPATH == null || PROJECT_ROOTPATH.equals(" ")){
                            errorMessage.append("默认项目路径不存在，请配置或选择新的目录");
                            return;
                        }
                        svnCommandQueue.addCommand(new SvnUpdateCommand(MainUI.this,
                                0, PROJECT_ROOTPATH ));
                    }else{
                        if(!(new File(proPath).isDirectory())){
                            errorMessage.append("目录有误，请重新选择或输入" + "\r\n");
                            return;
                        }
                        svnCommandQueue.addCommand(new SvnUpdateCommand(MainUI.this,
                                0, jtf_proDir.getText()));
                        mscb_pack.removeAllItems();
                    }

                    new Thread(svnCommandQueue).start();
                    isUpdating = true;
                }
            }
        });
        jp_update.add(jl_inVersion);
        jp_update.add(inputVersion);
        jp_update.add(update);
        jp_update.add(updateNewest);
        jp_update.add(config);
    }

    //打包栏初始化
    @SuppressWarnings("unchecked")
    public void packInit(){
        jp_pack = new JPanel();
        final JLabel jl_pack = new JLabel("选择打包项目");
        mscb_pack = (PROJECT_NAMES != null) ? new MultiSelectComboBox(PROJECT_NAMES) : new MultiSelectComboBox();
        mscb_pack.setPreferredSize(new Dimension(250,25));
        final ChooseButton choose_pack = new ChooseButton(MainUI.this, new File(PROJECT_ROOTPATH),
                "请选择要打包的项目", false, JFileChooser.DIRECTORIES_ONLY).bind(mscb_pack);
        final JButton pack = new JButton("打包");
        pack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                errorMessage.setText("");
                if(mscb_pack.isNonSelected())
                    return;
                if(packCommandQueue == null){
                    packCommandQueue = new CommandQueue(MainUI.this, "打包");
                }
                //对选中的项目进行处理
                for(Object i : mscb_pack.getSelectedIndexs()){
                    int selectIndex = (Integer)i;
                    String projectName;
                    String projectPath;
                    //如果选中的项目的索引超出默认项目数，说明是新添加的项目路径
                    if(selectIndex >= PROJECT_NAMES.length){
                        projectPath = (String) mscb_pack.getItemAt(selectIndex);
                        projectName = projectPath.substring(projectPath.lastIndexOf("\\") + 1);
                    } else {
                        projectName = (String) mscb_pack.getItemAt(selectIndex);
                        projectPath = FileUtils.getProjectPath(PROJECT_ROOTPATH, projectName);
                    }
                    packCommandQueue.addCommand(new PackCommand(projectName, MainUI.this,
                            projectPath));
                    ccb_upload.addItem(projectPath);
                }
                if(!isUpdating && !isPacking){
                    new Thread(packCommandQueue).start();
                    isPacking = true;
                }
            }
        });

        jp_pack.add(jl_pack);
        jp_pack.add(mscb_pack);
        jp_pack.add(choose_pack);
        jp_pack.add(pack);
    }

    //上传栏初始化
    @SuppressWarnings("unchecked")
    public void uploadInit(){
        jp_upload = new JPanel();
        final JLabel jLabel = new JLabel("选择部署的项目");
        //上传项目选择下拉框
        ccb_upload = new CustomComboBox();
        ccb_upload.setPreferredSize(new Dimension(250, 25));
        ccb_upload.setEditable(true);
        final JLabel jl_inDeployPath = new JLabel("部署路径");
        //部署路径下拉框初始化
        ccb_deployPath = new CustomComboBox(keyWordHelper.getKeyWords());
        ccb_deployPath.setEditable(true);
        ccb_deployPath.setSelectedIndex(-1);
        ccb_deployPath.setPreferredSize(new Dimension(250,25));
        final ChooseButton choose_upload = new ChooseButton(MainUI.this, new File(PROJECT_ROOTPATH),
                "请选择要部署的项目", false, JFileChooser.DIRECTORIES_ONLY).bind(ccb_upload);
        final JButton deploy = new JButton("部署");

        /**部署按钮功能：部署路径以及项目名为空时，读取配置信息实现一键部署 */
        deploy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                errorMessage.setText("");
                String deployPath = ((JTextField)ccb_deployPath.getEditor().getEditorComponent()).getText();
                String projectPath = (String)ccb_upload.getItemAt(ccb_upload.getSelectedIndex());
                //如果项目名为空，且部署路径为空，读取对应的配置项进行；其余情况给出错误提示
                if(projectPath == null || projectPath.equals("")){
                    if(deployPath == null || deployPath.equals("")){
                        String projectLocalPath = MyProperties.getMyPropertiesInstance().getProperty("project.local.path");
                        if(projectLocalPath == null){
                            showMessage("不存在默认的项目本地路径", 1);
                            return;
                        }else {
                            String projectDeployPath = MyProperties.getMyPropertiesInstance().getProperty("project.deploy.path");
                            if(projectDeployPath == null){
                                showMessage("不存在默认的项目部署路径", 1);
                                return;
                            }else {
                                if(svnCommandQueue == null){
                                    svnCommandQueue = new CommandQueue(MainUI.this, "更新");
                                }
                                if(packCommandQueue == null){
                                    packCommandQueue = new CommandQueue(MainUI.this, "打包");
                                }
                                if(uploadCommandQueue == null){
                                    uploadCommandQueue = new CommandQueue(MainUI.this, "上传");
                                }
                                if(deployCommandQueue == null){
                                    deployCommandQueue = new CommandQueue(MainUI.this, "部署");
                                }
                                svnCommandQueue.addCommand(new SvnUpdateCommand(MainUI.this,
                                        0, PROJECT_ROOTPATH ));
                                String[] plpList = projectLocalPath.split(",");
                                String[] pdpList = projectDeployPath.split(",");
                                for(int i=0; i<plpList.length; i++){
                                    String projectName = plpList[i].substring(plpList[i].lastIndexOf("\\") + 1);

                                    packCommandQueue.addCommand(new PackCommand(projectName,MainUI.this,
                                            plpList[i]));
                                    uploadCommandQueue.addCommand(new UploadCommand(projectName,
                                            MainUI.this, pdpList[i], plpList[i]));
                                    deployCommandQueue.addCommand(new DeployCommand(projectName,
                                            MainUI.this, pdpList[i]));
                                }
                                if(!isUpdating){
                                    new Thread(svnCommandQueue).start();
                                    isUpdating = true;
                                }
                                return;
                            }
                        }
                    }else {
                        showMessage("请选择要部署的项目", 1);
                        return;
                    }
                }else {
                    if(deployPath == null || deployPath.equals("")){
                        showMessage("请选择或输入要部署的路径", 1);
                        return;
                    }
                }
                //记忆选中项
                keyWordHelper.addKeyWord(deployPath);
                ccb_deployPath.addItem(deployPath);
                if(uploadCommandQueue == null){
                    uploadCommandQueue = new CommandQueue(MainUI.this, "上传");
                }
                if(deployCommandQueue == null){
                    deployCommandQueue = new CommandQueue(MainUI.this, "部署");
                }
                String projectName = projectPath.substring(projectPath.lastIndexOf("\\") + 1);
                uploadCommandQueue.addCommand(new UploadCommand(projectName,
                        MainUI.this, deployPath, projectPath));
                deployCommandQueue.addCommand(new DeployCommand(projectName,
                        MainUI.this, deployPath));
                if(!isPacking && !isUploading){
                    new Thread(uploadCommandQueue).start();
                    isUploading = true;
                }
            }
        });

        final JButton start = new JButton("启动");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                errorMessage.setText("");
                String deployPath = ((JTextField)ccb_deployPath.getEditor().getEditorComponent()).getText();
                String projectPath = (String)ccb_upload.getItemAt(ccb_upload.getSelectedIndex());
                if(projectPath == null || projectPath.equals("")){
                    showMessage("请选择要部署的项目", 1);
                    return;
                }else if(deployPath == null || deployPath.equals("")){
                    showMessage("请选择要部署的路径", 1);
                    return;
                }
                keyWordHelper.addKeyWord(deployPath);
                ccb_deployPath.addItem(deployPath);
                String projectName = projectPath.substring(projectPath.lastIndexOf("\\") + 1);
                if(deployCommandQueue == null){
                    deployCommandQueue = new CommandQueue(MainUI.this, "部署");
                }
                deployCommandQueue.addCommand(new DeployCommand(projectName,
                        MainUI.this, deployPath));
                if(!isDeploying){
                    new Thread(deployCommandQueue).start();
                    isDeploying = true;
                }
            }
        });

        jp_upload.add(jLabel);
        jp_upload.add(ccb_upload);
        jp_upload.add(choose_upload);
        jp_upload.add(jl_inDeployPath);
        jp_upload.add(ccb_deployPath);
        jp_upload.add(deploy);
        jp_upload.add(start);
        //jp_upload.add(retry);
    }

    //信息栏初始化
    public void messageInit(){
        jp_message = new JPanel();
        jp_message.setBackground(Color.GRAY);
        jp_message.setLayout(new BoxLayout(jp_message, BoxLayout.Y_AXIS));
        jl_message = new JLabel("更新信息");
        jl_message.setPreferredSize(new Dimension(100, 25));
        downloadIfo = new JTextArea(10, 30);
        downloadIfo.setEditable(false);
        JScrollPane jslp1 = new JScrollPane(downloadIfo); //给file加滚动条
        //竖直滚动条总是显示
        jslp1.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //水平滚动条总是显示
        jslp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        final JLabel jl_error = new JLabel("错误信息");
        jl_error.setPreferredSize(new Dimension(100, 25));
        //jl_error.setBackground(Color.gray);
        errorMessage = new JTextArea(10, 30);
        errorMessage.setEditable(false);
        JScrollPane jslp2 = new JScrollPane(errorMessage); //给file加滚动条
        jslp2.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jslp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jp_message.add(jl_message);
        jp_message.add(jslp1);
        jp_message.add(jl_error);
        jp_message.add(jslp2);
    }

    public void showMessage(final String message, int messCategory){
        switch (messCategory){
            case ERROR_MESSAGE:
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        errorMessage.append(message + "\r\n");
                    }
                });
                break;
            case DOWNLOAD_MESSAGE:
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        //update the GUI
                        if(errorMessage.getText().length() > 50000)
                            downloadIfo.setText(message + "\r\n");
                        else {
                            downloadIfo.append(message + "\r\n");
                        }
                    }
                });
                break;
            case COMPLETE_FLAG:
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        ccb_upload.removeItem(message);
                    }
                });
                break;

        }
    }

    /**
     * 根据处理队列传来进度信息，更新状态信息，唤醒下一级队列
     * @param progress 进度
     * @return void
     * @author zhangym v1.0  2018/6/7
     */
    public void showProgress(final String progress){
        //System.out.println(progress);
        if(progress.endsWith("完成"))
            showMessage(progress, 1);
        if(progress.startsWith("更新")){
            if(progress.endsWith("完成")){
                svnCommandQueue = null;
                if(packCommandQueue != null){
                    new Thread(packCommandQueue).start();
                }
            }
            isUpdating = false;
        }else if (progress.startsWith("打包")){
            if(progress.endsWith("完成")){
                packCommandQueue = null;
                if(uploadCommandQueue != null){
                    new Thread(uploadCommandQueue).start();
                }
            }
            isPacking = false;
        } else if(progress.startsWith("上传")) {
            if(progress.endsWith("完成")){
                uploadCommandQueue = null;
                if(deployCommandQueue != null){
                    new Thread(deployCommandQueue).start();
                }
            }
            isUploading = false;
        }else if(progress.startsWith("部署")){
            if(progress.endsWith("完成")){
                deployCommandQueue = null;
            }
            isDeploying = false;
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    //update the GUI
                    jl_message.setText(progress);
                }
            });
        }

    }

    public static void main(String[] args){
        String[] s = new String[]{"d:\\config\\rhf-parameter.properties"};
        new MainUI().init(s);

    }

}

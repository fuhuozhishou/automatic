package com.fingard.deploy.command;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.utils.MyProperties;
import com.jcraft.jsch.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author zhangym
 * @version 1.0  2018/4/28
 */
public class UploadCommand extends Command {
    private static final String ip = MyProperties.getMyPropertiesInstance().getProperty("linux.ip");
    private static final String username = MyProperties.getMyPropertiesInstance().getProperty("linux.username");
    private static final String password = MyProperties.getMyPropertiesInstance().getProperty("linux.password");
    private static final String port = MyProperties.getMyPropertiesInstance().getProperty("linux.port");

    private String projectPath;
    private String deployPath = null;

    public UploadCommand(String projectName, MessageShow messageShow) {
        super(projectName, messageShow);
    }

    public UploadCommand(String projectName, MessageShow messageShow, String deployPath, String projectPath){
        super(projectName, messageShow);
        this.deployPath = deployPath;
        this.projectPath = projectPath;
    }
    public int execute(){
        getMessageShow().showProgress("上传信息");
        setState(Command.CMD_STATE_EXECUTING);
        upload();
        return getState();
    }

    private void upload(){
        try{
            sftp();
            if(getState() != Command.CMD_STATE_FAIL){
                setState(Command.CMD_STATE_SUCCESS);
                getMessageShow().showMessage("项目 " + getProjectName() + " 上传成功",
                        1);
            }else {
                setState(Command.CMD_STATE_FAIL);
                getMessageShow().showMessage("项目 " + getProjectName() + " 上传失败", 1);
            }
        }catch (Exception e) {
            setState(Command.CMD_STATE_FAIL);
            getMessageShow().showMessage(e.getMessage(), -1);
            getMessageShow().showMessage("项目 " + getProjectName() + " 上传失败", 1);
        }
    }

    /**
     * 先重命名linux系统上的文件夹，然后将打包后的文件上传到指定目录下
     * @throws Exception 异常
     * @author zhangym v1.0  2018/6/7
     */
    private void sftp() throws Exception{
        SFTPUtil sftpUtil = new SFTPUtil(ip, username, password, port);
        //重命名linux文件
        ChannelSftp chsftp = sftpUtil.getChSftp();
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String date = sdf.format(new Date());
        String projectName = getProjectName();
        String linuxProjectName = projectName + "-1.0.0";
        sftpUtil.renameFile(deployPath, linuxProjectName, linuxProjectName + date, chsftp);
        //上传打包后的文件到linux上
        String localProjectPath = projectPath + "\\target\\" + projectName + "-1.0.0";
        sftpUtil.put(localProjectPath, deployPath);
        sftpUtil.close();

    }

    class SFTPUtil{
        public static final String SFTP_REQ_HOST = "host";
        public static final String SFTP_REQ_PORT = "port";
        public static final String SFTP_REQ_USERNAME = "username";
        public static final String SFTP_REQ_PASSWORD = "password";
        public static final int SFTP_DEFAULT_PORT = 22;
        private Session session = null;
        private Channel channel = null;
        private ChannelSftp chSftp;
        public ChannelSftp getChSftp() {
            return chSftp;
        }
        /**
         *  <默认构造函数>
         */
        public SFTPUtil(Map<String, String> sftpDetails) {
            try {
                chSftp = getChannel(sftpDetails, 60000);
            }catch (JSchException e){
                e.printStackTrace();
            }
        }
        /**
         *
         *  <默认构造函数>
         */
        public SFTPUtil(String ip, String userName, String pwd, String port) throws JSchException{
            Map<String, String> sftpDetails = new HashMap<String, String>(); // 设置主机ip，端口，用户名，密码
            sftpDetails.put(SFTPUtil.SFTP_REQ_HOST, ip);
            sftpDetails.put(SFTPUtil.SFTP_REQ_USERNAME, userName);
            sftpDetails.put(SFTPUtil.SFTP_REQ_PASSWORD, pwd);
            sftpDetails.put(SFTPUtil.SFTP_REQ_PORT, port);
            try {
                chSftp = getChannel(sftpDetails, 60000);
            }catch (JSchException e){
                throw new JSchException("连接异常：" + e.getMessage());
            }
        }

        /**
         *  上传文件，支持文件夹上传 <一句话功能简述> <功能详细描述>
         *
         * @param sPath 源文件目录
         * @param dPath 目标文件夹
         * @throws Exception 异常
         * @see [类、类#方法、类#成员]
         */
        public void put(String sPath, String dPath)throws Exception{
            try{
                chSftp.cd(dPath);
            }catch (SftpException e){
                chSftp.mkdir(dPath);
                chSftp.cd(dPath);
            }
            File file = new File(sPath);
            copyFile(file, chSftp.pwd());
        }

        /**
         * 关闭连接 <一句话功能简述> <功能详细描述>
         * @see [类、类#方法、类#成员]
         */
        public void close(){
            chSftp.quit();
            try{
                closeChannel();
            }catch (Exception e){

            }
        }

        /**
         * 拷贝文件递归方法 <一句话功能简述> <功能详细描述>
         *
         * @param file
         * @param pwd
         * @see [类、类#方法、类#成员]
         */
        private void copyFile(File file, String pwd) throws Exception{
            if (file.isDirectory()){
                File[] list = file.listFiles();
                try{
                    String fileName = file.getName();
                    chSftp.cd(pwd);
                    String str1 = "正在创建目录:" + chSftp.pwd() + "/" + fileName;
                    System.out.println(str1);
                    chSftp.mkdir(fileName);
                    str1 = "目录创建成功:" + chSftp.pwd() + "/" + fileName;
                    System.out.println(str1);
                }catch (Exception e){
                    throw new Exception("创建目录 " + chSftp.pwd() + "/" +
                            file.getName() + "异常:" + e.getMessage());
                }
                pwd = pwd + "/" + file.getName();
                try{
                    chSftp.cd(file.getName());
                }catch (SftpException e){
                    throw new SftpException(1, "进入" + file.getName() + "异常: " + e.getMessage());
                }

                for (int i = 0; i < list.length; i++){
                    copyFile(list[i], pwd);
                }
            }else{
                try{
                    chSftp.cd(pwd);
                } catch (SftpException e1){
                    throw new SftpException(1, "进入" + pwd + "异常：" + e1.getMessage());
                }
                //messageShow.showMessage("正在复制文件:" + file.getAbsolutePath(), 1);
                System.out.println("正在复制文件:" + file.getAbsolutePath());
                InputStream instream = null;
                OutputStream outstream = null;
                try{
                    outstream = chSftp.put(file.getName());
                    instream = new FileInputStream(file);
                    byte b[] = new byte[1024];
                    int n;
                    try{
                        while ((n = instream.read(b)) != -1){
                            outstream.write(b, 0, n);
                        }
                    }catch (IOException e){
                        throw new IOException("复制文件 " + file.getAbsolutePath() + "异常: " + e.getMessage());
                    }
                }catch (SftpException e){
                    throw new SftpException(2,"复制文件 " + file.getAbsolutePath() + "异常: " + e.getMessage());
                }catch (IOException e){
                    throw new IOException("复制文件 " + file.getAbsolutePath() + "异常: " + e.getMessage());
                }finally{
                    try {
                        if(outstream != null){
                            outstream.flush();
                            outstream.close();
                        }
                        if(instream != null)
                            instream.close();
                    }catch (Exception e2){
                        e2.printStackTrace();
                    }
                }
            }
        }

        /**
         * 获取ChannelSftp <一句话功能简述> <功能详细描述>
         * @param sftpDetails
         * @param timeout
         * @return
         * @throws JSchException
         * @author yuanxq  v1.0   2018年1月18日
         */
        private ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout) throws JSchException{
            String ftpHost = sftpDetails.get(SFTPUtil.SFTP_REQ_HOST);
            String port = sftpDetails.get(SFTPUtil.SFTP_REQ_PORT);
            String ftpUserName = sftpDetails.get(SFTPUtil.SFTP_REQ_USERNAME);
            String ftpPassword = sftpDetails.get(SFTPUtil.SFTP_REQ_PASSWORD);
            int ftpPort = SFTPUtil.SFTP_DEFAULT_PORT;
            if (port != null && !port.equals("")){
                ftpPort = Integer.valueOf(port);
            }
            JSch jsch = new JSch(); // 创建JSch对象
            session = jsch.getSession(ftpUserName,ftpHost, ftpPort); // 根据用户名，主机ip，端口获取一个Session对象
            getMessageShow().showMessage("项目 " + getProjectName() +
                    " 开始上传....", 1);
            System.out.println("Session created.");
            if (ftpPassword != null){
                session.setPassword(ftpPassword); // 设置密码
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config); // 为Session对象设置properties
            session.setTimeout(timeout); // 设置timeout时间
            session.connect(); // 通过Session建立链接
            System.out.println("Session connected.");
            System.out.println("Opening Channel.");
            channel = session.openChannel("sftp"); // 打开SFTP通道
            channel.connect(); // 建立SFTP通道的连接

            System.out.println("Connected successfully to ftpHost = " + ftpHost + ",as ftpUserName = " + ftpUserName
                    + ", returning: " + channel);
            return (ChannelSftp)channel;
        }
        /**
         * 关闭 <一句话功能简述> <功能详细描述>
         *
         * @throws Exception
         * @see [类、类#方法、类#成员]
         */
        private void closeChannel()throws Exception{
            if (channel != null){
                channel.disconnect();
            }
            if (session != null){
                session.disconnect();
            }
        }
        /**
         * 文件重命名
         * @param directory 远程系统上文件所在目录
         * @param oldname 旧文件名
         * @param newname 新文件名
         * @param sftp
         */
        public void renameFile(String directory, String oldname, String newname,ChannelSftp sftp) throws Exception{
            try {
                sftp.cd(directory);
                sftp.rename(oldname, newname);
            }catch (Exception e){
                throw new Exception("文件重命名异常：" + e.getMessage());
            }
        }
    }

}

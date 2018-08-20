package com.fingard.deploy.logreader;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.utils.MyProperties;
import com.jcraft.jsch.SftpException;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
/**
 * @author zhangym
 * @version 1.0  2018/5/2
 */
public class LogReader implements Runnable {
    private static final String userName = MyProperties.getMyPropertiesInstance().
            getProperty("linux.username");
    private static final String password = MyProperties.getMyPropertiesInstance().
            getProperty("linux.password");
    private static final String ip = MyProperties.getMyPropertiesInstance().
            getProperty("linux.ip");
    private File logFile = null;
    private long lastTimeFileSize = 0; // 上次文件大小
    private String localLogPath = null;
    private String remoteLogPath = null;

    private boolean hasCreated = false;//判断远程日志文件是否已经创建
    private boolean isError = false;//判断日志文件是否出现错误

    private MessageShow messageShow;
    private CountDownLatch countDownLatch;


    /**初始化，将远程服务器上tomcat的日志文件复制到本地指定目录下*/
    public LogReader(String projectName, String tomcatPath, CountDownLatch countDownLatch,
                     MessageShow messageShow){
        this.countDownLatch = countDownLatch;
        this.messageShow = messageShow;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        remoteLogPath = tomcatPath + "/logs/catalina." + dateFormat.format(new Date()) + ".log";
        localLogPath = "d:/LocalCatalina/" + projectName + "-catalina.out";
        logFile = new File(localLogPath);
        lastTimeFileSize = logFile.length();
        System.out.println("文件初始长度：" + lastTimeFileSize);
    }

    public boolean isError(){
        return isError;
    }

    /**
     * 每隔2秒输出日志信息，每次从上次的末尾读取，根据读取到的
     * 日志信息判断服务是否成功启动
     */
    public void run() {
        while (true) {

            try {
                getRemoteLog();
                long len = logFile.length();
                if (len < lastTimeFileSize) {
                    System.out.println("Log file was reset. Restarting logging from start of file.");
                    lastTimeFileSize = len;
                } else if(len > lastTimeFileSize) {
                    //从上次获取日志的末尾开始读取
                    RandomAccessFile randomFile = new RandomAccessFile(logFile, "r");
                    randomFile.seek(lastTimeFileSize);
                    String tmp ;
                    while ((tmp = randomFile.readLine()) != null) {
                        if(!isError){
                            if(tmp.startsWith("INFO: Server startup")){
                                countDownLatch.countDown();
                                return;
                            }
                            /*if(tmp.startsWith("SEVERE:")){
                                isError = true;
                                messageShow.showMessage(tmp, -1);
                            }*/
                        }else {
                            messageShow.showMessage(tmp, -1);
                        }
                    }
                    if(isError){
                        countDownLatch.countDown();
                        return;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getRemoteLog(){
        SshClient client = new SshClient();
        try{
            //忽略认证
            client.connect(ip, new IgnoreHostKeyVerification());
            //设置用户名和密码
            PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
            pwd.setUsername(userName);
            pwd.setPassword(password);
            int result = client.authenticate(pwd);
            if(result == AuthenticationProtocolState.COMPLETE) {//如果连接完成
                OutputStream os = new FileOutputStream(localLogPath);
                //获取当天tomcat启动日志
                if(!hasCreated){
                    waitforCreated(client);
                }else {
                    client.openSftpClient().get(remoteLogPath, os);
                }
                //以行为单位读取文件start
                logFile = new File(localLogPath);
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 等待服务日志文件的创建
     * @param client
     * @author zhangym v1.0  2018/6/11
     */

    private void waitforCreated(SshClient client){
        while(!hasCreated){
            try {
                client.openSftpClient().get(remoteLogPath);
                System.out.println("日志文件已创建。");
                hasCreated = true;
                return;
            } catch (IOException e){
                System.out.println("日志文件未创建。");
                hasCreated = false;
            }
            if(!hasCreated){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
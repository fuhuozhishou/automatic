package com.fingard.deploy.command;

import ch.ethz.ssh2.Session;
import com.fingard.deploy.component.MainUI;
import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.linux.LinuxCommand;
import com.fingard.deploy.logreader.LogReader;
import com.fingard.deploy.utils.MyProperties;
import java.util.concurrent.CountDownLatch;


public class DeployCommand extends Command {
    private static final String ip = MyProperties.getMyPropertiesInstance()
            .getProperty("linux.ip");
    private static final String username = MyProperties.getMyPropertiesInstance()
            .getProperty("linux.username");
    private static final String password = MyProperties.getMyPropertiesInstance()
            .getProperty("linux.password");

    private String deployPath = null;
    public DeployCommand(String projectName, MessageShow messageShow, String deployPath) {
        super(projectName, messageShow);
        this.deployPath = deployPath;
    }
    public int execute(){
        setState(Command.CMD_STATE_EXECUTING);
        restart();
        return getState();
    }

    /**
     * 部署命令首先杀死远程linux系统上占用该项目端口的进程，然后定时读取项目tomcat
     * 日志判断服务是否成功启动。
     * @author zhangym v1.0  2018/6/7
     */

    private void restart(){
        //杀死占用该项目端口的进程
        killProgress();
        String tomcatPath = deployPath + "/apache-tomcat-7.0.78";

        //启动该项目的tomcat服务
        LinuxCommand rec1 = new LinuxCommand(ip, username,password);
        try{
            if(rec1.login()){
                Session session = LinuxCommand.conn.openSession();//打开一个会话
                session.execCommand("sh " + tomcatPath + "/bin/startup.sh");//执行命令
                String result = LinuxCommand.processStdout(session.getStdout(), LinuxCommand.DEFAULTCHART);
                session.close();
                LinuxCommand.conn.close();
            }
        }catch (Exception e){
            getMessageShow().showMessage(e.getMessage(), MainUI.ERROR_MESSAGE);
        }

        //启动远程tomcat服务日志查询
        final CountDownLatch latch = new CountDownLatch(1);
        LogReader logReader = new LogReader(getProjectName(), tomcatPath, latch, getMessageShow());
        new Thread(logReader).start();

        try{
            getMessageShow().showMessage("等待项目 " + getProjectName() + " Tomcat服务启动.....", 1);
            latch.await();
            if (logReader.isError()){
                getMessageShow().showMessage("项目 " + getProjectName() + " Tomcat服务启动失败",
                        1);
                setState(Command.CMD_STATE_FAIL);
            }else {
                getMessageShow().showMessage("项目 " + getProjectName() + " Tomcat服务启动成功",
                        1);
                setState(Command.CMD_STATE_SUCCESS);
            }
        }catch (InterruptedException e){
            getMessageShow().showMessage(e.getMessage(), MainUI.ERROR_MESSAGE);
        }
    }

    /**
     * 首先获取远程linux系统占用该端口号的进程号，进程号不为空则杀死
     * @author zhangym v1.0  2018/6/7
     */
    private void killProgress(){
        final String projectPort = MyProperties.getMyPropertiesInstance().
                getProperty("project." + getProjectName() + ".port");
        LinuxCommand rec = new LinuxCommand(ip, username, password);
        try{
            if(rec.login()){
                //查看占用项目端口的进程号
                Session session = LinuxCommand.conn.openSession();//打开一个会话
                session.execCommand("netstat -anp|grep " + projectPort + " |awk '{print $7}'");//执行命令
                String result = LinuxCommand.processStdout(session.getStdout(), LinuxCommand.DEFAULTCHART); //解析脚本执行返回的结果集
                String pid = result.split("\\s+")[0].split("\\/")[0];  //得到进程号
                session.close();
                System.out.println("进程号：" + pid);
                //获取到的进程号不为空则杀死
                if(pid != null){
                    Session session1 = LinuxCommand.conn.openSession();//打开一个会话
                    session1.execCommand("kill -9 " + pid);//执行命令
                    String result1 = LinuxCommand.processStdout(session1.getStdout(), LinuxCommand.DEFAULTCHART);
                    System.out.println(result1);
                    session1.close();
                }

                LinuxCommand.conn.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

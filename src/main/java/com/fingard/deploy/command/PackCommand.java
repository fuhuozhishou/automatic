package com.fingard.deploy.command;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.utils.MyProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author zhangym
 * @version 1.0  2018/4/28
 */
public class PackCommand extends Command{
    private static final String CMD_PACK_SUFFIX = MyProperties.
            getMyPropertiesInstance().getProperty("cmd.package.command.suffix");

    private String projectPath;

    public PackCommand(String projectName, MessageShow messageShow) {
        super(projectName, messageShow);
    }
    public PackCommand(String projectName, MessageShow messageShow, String projectPath){
        super(projectName, messageShow);
        this.projectPath = projectPath;
    }
    @Override
    public int execute(){
        setState(Command.CMD_STATE_EXECUTING);
        pack();
        return getState();
    }
    /**
     * 使用maven打包命令打包指定项目（打包指定项目虽然比打包整个项目省时，但更容易出错）
     * @author zhangym v1.0  2018/6/7
     */
    private void pack(){
        getMessageShow().showProgress("打包信息");
        String projectName = getProjectName();
        String cmdPackageCommand = "cmd /c " + projectPath.charAt(0) + ": && cd " +
                projectPath.replaceAll("\\\\", "\\\\\\\\") + " " + CMD_PACK_SUFFIX;
        try {
            //执行打包命令
            getMessageShow().showMessage("正在打包项目" + projectName,
                    1);
            execCommand(cmdPackageCommand);
            //execCommand("cmd /c e: && cd e:\\deploy\\rhf && mvn clean install -Dmaven.test.skip=true -X -DskipTests package");
        } catch (Exception e) {
            setState(Command.CMD_STATE_FAIL);
            getMessageShow().showMessage(e.getMessage(), -1);
            return;
        }
        if(getState() == Command.CMD_STATE_SUCCESS){
            getMessageShow().showMessage("项目 " + projectName + " 打包完毕。",
                    1);
        }
    }
    /**
     * 执行打包命令，并从执行进程的输出流中读出打包信息，只显示错误信息到界面上
     * @param command 打包命令
     * @author zhangym v1.0  2018/6/7
     */
    private void execCommand(final String command) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(command);//在单独的进程中执行指定的字符串命令。
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            boolean isError = false;
            //每次读取一行，只显示错误信息
            while ((line = br.readLine()) != null) {
                if(isError)
                {
                    getMessageShow().showMessage(line , -1);
                }else {
                    if(line.startsWith("[ERROR]")){
                        isError = true;
                        getMessageShow().showMessage(line , -1);
                    }
                }
                System.out.println(line);//每次读取一行
            }
            if(isError)
                setState(Command.CMD_STATE_FAIL);
            else
                setState(Command.CMD_STATE_SUCCESS);
        } catch (Exception e) {
            setState(Command.CMD_STATE_FAIL);
            getMessageShow().showMessage(e.getMessage() , -1);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    getMessageShow().showMessage(e.getMessage(), -1);
                }
            }
        }
    }

}

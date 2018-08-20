package com.fingard.deploy.command;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.svn.SvnUtils;
import org.tmatesoft.svn.core.SVNException;

/**
 * @author zhangym
 * @version 1.0  2018/5/2
 */
public class SvnUpdateCommand extends Command{
    private long reversion = 0;
    private String projectPath;

    public SvnUpdateCommand(MessageShow messageShow, long reversion, String projectPath){
        super(messageShow);
        this.reversion = reversion;
        this.projectPath = projectPath.replaceAll("\\\\", "\\\\\\\\");
    }
    /**
     * 根据版本号更新，版本号为0则更新至最新
     * @author zhangym v1.0  2018/6/7
     */
    public int execute(){
        setState(Command.CMD_STATE_EXECUTING);
        getMessageShow().showMessage("正在更新....", 1);
        try {
            if(reversion == 0){
                new SvnUtils(getMessageShow()).update(projectPath);
            }else{
                new SvnUtils(getMessageShow()).update(reversion, projectPath);
            }
            setState(Command.CMD_STATE_SUCCESS);
        }catch (SVNException e){
            getMessageShow().showMessage(e.getMessage() + "\r\n", -1);
            setState(Command.CMD_STATE_FAIL);
        }
        return getState();
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
}

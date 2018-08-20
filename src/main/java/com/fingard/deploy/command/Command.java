package com.fingard.deploy.command;

import com.fingard.deploy.component.MessageShow;

/**
 * @author zhangym
 * @version 1.0  2018/4/27
 */
public abstract class Command implements ICommand{
    public static final int CMD_STATE_EXECUTABLE = 1;
    public static final int CMD_STATE_EXECUTING = 2;
    public static final int CMD_STATE_SUCCESS = 3;
    public static final int CMD_STATE_FAIL = -1;

    private String projectName = null;
    private int state = 0;
    private MessageShow messageShow;

    public Command(String projectName, MessageShow messageShow){
        this.projectName = projectName;
        this.messageShow = messageShow;
        this.state = CMD_STATE_EXECUTABLE;
    }
    public Command(MessageShow messageShow){
        this.messageShow = messageShow;
        this.state = CMD_STATE_EXECUTABLE;
    }
    public MessageShow getMessageShow() {
        return messageShow;
    }

    public void setMessageShow(MessageShow messageShow) {
        this.messageShow = messageShow;
    }

    public String getProjectName(){
        return projectName;
    }
    public void setProjectName(String projectName){
        this.projectName = projectName;
    }
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int execute(){
        return 0;
    }
}

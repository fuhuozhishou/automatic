package com.fingard.deploy.commandQueue;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.command.Command;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangym
 * @version 1.0  2018/5/7
 */
public class CommandQueue implements Runnable{
    private final List<Command> commandQueue;
    private MessageShow messageShow;
    private String commandType;
    private boolean hasFailed = false;
    public CommandQueue(MessageShow messageShow, String commandType){
        commandQueue = new LinkedList<>();
        this.messageShow = messageShow;
        this.commandType = commandType;
    }

    public boolean isFinish(){
        return commandQueue.isEmpty();
    }
    /**
     * 添加新命令，如果新命令所操作的项目名已存在，则不再添加;
     * 若队列执行失败过，往头部添加新命令，否则往尾部添加。
     * @param newCommand 新命令
     * @author zhangym v1.0  2018/6/7
     */
    public void addCommand(Command newCommand){
        synchronized (commandQueue){
            for(Command Command : commandQueue){
                if (Command.getProjectName().equals(newCommand.getProjectName()))
                    return;
            }
            if(hasFailed){
                commandQueue.add(0, newCommand);
                hasFailed = false;
            }else
                commandQueue.add(newCommand);

            System.out.println("增加一条" + commandType + "命令");
        }

    }
    public void run(){
        executeQueue();
    }

    /**
     * 除部署命令队列外，队列执行命令出错会给予提示，并继续执行直到队列为空，但不会唤醒下一级队列
     * 部署队列一旦出错将停止，不再继续执行。
     * @author zhangym v1.0  2018/6/7
     */
    private void executeQueue(){
        while(!commandQueue.isEmpty()){
            Command command = commandQueue.get(0);
            int result = command.execute();
            if(result != Command.CMD_STATE_SUCCESS){
                hasFailed = true;
                messageShow.showMessage("项目 " + command.getProjectName() + " 的" +
                        commandType + "命令执行失败!", 1);
                if("部署".equals(commandType)){
                    commandQueue.remove(0);
                    return;
                }
            }
            commandQueue.remove(0);
        }
        String flag = (hasFailed) ? "失败" : "完成";
        messageShow.showProgress(commandType + flag);
    }

}

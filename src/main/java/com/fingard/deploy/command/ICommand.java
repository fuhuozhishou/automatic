package com.fingard.deploy.command;

/**
 * Created by F on 2018/4/28.
 */
public interface ICommand {
    int getState();
    void setState(int state);
    int execute();
}

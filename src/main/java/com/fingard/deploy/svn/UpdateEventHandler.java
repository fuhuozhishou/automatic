package com.fingard.deploy.svn;

import com.fingard.deploy.component.MessageShow;
import org.tmatesoft.svn.core.SVNCancelException;  
import org.tmatesoft.svn.core.SVNNodeKind;  
import org.tmatesoft.svn.core.wc.ISVNEventHandler;  
import org.tmatesoft.svn.core.wc.SVNEvent;  
import org.tmatesoft.svn.core.wc.SVNEventAction; 
public class UpdateEventHandler implements ISVNEventHandler{
    private MessageShow messageShow;
    public UpdateEventHandler(MessageShow messageShow){
        this.messageShow = messageShow;
    }
	public void handleEvent(SVNEvent event, double progress) {
        SVNEventAction action = event.getAction();  
        SVNNodeKind nodeKind = event.getNodeKind();
        if (SVNNodeKind.DIR.equals(nodeKind)) {  
            // folder
            String str = "更新目录：" + event.getFile().getName();
            System.out.println(str);
  
        } else {  
            // treat as file for all other type  
            if (action == SVNEventAction.UPDATE_DELETE) {
                String str = "删除文件："  + event.getFile().getName();
                messageShow.showMessage(str, 1);

            } else if (action == SVNEventAction.UPDATE_UPDATE) {
                String str = "更新文件：" + event.getFile().getName();
                messageShow.showMessage(str, 1);

            }else if (action == SVNEventAction.UPDATE_ADD ){
                String str = "新增文件：" + event.getFile().getName();
                messageShow.showMessage(str, 1);
                //System.out.println(str);
            }
        }  
    }  
  
    public void checkCancelled() throws SVNCancelException {  
    }
}

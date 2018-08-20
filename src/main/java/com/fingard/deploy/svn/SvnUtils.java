package com.fingard.deploy.svn;

import java.io.File;

import com.fingard.deploy.component.MessageShow;
import com.fingard.deploy.svn.UpdateEventHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import com.fingard.deploy.utils.MyProperties;


public class SvnUtils {
	private static final String svnAddress = MyProperties.getMyPropertiesInstance().getProperty("svn.address.url");
	//private static final String svnLocalAddress = MyProperties.getMyPropertiesInstance().getProperty("svn.local.address");
	static {  
        DAVRepositoryFactory.setup();  
    }  

    private MessageShow messageShow;
    private SVNClientManager manager;  
    private SVNURL repositoryBaseUrl;  
  
    @SuppressWarnings("deprecation")
	public SvnUtils(MessageShow messageShow) {
        DefaultSVNOptions options = new DefaultSVNOptions();  
        manager = SVNClientManager.newInstance(options);  
        // manager = SVNClientManager.newInstance(options,  
        // "username","passwrod"); //如果需要用户名密码  
        try {  
            repositoryBaseUrl = SVNURL.parseURIDecoded(svnAddress); // 传入svn地址  
        } catch (SVNException e) {
            e.printStackTrace();
        }
        this.messageShow = messageShow;
    }
    public SvnUtils(){
        DefaultSVNOptions options = new DefaultSVNOptions();
        manager = SVNClientManager.newInstance(options);
        // manager = SVNClientManager.newInstance(options,
        // "username","passwrod"); //如果需要用户名密码
        try {
            repositoryBaseUrl = SVNURL.parseURIDecoded("http://124.160.27.118:81/ATS_Code/FinGard/Java/Platform/ZL_manage/trunk/src"); // 传入svn地址
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }
    public void getInfo() throws SVNException{
        Long myReversion = 78879L;
        String author = manager.getLookClient().doGetAuthor(new File(""),
                SVNRevision.create(myReversion));
        System.out.println(author);
    }
    public void update(String svnLocalAddress) throws SVNException {
    	// svn co  
         UpdateEventHandler svnEventHandler = new UpdateEventHandler(messageShow); // svn  co时对每个文件的处理
         SVNUpdateClient client = manager.getUpdateClient();
    	 File to = new File(svnLocalAddress); // copy出来的文件存放目录

         if (to.exists()&& to.isDirectory()){
             /*File[] files = to.listFiles();
             if(files !=null&&files.length > 0){ //此方法判断OK,需要使用数组的长度来判断。
             	deleteDir(to);
             } */
        	// svn 更新操作
             client.setIgnoreExternals(false);
             client.setEventHandler(svnEventHandler);
             long version =client.doUpdate(to, SVNRevision.HEAD, SVNDepth.INFINITY,true, false);
             messageShow.showMessage("更新至版本号：" + version, 1);
             System.out.println("更新至版本号：" + version);
         }else{
        	  //svn做检出操作
        	  client.setIgnoreExternals(false);
              client.setEventHandler(svnEventHandler);

              long version =client.doCheckout(repositoryBaseUrl, to, SVNRevision.HEAD,
                      SVNRevision.HEAD, SVNDepth.INFINITY, false);
              messageShow.showMessage("更新至版本号：" + version, 1);
         }
         
         /*SVNLogClient logClient = manager.getLogClient();  
		  
         // svn list  
         DirEntryHandler handler = new DirEntryHandler(); // 在svn  
                                                             // co时对每个文件目录的处理，实现ISVNDirEntryHandler接口  
         logClient.doList(repositoryBaseUrl, SVNRevision.HEAD, SVNRevision.HEAD,  
                 false, true, handler); // 列出当前svn地址的目录，对每个文件进行处理  
*/   
    }

    public void update(Long myReversion, String svnLocalAddress) throws SVNException{
        UpdateEventHandler svnEventHandler = new UpdateEventHandler(messageShow); // svn  co时对每个文件的处理
        SVNUpdateClient client = manager.getUpdateClient();
        File to = new File(svnLocalAddress); // copy出来的文件存放目录

        if (to.exists()&& to.isDirectory()){
             /*File[] files = to.listFiles();
             if(files !=null&&files.length > 0){ //此方法判断OK,需要使用数组的长度来判断。
             	deleteDir(to);
             } */
            // svn 更新操作
            client.setIgnoreExternals(false);
            client.setEventHandler(svnEventHandler);
            long version =client.doUpdate(to, SVNRevision.create(myReversion), SVNDepth.INFINITY,true, false);
            messageShow.showProgress("更新至版本号：" + version);
            System.out.println(version);
        }else{
            //svn做检出操作
            client.setIgnoreExternals(false);
            client.setEventHandler(svnEventHandler);

            long version =client.doCheckout(repositoryBaseUrl, to, SVNRevision.HEAD,
                    SVNRevision.HEAD, SVNDepth.INFINITY, false);
            messageShow.showProgress("更新至版本号：" + version);
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
        	/**递归删除目录中的子目录下*/
            String[] children = dir.list();
            if (children != null){
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        // 目录此时为空，可以删除
        return dir.delete();
       // return false;
    }

    public static void main(String[] args) {

        try {
            new SvnUtils().getInfo();
        }catch (SVNException e){
            e.printStackTrace();
        }

    }
}

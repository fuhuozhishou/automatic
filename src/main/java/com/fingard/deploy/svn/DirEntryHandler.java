package com.fingard.deploy.svn;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;

public class DirEntryHandler implements ISVNDirEntryHandler{

	public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
		System.out.println(dirEntry.getRevision()+":"+dirEntry.getRelativePath() + "/" + dirEntry.getName());
	}
	
}

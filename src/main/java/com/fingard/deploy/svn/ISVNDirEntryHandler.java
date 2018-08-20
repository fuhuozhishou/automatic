package com.fingard.deploy.svn;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;

public interface ISVNDirEntryHandler {
	public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException;
}

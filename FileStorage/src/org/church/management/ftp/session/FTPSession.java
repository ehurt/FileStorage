package org.church.management.ftp.session;

import it.sauronsoftware.ftp4j.FTPFile;

import java.io.File;
import java.util.List;

public interface FTPSession 
{
	public void close();
	
	public boolean isClosed();
	
	public String createDirectory(String directory) throws Exception;
	
	public void deleteDirectory(String directory) throws Exception;
	
	public String renameDirectory(String oldDirectory, String name) throws Exception;
	
	public String upload(String directory, File file) throws Exception;
	
	public String copy(String directory, String renameFile, File file) throws Exception;
	
	public void delete(String filePath) throws Exception;
	
	public void retrieve(String filePath, File localeFile) throws Exception;
	
	public String update(String filePath, File file) throws Exception;
	
	public String renameFile(String filepath, String newFileName) throws Exception ;

	public List<FTPFile> getAllFilesForDirectory(String directory) throws Exception;
	
}

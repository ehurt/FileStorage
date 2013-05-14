package org.church.management.file.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @author Trae
 * 
 * This interface is used for storing files on a server, ftp, or drive.
 *
 */
public interface FileStorage 
{
	
	public String createDirectory(String directory) throws Exception;
	public void deleteDirectory(String directory) throws Exception;
	public String renameDirectory(String oldDirectly, String newDirectory) throws Exception;
	
	public String upload(String folder, File file) throws Exception;
	public String copy(String folder, String renameFile, File file) throws Exception;
	public void deleteFile(String filePath) throws Exception;
	public File retrieve(String filePath) throws Exception;
	public String update(String filePath, File file) throws Exception;
	public String renameFile(String filepath, String newFileName) throws Exception;
	
	public List<String> getAvailableFileExtensions() throws Exception;
	public long getMemoryUsage() throws Exception;
	public long getMemoryLimitation() throws Exception;
	
	public List<File> getAllFiles() throws Exception;
	public List<File> getAllFilesForDirectory(String directory) throws Exception;
	public boolean isCloudStorage();
	public File zipUpAllFiles(String archiveName) throws Exception;
	public File zipUpDirectory(String fullPath, String archiveName) throws Exception;
}
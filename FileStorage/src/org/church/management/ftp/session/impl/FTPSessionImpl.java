package org.church.management.ftp.session.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.church.management.ftp.connection.pool.FTPConnectionPoolManagement;
import org.church.management.ftp.session.FTPSession;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * 
 * @author Trae
 *
 * This class gives you a connection to the ftp server.
 * Once the session is close, you cannot use it again.
 *
 */
public class FTPSessionImpl implements FTPSession
{
	private static final Logger logger = Logger.getLogger(FTPSessionImpl.class);
	private FTPClient client;
	private boolean closed = false;
	private FTPConnectionPoolManagement pool;
	private String currentDirectory = "";
	
	public FTPSessionImpl(String currentDirectory, FTPClient client, FTPConnectionPoolManagement pool)
	{
		this.client = client;
		this.pool = pool;
		this.currentDirectory = currentDirectory;
	}

	public void close() 
	{
		this.closed = true;
		pool.returnConnection(client);
	}
	
	public boolean isClosed()
	{
		return closed;
	}

	public String createDirectory(String directory) throws Exception 
	{
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			directory = currentDirectory+"/"+directory;
			client.createDirectory(directory);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.createDirectory()- Could not create directory: "+directory+".", e);
			throw e;
		}
		
		return directory;
	}

	public void deleteDirectory(String directory) throws Exception 
	{
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			directory = currentDirectory+"/"+directory;
			client.deleteDirectory(directory);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.deleteDirectory()- Could not delete directory: "+directory+".", e);
			throw e;
		}
	}

	public String renameDirectory(String oldDirectory, String name) throws Exception 
	{
		String path = "";
		
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			int index = oldDirectory.lastIndexOf("/");
			String directory = oldDirectory.substring(0, index);
			path = directory+"/"+name;
			client.rename(oldDirectory, path);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.renameDirectory()- Could not rename directory: "+oldDirectory+" to "+name+".", e);
			throw e;
		}
		
		return path;
	}

	public String upload(String directory, File file) throws Exception 
	{
		String fullPath = "";
		
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			fullPath = directory+"/"+file.getName();
			client.changeDirectory(directory);
			client.upload(file);
			client.changeDirectory(currentDirectory);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.upload()- Could not upload file to directory: "+directory+".", e);
			throw e;
		}
		
		return fullPath;
	}

	public String copy(String directory, String renameFile, File file) throws Exception 
	{
		String newFilePath = "", oldFilePath = "";
		
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			oldFilePath = directory+"/"+file.getName();
			newFilePath = directory+"/"+renameFile;
			client.changeDirectory(directory);
			client.upload(file);
			client.rename(oldFilePath, renameFile);
			client.changeDirectory(currentDirectory);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.copy()- Could not copy file to directory: "+directory+".", e);
			throw e;
		}
		
		return newFilePath;
	}

	public void delete(String filePath) throws Exception 
	{
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			client.deleteFile(filePath);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.delete()- Could not delete file: "+filePath+".", e);
			throw e;
		}
	}

	public void retrieve(String remoteFilePath, File localFile) throws Exception 
	{
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			client.download(remoteFilePath, localFile);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.retrieve()- Could not download file: "+remoteFilePath+".", e);
			throw e;
		}
	}

	public String update(String filePath, File file) throws Exception 
	{
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			File originalFile = new File(filePath);
			String filename = originalFile.getName();
			String parent = originalFile.getParent();
			String newFilePath = parent+"/"+file.getName();
			client.changeDirectory(parent);
			client.upload(file);
			client.deleteFile(filePath);
			client.rename(newFilePath, filePath);
			client.changeDirectory(currentDirectory);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.update()- Could not update a file.",e);
			throw e;
		}
		
		return filePath;
	}

	public String renameFile(String filepath, String newFileName) throws Exception 
	{
		String path = "";
		
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			int index = filepath.lastIndexOf("/");
			String directory = filepath.substring(0, index);
			path = directory+"/"+newFileName;
			client.rename(filepath, path);
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.renameFile()- Could not rename file: "+filepath+" to "+newFileName+".", e);
			throw e;
		}
		
		return path;
	}

	public List<FTPFile> getAllFilesForDirectory(String directory) throws Exception 
	{
		List<FTPFile> files = null;
		
		if(closed)
		{
			throw new Exception("Session is currently closed.");
		}
		
		try
		{
			FTPFile temp[] = client.list(directory);
			if(temp.length > 0)
			{
				files = new ArrayList<FTPFile>();
				for(FTPFile file : temp)
				{
					files.add(file);
				}
			}
			
			else
			{
				files = new ArrayList<FTPFile>();
			}
		}
		catch(Exception e)
		{
			logger.error("FTPSessionImpl.getAllFilesForDirectory()- Could not get all the files from the directory: "+directory+".", e);
			throw e;
		}
		
		return files;
	}
}

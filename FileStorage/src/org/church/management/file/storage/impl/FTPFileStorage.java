package org.church.management.file.storage.impl;

import it.sauronsoftware.ftp4j.FTPCodes;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.church.management.annotations.Supported;
import org.church.management.annotations.Unsupported;
import org.church.management.file.storage.CloudFileStorage;
import org.church.management.ftp.connection.pool.FTPConnectionPoolManagement;
import org.church.management.ftp.session.FTPSession;
import org.church.management.temporary.file.factory.TemporaryFileFactory;
/***
 * 
 * @author Trae
 * 
 * This class is for storing files on a ftp server.
 *
 * need properties
 * 		host: the ip address to the ftp
 * 		port: the port for the ftp server
 * 		username: the username for ftp login
 * 		password: the password for ftp login
 * 		passive: this for data upload, the usually setting is true
 *		noopTimeout: this variable will ping the server to keep the connection alive.
 *		storageFolder: this directory is where all the directories and files are stored.
 *		
 *		minPoolSize: the minimum number of connections in the pool.
 *		maxPoolSize: the max number of connections in the pool.
 *
 *		TODO will need new properties for passive and active
 */
public class FTPFileStorage implements CloudFileStorage
{
	private static final Logger logger = Logger.getLogger(FTPFileStorage.class);
	
	private static String tempFolderPath;
	private static String fileSeparator;
	private String storageFolder;
	
	private FTPConnectionPoolManagement pool;
	
	static
	{
		fileSeparator = System.getProperty("file.separator");
	}
	
	public FTPFileStorage(Properties properties) throws Exception
	{
		Integer port = null;
		Integer noopTimeout = Integer.parseInt(properties.getProperty("noopTimeout"));
		String username = properties.getProperty("username");
		Boolean passive = Boolean.parseBoolean(properties.getProperty("passive"));
		String password = properties.getProperty("password");
		String host = properties.getProperty("host");
		int minimumPoolSize = Integer.parseInt(properties.getProperty("minimumPoolSize"));
		int maximumPoolSize = Integer.parseInt(properties.getProperty("maximumPoolSize"));
		
		String tempFolderFullPath = properties.getProperty("tempFolderPath");
		
		String hostPort = properties.getProperty("port");
		
		if(hostPort == null)
		{
			port = null;
		}
		
		else 
		{
			port = Integer.parseInt(properties.getProperty("port"));
		}
		
		storageFolder = properties.getProperty("storagePath");
		
		if(tempFolderFullPath != null)
		{
			tempFolderPath = tempFolderFullPath;
		}
		
		else
		{
		
			URL url = Thread.currentThread().getContextClassLoader().getResource("hibernate.cfg.xml");
			String path = url.getFile().replace("%20", " ");
			File file = new File(path);
			File parent = file.getParentFile();
			parent = parent.getParentFile();
			File tempFolder = new File(parent.getPath()+fileSeparator+"temp");
			tempFolderPath = tempFolder.getPath();
		
		}
		
		if(port != null)
		{
			pool = new FTPConnectionPoolManagement(host, port, username, password, storageFolder, passive, minimumPoolSize, maximumPoolSize, noopTimeout);
		}
		
		else
		{
			pool = new FTPConnectionPoolManagement(host, username, password, storageFolder, passive, minimumPoolSize, maximumPoolSize, noopTimeout);
		}
	}
	
	@Supported
	public String createDirectory(String directory) throws Exception
	{
		String path = "";
		FTPSession session = null;
				
		try 
		{
			session = pool.openSession();
			path = session.createDirectory(directory);
		} 
		
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.createDirectory()- Illegal State on the server.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.createDirectory()- IO Error could not create the directory.", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.createDirectory()- Could not create the directory: "+directory+".", e);
			throw e;
		} 
		
		catch (FTPException e) 
		{
			logger.error("FTPFileStorage.createDirectory()- Could not create the directory: "+directory+".", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException(directory+" could not be created.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Supported
	public void deleteDirectory(String directory) throws Exception
	{
		FTPSession session = null;
		
		try 
		{
			session = pool.openSession();
			session.deleteDirectory(directory);
		} 
		
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.deleteDirectory()- Illegal State happened while deleting directory: "+directory+".", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.deleteDirectory()- IO Error deleting the directory: "+directory+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.deleteDirectory()- Could not delete the directory: "+directory+".", e);
			throw e;
		} 
		
		catch (FTPException e) 
		{
			logger.error("FTPFileStorage.deleteDirectory()- Could not delete the directory: "+directory+".", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException(directory+" could not be deleted.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
	}

	@Supported
	public String renameDirectory(String oldDirectory, String newDirectory) throws Exception 
	{
		String path = "";
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			path = session.renameDirectory(oldDirectory, newDirectory);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.renameDirectory()- Illegal State happened while rename directory: "+oldDirectory+".", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.renameDirectory()- IO Error rename the directory: "+oldDirectory+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.renameDirectory()- Could not rename the directory: "+oldDirectory+".", e);
			throw e;
		} 
		
		catch (FTPException e) 
		{
			logger.error("FTPFileStorage.renameDirectory()- Could not rename the directory: "+oldDirectory+".", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException(oldDirectory+" could not be renamed.");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(oldDirectory+" could not be renamed.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Supported
	public String upload(String directory, File file) throws Exception 
	{
		String path = "";
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			path = session.upload(directory, file);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.upload()- Illegal State happened while uploading file.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.upload()- IO Error upload file to directory: "+directory+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.upload()- Could not upload the file.", e);
			throw e;
		} 
		
		catch (FTPException e) 
		{
			logger.error("FTPFileStorage.upload()- Could not upload the file.", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException("Could not upload file to "+directory+".");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(directory+" could not upload file to.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Supported
	public String copy(String directory, String renameFile, File file) throws Exception 
	{
		String path = "";
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			path = session.copy(directory, renameFile, file);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.copy()- Illegal State happened while copying file.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.copy()- IO Error copying file to directory: "+directory+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.copy()- Could not copy the file.", e);
			throw e;
		} 
		
		catch (FTPException e) 
		{
			logger.error("FTPFileStorage.copy()- Could not copy the file.", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException("Could not copy file to "+directory+".");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(directory+" could not upload file to.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Supported
	public void deleteFile(String filePath) throws Exception 
	{
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			session.delete(filePath);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.deleteFile()- Illegal State happened while deleting the file.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.deleteFile()- IO Error delete file: "+filePath+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.deleteFile()- Could not delete the file.", e);
			throw e;
		}
		
		catch(FTPException e)
		{
			logger.error("FTPFileStorage.deleteFile()- Could not delete the file.", e);
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
	}

	@Supported
	public File retrieve(String filePath) throws Exception 
	{
		File temporaryFile = null;
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			File file = new File(filePath);
			String filename = file.getName();
			temporaryFile = TemporaryFileFactory.createFile(filename);
			session.retrieve(filePath, temporaryFile);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.retrieve()- Could not retrieve file: "+filePath+".", e);
			throw e;
		}
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.retrieve()- Could not retrieve file: "+filePath+", because of io exception.", e);
			throw e;
		}
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.retrieve()- Could not retrieve file: "+filePath+".", e);
			throw e;
		}
		
		catch(FTPException e)
		{
			logger.error("FTPFileStorage.retrieve()- Could not retrieve file: "+filePath+", because of state: "+e.getCode()+".", e);
			
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException("Could not be retrieved "+filePath+".");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(filePath+" could not retrieve the file.");
			}
			
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
			
		return temporaryFile;
	}

	@Supported
	public String update(String filePath, File file) throws Exception 
	{	
		String path = "";
		FTPSession session = null;
		
		try
		{
			session = pool.openSession();
			path = session.update(filePath, file);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.update()- Illegal State happened while uploading file.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.update()- IO Error update file: "+filePath+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.update()- Could not update the file.", e);
			throw e;
		}
		
		catch(FTPException e)
		{
			logger.error("FTPFileStorage.update()- Could not update the file.", e);
		
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException("Could not copy file to "+filePath+".");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(filePath+" could not update the file.");
			}
				
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Supported
	public String renameFile(String filepath, String newFileName) throws Exception 
	{
		FTPSession session = null;
		String path = "";
		
		try
		{
			session = pool.openSession();
			path = session.renameFile(filepath, newFileName);
		}
		catch (IllegalStateException e) 
		{
			logger.error("FTPFileStorage.renameFile()- Illegal State happened while renaming file.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPFileStorage.renameFile()- IO Error renaming file: "+filepath+".", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPFileStorage.renameFile()- Could not renaming the file.", e);
			throw e;
		}
		
		catch(FTPException e)
		{
			logger.error("FTPFileStorage.renameFile()- Could not renaming the file.", e);
		
			if(e.getCode() == FTPCodes.FILE_NOT_FOUND)
			{
				throw new FileNotFoundException("Could not rename the file: "+filepath+".");
			}
			
			else if(e.getCode() == FTPCodes.FILE_NAME_NOT_ALLOWED)
			{
				throw new FileNotFoundException(filepath+" could not rename the file.");
			}
				
			throw e;
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return path;
	}

	@Unsupported
	public List<String> getAvailableFileExtensions() throws Exception 
	{
		return new ArrayList<String>();
	}

	@Unsupported
	public long getMemoryUsage() throws Exception {
		return 0;
	}

	@Unsupported
	public long getMemoryLimitation() throws Exception {
		return 0;
	}

	@Supported
	public List<File> getAllFiles() throws Exception {
		// TODO Auto-generated method stub
		
		//TODO get a directory from the temp file storage.
		
		return null;
	}

	@Supported
	public List<File> getAllFilesForDirectory(String directory) throws Exception 
	{
		List<File> files = null;
		FTPSession session = null;
		
		try
		{
			//TODO get a directory from the temp file storage and some how keep the original names.
			session = pool.openSession();
			List<FTPFile> ftpFiles = session.getAllFilesForDirectory(directory);
		
			if(ftpFiles.size() > 0)
			{
				File directoryFile = TemporaryFileFactory.createTempDirectory();
				
				for(FTPFile file: ftpFiles)
				{
					if(file.getType() == FTPFile.TYPE_FILE)
					{
						
					}
					
					else if(file.getType() == FTPFile.TYPE_DIRECTORY)
					{
						//create new directory within the 
					}
				}
			}
			
			else
			{
				files = new ArrayList<File>();
			}
		}
		catch (IllegalStateException e)
		{
			
		}
		
		catch (IOException e)
		{
			
		}
		
		catch (FTPIllegalReplyException e) 
		{
			
		}
		
		catch(FTPException e)
		{
			
		}
		
		finally
		{
			if(session != null)
			{
				session.close();
			}
		}
		
		return files;
	}

	public boolean isCloudStorage() 
	{
		return true;
	}

	@Supported
	public File zipUpAllFiles(String archiveName) throws IOException
	{
		//TODO get a directory from the temp file storage.
		
		return null;
	}

	@Supported
	public File zipUpDirectory(String fullPath, String archiveName) throws IOException {
		
		// TODO Auto-generated method stub
		
		//TODO get a directory from the temp file storage.
		
		return null;
	}
	
	public FTPConnectionPoolManagement getPool()
	{
		return this.pool;
	}

	public boolean isConnected() 
	{
		return pool.isConnected();
	}

	public void disconnect() 
	{
		pool.disconnect();	
	}

	public void reconnect() throws Exception 
	{
		pool.reconnect();
	}
}

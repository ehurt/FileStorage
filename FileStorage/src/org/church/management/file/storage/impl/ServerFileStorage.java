package org.church.management.file.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.church.management.annotations.Supported;
import org.church.management.file.storage.FileStorage;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;

/**
 * @author Trae
 *
 * This class is for storing the files on the server 
 * in the storage folder.
 * 
 *  need properties:
 *  	memoryLimitation: the limitation on the memory available
 */
public class ServerFileStorage implements FileStorage
{
	private static Logger logger = Logger.getLogger(ServerFileStorage.class);
	
	private Properties properties = null;
	private long memoryLimitation = 0;
	private String storageFolderPath = null;
	private static String fileSeparator;
	private static String tempFolderPath;
	
	static
	{
		fileSeparator = System.getProperty("file.separator");
	}
	
	public ServerFileStorage(Properties properties)
	{
		this.properties = properties;
		String tempPath = this.properties.getProperty("storagePath");
		String tempFolderFullPath = this.properties.getProperty("tempFolderPath");
		
		if(tempPath != null)
		{
			storageFolderPath = tempPath;
		}
		
		else
		{
			URL url = Thread.currentThread().getContextClassLoader().getResource("hibernate.cfg.xml");
			String path = url.getFile().replace("%20", " ");
			File file = new File(path);
			File parent = file.getParentFile();
			parent = parent.getParentFile();
			File storageFolder = new File(parent.getPath()+fileSeparator+"storage");
			storageFolderPath = storageFolder.getPath();
		}
				
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
		
		try
		{
			memoryLimitation = Long.parseLong(this.properties.getProperty("storageMemoryLimitation"));
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.<init> - An error has occurred while parsing memory limitation property.", e);
		}
	}

	@Supported
	public String createDirectory(String directoryName) throws Exception 
	{
		String directoryPath = storageFolderPath+fileSeparator+directoryName;
		File directory = new File(directoryPath);
	
		try
		{
			if(directory.exists())
			{
				return directory.getPath();
			}
			
			boolean created = directory.mkdir();
			
			if(created == false)
			{
				logger.error("Directory failed to created with directory name: "+directoryName+".");
				throw new Exception("Directory failed to created with directory name: "+directoryName+".");
			}
		}
		catch(Exception e)
		{
			logger.error("Directory failed to created with directory name: "+directoryName+".", e);
			throw e;
		}
		
		return directory.getPath();
	}
	

	@Supported
	public void deleteFile(String filePath) throws Exception 
	{
		boolean deleted = false;
		File file = new File(filePath);
		
		try
		{
			if(file.exists())
			{
				deleted = file.delete();
			}
			
			else
			{
				return;
			}
			
			if(deleted == false)
			{
				throw new Exception("Could not delete file.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.delete() - An error has occurred while deleting the file: "+filePath+".", e);
			throw e;
		}
	}

	@Supported
	public void deleteDirectory(String directoryFilePath) throws Exception 
	{
		try
		{
			File directory = new File(directoryFilePath);
			
			if(directory.exists())
			{
				FileUtils.deleteDirectory(directory);
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.deleteDirectory()- An error has occurred while deleting the directory.", e);
			throw e;
		}
	}

	@Supported
	public List<File> getAllFiles() throws Exception 
	{
		ServerStorageFileFilter filter = new ServerStorageFileFilter(true);
		return new ArrayList<File>(FileUtils.listFiles(new File(storageFolderPath), filter, filter));
	}

	@Supported
	public List<File> getAllFilesForDirectory(String directoryFilePath) throws Exception 
	{
		ServerStorageFileFilter filter = new ServerStorageFileFilter(false);
		return new ArrayList<File>(FileUtils.listFiles(new File(directoryFilePath), filter, filter));
	}

	@Supported
	public List<String> getAvailableFileExtensions() throws Exception 
	{
		return new ArrayList<String>();
	}

	@Supported
	public long getMemoryLimitation() throws Exception 
	{
		return memoryLimitation;
	}

	@Supported
	public long getMemoryUsage() throws Exception 
	{
		return FileUtils.sizeOfDirectory(new File(storageFolderPath));
	}

	@Supported
	public String renameDirectory(String oldDirectoryPath, String newDirectoryName) throws Exception 
	{
		File oldDirectory = new File(oldDirectoryPath);
		File newDirectory = new File(storageFolderPath+fileSeparator+newDirectoryName);
		boolean renamed = false;
		
		try
		{
			if(oldDirectory.exists())
			{
				renamed = oldDirectory.renameTo(newDirectory);
			}
			
			else
			{
				throw new FileNotFoundException("Directory "+oldDirectoryPath+" could not be renamed because it does not existed.");
			}
			
			if(renamed == false)
			{
				throw new Exception("Could not renamed directory.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.renameDirectory() - An error has occurred while renaming a directory: "+oldDirectoryPath+" to "+newDirectoryName+".", e);
			throw e;
		}
		
		return newDirectory.getPath();
	}

	@Supported
	public String renameFile(String filePath, String newFileName) throws Exception 
	{
		File renamedFile = null;
		
		try
		{
			File file = new File(filePath);
			
			if(file.exists())
			{
				File parent = file.getParentFile();
				renamedFile = new File(parent.getPath()+fileSeparator+newFileName);
				file.renameTo(renamedFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+filePath+" could not rename, because it does not existed.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.renamedFile() - An error has occurred while renaming the file: "+filePath+" to "+newFileName+".", e);
			throw e;
		}
		
		return renamedFile.getPath();
	}

	@Supported
	public File retrieve(String filePath) throws Exception {
		
		File file = new File(filePath);

		if(file.exists() == false)
		{
			logger.error("ServerStorage.retrieve()- An error has occurred while retrieving the file: "+filePath+".");
			throw new FileNotFoundException("File "+filePath+" does not existed.");
		}

		return file;
	}

	@Supported
	public String upload(String folder, File file) throws Exception 
	{
		String fullPath = storageFolderPath+fileSeparator+folder+fileSeparator+file.getName();
		File copiedFile = new File(fullPath);
		
		try
		{
			if(file.exists())
			{
				FileUtils.copyFile(file, copiedFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+file.getAbsolutePath()+" could not be copied.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.upload() - An error has occurred while uploading file: "+file.getPath()+".", e);
			throw e;
		}
		
		return copiedFile.getPath();
	}

	@Supported
	public String copy(String folder, String renameFile, File file) throws Exception 
	{
		String fullPath = storageFolderPath+fileSeparator+folder+fileSeparator+renameFile;
		File copiedToFile = new File(fullPath);
		
		try
		{
			if(file.exists())
			{
				FileUtils.copyFile(file, copiedToFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+file.getAbsolutePath()+" could not be copied.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.upload()- An error has occurred while copying files.", e);
			throw e;
		}
		
		return fullPath;
	}

	@Supported
	public String update(String filePath, File file) throws Exception {
		
		
		File originalFile = new File(filePath), copiedFile = null;
		
		try
		{
			if(originalFile.exists())
			{
				FileUtils.forceDelete(originalFile);
				File parent = originalFile.getParentFile();
					
			    copiedFile = new File(parent.getPath(), file.getName());
				FileUtils.copyFile(file, copiedFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+filePath+" could not be update, because it does not existed.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.update()- An error has while updating file: "+filePath+".", e);
			throw e;
		}
		
		return copiedFile.getPath();
	}

	public boolean isCloudStorage()
	{
		return false;
	}
	
	private class ServerStorageFileFilter implements IOFileFilter
	{
		private boolean includeDirectories = false;
		
		public ServerStorageFileFilter(boolean includeDirectories)
		{
			this.includeDirectories = includeDirectories;
		}

		public boolean accept(File arg0) 
		{
			return true;
		}

		public boolean accept(File arg0, String arg1) 
		{
			return includeDirectories;
		}
	}

	@Supported
	public File zipUpAllFiles(String archiveName) throws Exception 
	{
		TFile archive = new TFile(tempFolderPath+fileSeparator+archiveName);
		
		try
		{
			TFile direct = new TFile(storageFolderPath);
			direct.cp_rp(archive);
			
			TVFS.umount(archive);
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.zipUpAllFiles()- An error has occurred while zip up files.", e);
			throw e;
		}

		return archive;
	}

	@Supported
	public File zipUpDirectory(String fullPath, String archiveName) throws Exception 
	{
		TFile archive = new TFile(tempFolderPath+File.separatorChar+archiveName);		
		try
		{
			TFile direct = new TFile(fullPath);
			
			if(direct.exists())
			{
				direct.cp_rp(archive);
				TVFS.umount(archive);
			}
			
			else
			{
				throw new FileNotFoundException("Directory "+fullPath+" could not zip up, because it is deleted.");
			}
		}
		catch(Exception e)
		{
			logger.error("ServerStorage.zipUpDirectory()- Could not zip up directory: "+fullPath+".", e);
			throw e;
		}
		
		return archive;
	}
}

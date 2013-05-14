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
 * 
 * @author Trae
 *
 * This class will store the files on a mapped drive or local drive.
 *
 * need properties:
 * 		storagePath: the directory where the files are stored
 * 		memoryLimitation: how much memory is available
 *
 */

public class DriveFileStorage implements FileStorage
{
	private static final Logger logger = Logger.getLogger(DriveFileStorage.class);
	
	private Properties properties;
	private String storagePath;
	private long memoryLimitation = 0;
	private static String fileSeparator = "";
	private static String tempFolderPath;
	
	static
	{
		fileSeparator = System.getProperty("file.separator");
	}
	
	public DriveFileStorage(Properties properties)
	{
		this.properties = properties;
		this.storagePath = properties.getProperty("storagePath");
		String tempFolderFullPath = this.properties.getProperty("tempFolderPath");
		
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
			logger.error("DriveStorage.<init> - An error has occurred while parsing memory limitation property.", e);
		}
	}

	@Supported
	public String createDirectory(String directoryName) throws Exception 
	{
		String path = storagePath+fileSeparator+directoryName;
		File directory = new File(path);
		
		try
		{
			if(directory.exists())
			{
				return directory.getPath();
			}
			
			boolean created = directory.mkdir();
			
			if(created == false)
			{
				logger.error("DriveStorage.createDirectory()- An error has occurred while create the directory.");
				throw new Exception("Could not create directory.");
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
			logger.error("DriveStorage.deleteDirectory()- An error has occurred while deleting the directory.", e);
			throw e;
		}
	}

	@Supported
	public String renameDirectory(String oldDirectoryPath, String newDirectoryName) throws Exception 
	{
		File oldDirectory = new File(oldDirectoryPath);
		File newDirectory = new File(storagePath+fileSeparator+newDirectoryName);
		boolean renamed = false;
		
		try
		{
			if(oldDirectory.exists())
			{
				renamed = oldDirectory.renameTo(newDirectory);
			}
			
			else
			{
				throw new FileNotFoundException("Directory "+oldDirectoryPath+" could not be renamed.");
			}
			
			if(renamed == false)
			{
				throw new Exception("Could not renamed directory.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.renameDirectory() - An error has occurred while renaming a directory: "+oldDirectoryPath+" to "+newDirectoryName+".", e);
			throw e;
		}
		
		return newDirectory.getPath();
	}

	@Supported
	public String upload(String folder, File file) throws Exception 
	{
		String fullPath = storagePath+fileSeparator+folder+fileSeparator+file.getName();
		File copiedFile = new File(fullPath);
		
		try
		{
			if(file.exists())
			{
				FileUtils.copyFile(file, copiedFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+file.getAbsolutePath()+" could not be upload, because it was deleted.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.upload() - An error has occurred while uploading file: "+file.getPath()+".", e);
			throw e;
		}
		
		return copiedFile.getPath();
	}

	@Supported
	public String copy(String folder, String renameFile, File file) throws Exception 
	{
		String fullPath = storagePath+fileSeparator+folder+fileSeparator+renameFile;
		File copiedToFile = new File(fullPath);
		
		try
		{
			if(file.exists())
			{
				FileUtils.copyFile(file, copiedToFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+file.getAbsolutePath()+" could not be copied, because it was deleted.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.upload()- An error has occurred while copying files.", e);
			throw e;
		}
		
		return fullPath;
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
			logger.error("DriveStorage.delete() - An error has occurred while deleting the file: "+filePath+".", e);
			throw e;
		}	
	}

	@Supported
	public File retrieve(String filePath) throws Exception 
	{
		File file = new File(filePath);

		if(file.exists() == false)
		{
			logger.error("DriveStorage.retrieve()- An error has occurred while retrieving the file: "+filePath+".");
			throw new FileNotFoundException("File "+filePath+" could not be found.");
		}

		return file;
	}

	@Supported
	public String update(String filePath, File file) throws Exception 
	{
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
				throw new FileNotFoundException("File "+filePath+" could not be updated, because it was deleted.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.update()- An error has while updating file: "+filePath+".", e);
			throw e;
		}
		
		return copiedFile.getPath();
	}

	@Supported
	public String renameFile(String filepath, String newFileName) throws Exception 
	{
		File renamedFile = null;
		
		try
		{
			File file = new File(filepath);
			
			if(file.exists())
			{
				File parent = file.getParentFile();
				renamedFile = new File(parent.getPath()+fileSeparator+newFileName);
				file.renameTo(renamedFile);
			}
			
			else
			{
				throw new FileNotFoundException("File "+filepath+" could not be renamed.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.renamedFile() - An error has occurred while renaming the file: "+filepath+" to "+newFileName+".", e);
			throw e;
		}
		
		return renamedFile.getPath();
	}

	@Supported
	public List<String> getAvailableFileExtensions() throws Exception 
	{
		return new ArrayList<String>();
	}

	@Supported
	public long getMemoryUsage() throws Exception 
	{
		return FileUtils.sizeOfDirectory(new File(storagePath));
	}

	@Supported
	public long getMemoryLimitation() throws Exception 
	{
		return this.memoryLimitation;
	}

	@Supported
	public List<File> getAllFiles() throws Exception 
	{
		DriveStorageFileFilter filter = new DriveStorageFileFilter(true);
		return new ArrayList<File>(FileUtils.listFiles(new File(storagePath), filter, filter));
	}

	@Supported
	public List<File> getAllFilesForDirectory(String directoryPath) throws Exception 
	{
		DriveStorageFileFilter filter = new DriveStorageFileFilter(false);
		return new ArrayList<File>(FileUtils.listFiles(new File(directoryPath), filter, filter));
	}

	public boolean isCloudStorage() 
	{
		return false;
	}
	
	@Supported
	public File zipUpAllFiles(String archiveName) throws Exception
	{
		TFile archive = new TFile(tempFolderPath+File.separatorChar+archiveName);
		
		try
		{
			TFile direct = new TFile(storagePath);
			direct.cp_rp(archive);
			
			TVFS.umount(archive);
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.zipUpAllFiles()- An error has occurred while zip up files.", e);
			throw e;
		}
		
		return archive;
	}
	
	private class DriveStorageFileFilter implements IOFileFilter
	{
		private boolean includeDirectories = false;
		
		public DriveStorageFileFilter(boolean includeDirectories)
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
				throw new FileNotFoundException("Directory "+fullPath+" could not be zipped up.");
			}
		}
		catch(Exception e)
		{
			logger.error("DriveStorage.zipUpDirectory()- An error has occurred while zip up files.", e);
			throw e;
		}
		
		return archive;
	}
}

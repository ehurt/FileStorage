package org.church.management.temporary.file.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Trae
 *
 * This class will create temporary files and directories for
 * file download and creation.
 *
 * 		temporaryFolderPath: the location where the temporary files are created.
 * 
 */
public class TemporaryFileFactory 
{
	private static final Logger logger = Logger.getLogger(TemporaryFileFactory.class);
	
	//the location where the temporary folder for files to be created in.
	private static String temporaryFolderPath;
	private static long counter = 0;
	
	static
	{
		URL url = Thread.currentThread().getContextClassLoader().getResource("temporaryFile.config.properties");
		String path = url.getPath().replace("%20", " ");
		
		Properties properties = new Properties();
		
		try
		{
			properties.load(new FileInputStream(path));
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.<init> could not read the config file.", e);
		}
		
		String tempPath = properties.getProperty("temporaryFilePath");
	
		if(tempPath.contains("${userdir}"))
		{
			String classPath = System.getProperty("user.dir");
			temporaryFolderPath = tempPath.replace("${userdir}", classPath);
		}
		
		else
		{
			temporaryFolderPath = properties.getProperty("temporaryFilePath");
		}
		
	}
	
	public static File createFile(String filename) throws Exception
	{
		File file = null;
		
		try
		{
			String fullPath = temporaryFolderPath+"\\"+filename;
			file = new File(fullPath);
			boolean check = file.createNewFile();
			
			if(check == false)
			{
				FileUtils.forceDelete(file);
				file.createNewFile();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createFile()- Could not create file: "+filename+".", e);
			throw e;
		}
		
		return file;
	}
	
	public static File createFileInDirectory(String directory, String filename) throws Exception
	{
		File file = null;
		
		try
		{
			file = new File(directory, filename);
			boolean check = file.createNewFile();
			
			if(check == false)
			{
				FileUtils.forceDelete(file);
				file.createNewFile();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createFileInDirectory()- Could not create file: "+filename+".", e);
			throw e;
		}
		
		
		return file;
	}
	
	public static synchronized File createTempFile(String extension) throws Exception
	{
		File file = null;
		
		try
		{
			counter++;
			String filename= "file_"+counter+"."+extension;
			String fullPath = temporaryFolderPath+"\\"+filename;
			file = new File(fullPath);
			boolean check = file.createNewFile();
			
			if(check == false)
			{
				FileUtils.forceDelete(file);
				file.createNewFile();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createFile()- Could create temporary file.", e);
			throw e;
		}
		
		return file;
	}
	
	public static File createDirectory(String directoryName) throws Exception
	{
		File file = null;
		
		try
		{
			File directory = new File(temporaryFolderPath, directoryName);
			boolean check = directory.mkdir();
			
			if(check == false)
			{
				FileUtils.forceDelete(directory);
				directory.createNewFile();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createDirectory()- Could not create directory: "+directoryName+".", e);
			throw e;
		}
		
		return file;
	}
	
	public static File createDirectoryInDirectory(String directory, String name)
	{
		
		return null;
	}
	
	public static synchronized File createTempDirectory() throws Exception
	{
		File file = null;
		
		try
		{
			counter++;
			String directoryname= "directory_"+counter;
			String fullPath = temporaryFolderPath+"\\"+directoryname;
			file = new File(fullPath);
			boolean check = file.mkdir();
			
			if(check == false)
			{
				FileUtils.forceDelete(file);
				file.mkdir();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createDirectory()- Could create temporary directory.", e);
			throw e;
		}
		
		return file;
	}
	
	public static synchronized File createTempFileInDirectory(File directory, String extension) throws Exception
	{
		File file = null;
		
		try
		{
			counter++;
			String filename = "file_"+counter+"."+extension;
			
			if(directory.exists())
			{
				String fullPath = directory.getAbsolutePath()+System.getProperty("file.separator")+filename;
				file = new File(fullPath);
				boolean check = file.createNewFile();
				
				if(check == false)
				{
					FileUtils.forceDelete(file);
					file.createNewFile();
				}
			}
			
			else
			{
				throw new FileNotFoundException("The directory does not existed.");
			}
			
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createDirectory()- Could create temporary file in directory: "+directory.getAbsolutePath()+".", e);
			throw e;
		}
		
		return file;
	}
	
	public static synchronized File createTempDirectoryInDirectory(File parentDirectory) throws Exception
	{
		File file = null;
		
		try
		{
			counter++;
			String directoryname= "directory_"+counter;
			String fullPath = parentDirectory+"\\"+directoryname;
			file = new File(fullPath);
			boolean check = file.mkdir();
			
			if(check == false)
			{
				FileUtils.forceDelete(file);
				file.createNewFile();
			}
		}
		catch(Exception e)
		{
			logger.error("TemporaryFileFactory.createDirectoryInDirectory()- Could not create directory in directory: "+parentDirectory+".", e);
			throw e;
		}
		
		return file;
	}
	
	public static String getTemporaryFilePath()
	{
		return temporaryFolderPath;
	}

	public static synchronized void reset()
	{
		counter = 0;
	}
}

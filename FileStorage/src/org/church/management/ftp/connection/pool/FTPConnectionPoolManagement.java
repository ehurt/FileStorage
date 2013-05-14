package org.church.management.ftp.connection.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.church.management.ftp.connection.pool.exception.ConnectionPoolExhaustedException;
import org.church.management.ftp.session.FTPSession;
import org.church.management.ftp.session.impl.FTPSessionImpl;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPCodes;
import it.sauronsoftware.ftp4j.FTPCommunicationListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class FTPConnectionPoolManagement 
{
	private static final Logger logger = Logger.getLogger(FTPConnectionPoolManagement.class);
	
	private int minimumPoolSize;
	private int maximumPoolSize;
	private int currentPoolSize;
	
	private String host;
	private Integer port;

	private String username;
	private String password;
	
	private boolean passive;
	private int noopTimeout;
	
	private String storageFolder;
	private String currentStorageFolderPath;
	
	private List<FTPClient> pool = new ArrayList<FTPClient>();
	
	public FTPConnectionPoolManagement(String host, int port, String username, String password, String storageFolder, boolean passive, int minimumPoolSize, int maximumPoolSize, int noopTimeout) throws Exception
	{
		this.maximumPoolSize = maximumPoolSize;
		this.minimumPoolSize = minimumPoolSize;
		this.currentPoolSize = 0;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.passive = passive;
		this.noopTimeout = noopTimeout;
		this.storageFolder = storageFolder;
		
		this.initialize();
	}
	
	public FTPConnectionPoolManagement(String host, String username, String password, String storageFolder, boolean passive, int minimumPoolSize, int maximumPoolSize, int noopTimeout) throws Exception
	{
		this.maximumPoolSize = maximumPoolSize;
		this.minimumPoolSize = minimumPoolSize;
		this.currentPoolSize = 0;
		this.host = host;
		this.username = username;
		this.password = password;
		this.passive = passive;
		this.noopTimeout = noopTimeout;
		this.storageFolder = storageFolder;
		
		this.initialize();
	}
	
	public int getActiveConnections()
	{
		return currentPoolSize;
	}
	
	public int getPoolSize()
	{
		return pool.size();
	}
	
	public String getFullStorageFolderPath()
	{
		return this.currentStorageFolderPath;
	}
	
	public boolean isConnected()
	{
		for(FTPClient client: pool)
		{
			if(client.isConnected())
			{
				return true;
			}
		}
		
		return false;
	}
	
	private void initialize() throws Exception
	{
		for(int i = 0; i < minimumPoolSize; i++)
		{
			FTPClient client = createConnection();
			pool.add(client);
		}
		
		if(pool.size() > 0)
		{
			currentStorageFolderPath = pool.get(0).currentDirectory();
		}
		
		this.currentPoolSize = pool.size();
		
		DisconnectFromFTPHook hook = new DisconnectFromFTPHook();
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	private FTPClient createConnection() throws Exception
	{
		FTPClient client = new FTPClient();
		
		try
		{
			client = new FTPClient();
			client.setAutoNoopTimeout(noopTimeout);
			
			if(port != null)
			{
				client.connect(host, port);
			}
			
			else
			{
				client.connect(host);
			}
			
			client.login(username, password);
			client.addCommunicationListener(new CommunicationListener());
			
			boolean compression = client.isCompressionSupported();
			
			if(compression)
			{
				client.setCompressionEnabled(true);
			}
			
			if(passive)
			{
				
			}
			
			else
			{
				
			}
			
			changeDirectory(client);
		
		}
		catch(FTPException e)
		{
			//TODO fire error event on these exceptions
			logger.error("FTPConnectionPoolManagement.createConnection()- Could not connect to ftp server.", e);
			throw e;
		}
		
		catch (IllegalStateException e) 
		{
			logger.error("FTPConnectionPoolManagement.createConnection()- Could not connect to ftp server.", e);
			throw e;
		} 
		
		catch (IOException e) 
		{
			logger.error("FTPConnectionPoolManagement.createConnection()- Could not connect to ftp server.", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPConnectionPoolManagement.createConnection()- Could not connect to ftp server.", e);
			throw e;
		}
		
		return client;
	}
	
	private void changeDirectory(FTPClient client) throws FTPException, IllegalStateException, IOException, FTPIllegalReplyException
	{
		String directory ="";
		
		try
		{
			directory = client.currentDirectory();
			client.changeDirectory(directory+storageFolder);
		}
		catch(FTPException e)
		{
			int code = e.getCode();
			
			if(code == FTPCodes.FILE_NOT_FOUND && directory.equals("") == false)
			{
				logger.debug("FTPConnectionPoolManagement.changeDirectory()- Create the current directory: "+directory+storageFolder);
				client.createDirectory(directory+storageFolder);
				client.changeDirectory(directory+storageFolder);
			}
			
			else
			{
				logger.error("FTPConnectionPoolManagement.changeDirectory()- Could not access the storage folder directory.", e);
				throw e;
			}
		}
		
		catch (IllegalStateException e) 
		{
			logger.error("FTPConnectionPoolManagement.changeDirectory()- Could not access the storage folder directory.", e);
			throw e;
		}
		
		catch (IOException e) 
		{
			logger.error("FTPConnectionPoolManagement.changeDirectory()- Could not create the storage folder directory.", e);
			throw e;
		} 
		
		catch (FTPIllegalReplyException e) 
		{
			logger.error("FTPConnectionPoolManagement.changeDirectory()- Could not access the storage folder directory.", e);
			throw e;
		}
	}
	
	public synchronized FTPSession openSession() throws Exception
	{
		FTPSession session = null;
		
		if(pool.size() > 0)
		{
			FTPClient client = pool.remove(0);
			
			if(client.isConnected())
			{	
				if(client.currentDirectory().equals(currentStorageFolderPath) == false)
				{
					client.changeDirectory(currentStorageFolderPath);
					logger.debug("Had to change the directory for a client back to the root.");
				}
				
				session = new FTPSessionImpl(currentStorageFolderPath, client, this);
			}
			
			//replace connection that was broken.
			else
			{
				closeConnection(client);
				client = createConnection();
				session = new FTPSessionImpl(currentStorageFolderPath, client, this);
			}
		}
		
		else if(pool.size() == 0 && this.currentPoolSize < this.maximumPoolSize)
		{
			FTPClient client = createConnection();
			this.currentPoolSize++;
			session = new FTPSessionImpl(currentStorageFolderPath, client, this);
		}
		
		else if(pool.size() == 0 && this.currentPoolSize == this.maximumPoolSize)
		{
			throw new ConnectionPoolExhaustedException("", null);
		}
		
		return session;
	}
	
	public synchronized void returnConnection(FTPClient client)
	{
		System.out.println("Connection returned to the pool.");
		this.pool.add(client);
	}
	
	public synchronized void disconnect()
	{
		for(FTPClient client: pool)
		{
			closeConnection(client);
		}
		
		pool.clear();
		this.currentPoolSize = 0;
	}
	
	public synchronized void reconnect() throws Exception
	{
		try 
		{
			initialize();
		} 
		catch (Exception e) 
		{
			logger.error("FTPConnectionPoolManagement.reconnect()- Failed to reconnect to the ftp server.", e);
			throw e;
		}
	}
	
	private void closeConnection(FTPClient client)
	{
		try
		{
			client.disconnect(true);
		}
		catch(Exception e)
		{
			logger.error("FTPConnectionPoolManagement.closeConnection()- Connection failed to close.", e);
		}
	}
	
	private class CommunicationListener implements FTPCommunicationListener
	{
		public void received(String arg0) 
		{

		}

		public void sent(String arg0) 
		{

		}
	}
	
	/**
	 * 
	 * @author Trae
	 *
	 * This class will disconnect the connection from the ftp server
	 * once java shuts down.
	 */
	private class DisconnectFromFTPHook extends Thread
	{
		public void run()
		{
			System.out.println("Disconnecting the ftp clients.");
			
			for(FTPClient client: pool)
			{
				try
				{
					if(client != null && client.isConnected())
					{
						client.disconnect(true);
						logger.debug("Disconnecting from the FTP server.");
					}
				}
				catch(Exception e)
				{
					logger.error("FTPStorage.DisconnectFromFTPHook.run()- Could not disconnect from the ftp server.", e);
				}
			}
		}
	}

}

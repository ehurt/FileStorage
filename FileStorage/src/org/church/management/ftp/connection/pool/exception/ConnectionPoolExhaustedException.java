package org.church.management.ftp.connection.pool.exception;

public class ConnectionPoolExhaustedException extends Exception 
{

	private static final long serialVersionUID = 1L;

	public ConnectionPoolExhaustedException(String message, Throwable ex)
	{
		super(message, ex);
	}
}

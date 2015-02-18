package com.defiancecraft.core.database;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import com.mongodb.MongoSocketException;

/**
 * Wrapper class for ThreadPoolExecutor (which is normally
 * instantiated via {@link java.util.concurrent.Executors#newFixedThreadPool(int) newFixedThreadPool(int)})
 * The difference about this class is that any exceptions thrown from within
 * the task will be caught, and, if a database exception, the task will be
 * retried. If the exception is anything else, it is printed to console with
 * a stack trace.
 */
public class DatabaseExecutorService extends ThreadPoolExecutor {

	public DatabaseExecutorService(int nThreads) {
		super(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(new DatabaseCallable<T>(task));
	}
	
	@Override
	public Future<?> submit(Runnable task) {
		return super.submit(new DatabaseRunnable<>(task));
	}
	
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return super.submit(new DatabaseRunnable<T>(task, result));
	}

	public abstract class DatabaseTask<T> implements Callable<T> {

		protected abstract T innerCall() throws Exception;
		
		public T call() throws Exception {
			
			int attempt = 1;
			
			while (true) {
				try {
					
					return this.innerCall();
					
				} catch (MongoSocketException | IOException e) {
					
					Bukkit.getLogger().warning(String.format("Failed to connect to the database! Attempt #%d.", attempt++));
					
				} catch (Throwable t) {
					
					Bukkit.getLogger().severe(
						"==================================\n" +
						"=         CRITICAL ERROR         =\n" +
						"==================================\n" +
						"=  An exception occurred while   =\n" +
						"=   performing a database task.  =\n" +
						"==================================\n" +
						"= Message: " + t.getMessage() + "\n" +
	 					"= Exception Type: " + t.getClass().getCanonicalName() + "\n" +
						"= Stack Trace:"
	 				);
					t.printStackTrace();
					
					throw t;
					
				}
			}
			
		}
		
	}
	
	public class DatabaseCallable<T> extends DatabaseTask<T> {
		
		private Callable<T> task;
		
		public DatabaseCallable(Callable<T> task) {
			this.task = task;
		}
		
		protected T innerCall() throws Exception {
			return this.task.call();
		}
		
	}
	
	public class DatabaseRunnable<T> extends DatabaseTask<T> {
		
		private Runnable task;
		private T defaultResult;
		
		public DatabaseRunnable(Runnable task) {
			this(task, null);
		}
		
		public DatabaseRunnable(Runnable task, T result) {
			this.task = task;
			this.defaultResult = result;
		}
		
		protected T innerCall() throws Exception {
			this.task.run();
			return defaultResult;
		}
		
	}
	
}

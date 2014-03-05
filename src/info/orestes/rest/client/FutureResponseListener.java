//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package info.orestes.rest.client;

import info.orestes.rest.service.EntityType;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;

/**
 * A {@link BufferingResponseListener} that is also a {@link Future}, to allow
 * applications to block (indefinitely or for a timeout) until
 * {@link #onComplete(Result)} is called, or to {@link #cancel(boolean) abort}
 * the request/response conversation.
 * <p />
 * Typical usage is:
 * 
 * <pre>
 * Request request = httpClient.newRequest(...)...;
 * FutureResponseListener listener = new FutureResponseListener(request);
 * request.send(listener); // Asynchronous send
 * ContentResponse response = listener.get(5, TimeUnit.SECONDS); // Timed block
 * </pre>
 */
public class FutureResponseListener<T> extends EntityResponseListener<T> implements Future<EntityResponse<T>>
{
	private final CountDownLatch latch = new CountDownLatch(1);
	private EntityResponse<T> response;
	private Throwable failure;
	private volatile boolean cancelled;
	
	public FutureResponseListener(Class<T> entityType) {
		this(new EntityType<>(entityType));
	}
	
	public FutureResponseListener(EntityType<T> entityType) {
		super(entityType);
	}
	
	public Throwable getFailure() {
		return failure;
	}
	
	@Override
	public void onComplete(EntityResult<T> result)
	{
		response = new HttpEntityResponse<T>(result.getResponse(), getEntityType(), result.getEntity());
		failure = result.getFailure();
		latch.countDown();
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		cancelled = true;
		return getRequest().abort(new CancellationException());
	}
	
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	@Override
	public boolean isDone()
	{
		return latch.getCount() == 0 || isCancelled();
	}
	
	@Override
	public EntityResponse<T> get() throws InterruptedException, ExecutionException
	{
		latch.await();
		try {
			return getResult();
		} catch (TimeoutException e) {
			throw new ExecutionException(e);
		}
	}
	
	@Override
	public EntityResponse<T> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
			TimeoutException
	{
		boolean expired = !latch.await(timeout, unit);
		if (expired) {
			throw new TimeoutException();
		}
		
		return getResult();
	}
	
	private EntityResponse<T> getResult() throws ExecutionException, TimeoutException {
		if (isCancelled()) {
			throw (CancellationException) new CancellationException().initCause(failure);
		}
		
		if (failure != null) {
			if (failure instanceof TimeoutException) {
				throw (TimeoutException) failure;
			} else {
				throw new ExecutionException(failure);
			}
		}
		
		return response;
	}
}

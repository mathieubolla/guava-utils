package com.mathieubolla.guava;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelUtils {
	public static <T, U> Iterable<U> parallelTransform(final Iterable<T> source, final Function<T, U> transform, int factor) {
		ExecutorService executorService = Executors.newFixedThreadPool(factor);
		return parallelTransform(source, transform, factor, executorService);
	}

	/**
	 * Computes transform on source, factor elements at a time, and iterates over these in source order, tapping into executorService threadPool
	 * BEWARE: executorService should be truly parallel, i.e. should work with at least 2 threads
	 * @param source
	 * @param transform
	 * @param factor
	 * @param executorService
	 * @param <T>
	 * @param <U>
	 * @return
	 */
	public static <T, U> Iterable<U> parallelTransform(final Iterable<T> source, final Function<T, U> transform, int factor, final ExecutorService executorService) {
		final LinkedBlockingQueue<FutureTask<U>> queue = new LinkedBlockingQueue<FutureTask<U>>((factor - 1) * 2);
		final AtomicBoolean finished = new AtomicBoolean(false);
		final Iterator<T> sourceIterator = source.iterator();

		return new Iterable<U>() {
			public Iterator<U> iterator() {
				executorService.submit(new Runnable() {
					public void run() {
						if (sourceIterator.hasNext()) {
							FutureTask<U> scheduledFuture = new FutureTask<U>(new Callable<U>() {
								public U call() throws Exception {
									return transform.apply(sourceIterator.next());
								}
							});
							try {
								executorService.submit(scheduledFuture);
								queue.put(scheduledFuture);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							finished.set(true);
						}
					}
				});

				return new AbstractIterator<U>() {
					@Override
					protected U computeNext() {
						if (queue.isEmpty() && finished.get()) {
							return endOfData();
						}
						try {
							return queue.take().get();
						} catch (InterruptedException e) {
							throw Throwables.propagate(e);
						} catch (ExecutionException e) {
							throw Throwables.propagate(e);
						}
					}
				};
			}
		};
	}
}

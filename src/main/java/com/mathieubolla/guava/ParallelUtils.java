package com.mathieubolla.guava;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.concurrent.*;

import static com.google.common.collect.FluentIterable.from;

public class ParallelUtils {
	/**
	 * @see #parallelTransform(Iterable, com.google.common.base.Function, int, java.util.concurrent.ExecutorService)
	 *      Will create a fixed thread pool executor service, and shut it down at iterator's end
	 */
	public static <T, U> Iterable<U> parallelTransform(final Iterable<T> source, final Function<T, U> transform, int factor) {
		ExecutorService executorService = Executors.newFixedThreadPool(factor);
		return doTransformStuf(source, transform, factor, executorService, true);
	}

	/**
	 * Computes transform on source, factor elements at a time, and iterates over these in source order, tapping into executorService threadPool
	 */
	public static <T, U> Iterable<U> parallelTransform(final Iterable<T> source, final Function<T, U> transform, int factor, final ExecutorService executorService) {
		return doTransformStuf(source, transform, factor, executorService, false);
	}

	/**
	 * Computes filter on source, factor elements at a time, and iterates over these in source order, tapping into executorService threadPool
	 */
	public static <T> Iterable<T> parallelFilter(Iterable<T> source, Predicate<T> predicate, int factor, ExecutorService executorService) {
		return doFilterStuf(source, predicate, factor, executorService, false);
	}

	/**
	 * @see #parallelFilter(Iterable, com.google.common.base.Predicate, int, java.util.concurrent.ExecutorService)
	 *      Will create a fixed thread pool executor service, and shut it down at iterator's end
	 */
	public static <T> Iterable<T> parallelFilter(Iterable<T> source, Predicate<T> predicate, int factor) {
		ExecutorService executorService = Executors.newFixedThreadPool(factor);
		return doFilterStuf(source, predicate, factor, executorService, true);
	}

	private static <T, U> Iterable<U> doTransformStuf(final Iterable<T> source, final Function<T, U> transform, int factor, final ExecutorService executorService, final boolean shutdownInTheEnd) {
		Preconditions.checkArgument(factor > 0, "You should really be using strictly positive factors when doing parallel computing");

		final LinkedBlockingQueue<FutureTask<U>> queue = new LinkedBlockingQueue<FutureTask<U>>(factor);
		final Iterator<T> sourceIterator = source.iterator();

		return new Iterable<U>() {
			public Iterator<U> iterator() {
				return new AbstractIterator<U>() {
					@Override
					protected U computeNext() {
						if (queue.isEmpty() && !sourceIterator.hasNext()) {
							if (shutdownInTheEnd) {
								executorService.shutdown();
							}
							return endOfData();
						}

						while (queue.remainingCapacity() > 0 && sourceIterator.hasNext()) {
							final T next = sourceIterator.next();
							FutureTask<U> scheduledFuture = new FutureTask<U>(new Callable<U>() {
								public U call() throws Exception {
									return transform.apply(next);
								}
							});
							try {
								executorService.submit(scheduledFuture);
								queue.put(scheduledFuture);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
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

	private static <T> Iterable<T> doFilterStuf(Iterable<T> source, Predicate<T> predicate, int factor, ExecutorService executorService, boolean shutdownInTheEnd) {
		return from(doTransformStuf(source, toResult(predicate), factor, executorService, shutdownInTheEnd)).filter(new Predicate<Pair<T, Boolean>>() {
			public boolean apply(Pair<T, Boolean> input) {
				return input.b;
			}
		}).transform(new Function<Pair<T, Boolean>, T>() {
			public T apply(Pair<T, Boolean> input) {
				return input.a;
			}
		});
	}

	private static <A> Function<A, Pair<A, Boolean>> toResult(final Predicate<A> predicate) {
		return new Function<A, Pair<A, Boolean>>() {
			public Pair<A, Boolean> apply(A input) {
				return new Pair<A, Boolean>(input, predicate.apply(input));
			}
		};
	}

	private static class Pair<A, B> {
		private final A a;
		private final B b;

		Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}
}

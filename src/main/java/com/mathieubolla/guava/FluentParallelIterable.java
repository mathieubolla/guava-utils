package com.mathieubolla.guava;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.Iterator;

public abstract class FluentParallelIterable<E> extends FluentIterable<E> {
	private final Iterable<E> delegate;

	FluentParallelIterable(Iterable<E> delegate) {
		this.delegate = delegate;
	}

	public static <E> FluentParallelIterable<E> from(final Iterable<E> source) {
		if (source instanceof FluentParallelIterable) {
			return (FluentParallelIterable<E>)source;
		}

		return new FluentParallelIterable<E>(source) {
			public Iterator<E> iterator() {
				return source.iterator();
			}
		};
	}

	public final <T> FluentParallelIterable<T> parallelTransform(Function<E, T> function, int factor) {
		return from(ParallelUtils.parallelTransform(delegate, function, factor));
	}

	public final FluentParallelIterable<E> parallelFilter(Predicate<E> predicate, int factor) {
		return from(ParallelUtils.parallelFilter(delegate, predicate, factor));
	}
}

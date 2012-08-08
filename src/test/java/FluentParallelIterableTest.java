import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.mathieubolla.guava.FluentParallelIterable;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fest.assertions.Assertions.assertThat;

public class FluentParallelIterableTest {
	@Test
	public void shouldComputeAll() {
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(1, 2, 3));

		FluentIterable<Integer> parallelMultiply = fluentParallelIterable.parallelTransform(new Function<Integer, Integer>() {
			public Integer apply(Integer input) {
				return input * 2;
			}
		}, 2);

		assertThat(parallelMultiply).containsOnly(2, 4, 6);
	}

	@Test
	public void shouldFilterAll() {
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(-2, -1, 1, 2));

		FluentIterable<Integer> parallelPositives = fluentParallelIterable.parallelFilter(new Predicate<Integer>() {
			public boolean apply(Integer input) {
				return input > 0;
			}
		}, 2);

		assertThat(parallelPositives).containsOnly(1, 2);
	}

	@Test(timeout = 200)
	public void shouldComputeAllWithOneSideThread() {
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(1, 2, 3));

		FluentIterable<Integer> parallelMultiply = fluentParallelIterable.parallelTransform(new Function<Integer, Integer>() {
			public Integer apply(Integer input) {
				return input * 2;
			}
		}, 2, Executors.newSingleThreadExecutor());

		assertThat(parallelMultiply).containsOnly(2, 4, 6);
	}

	@Test(timeout = 200)
	public void shouldFilterAllWithOneSideThread() {
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(-2, -1, 1, 2));

		FluentIterable<Integer> parallelPositives = fluentParallelIterable.parallelFilter(new Predicate<Integer>() {
			public boolean apply(Integer input) {
				return input > 0;
			}
		}, 2, Executors.newSingleThreadExecutor());

		assertThat(parallelPositives).containsOnly(1, 2);
	}

	@Test(timeout = 200)
	public void shouldTransformInParallel() {
		final AtomicBoolean hasBeenInterrupted = new AtomicBoolean(false);
		final CountDownLatch lock = new CountDownLatch(2);
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(1, 2));

		FluentIterable<Integer> interlockingTransform = fluentParallelIterable.parallelTransform(new Function<Integer, Integer>() {
			public Integer apply(Integer input) {
				lock.countDown();
				try {
					lock.await();
				} catch (InterruptedException e) {
					hasBeenInterrupted.set(true);
					throw new RuntimeException(e);
				}
				return input;
			}
		}, 2);

		assertThat(hasBeenInterrupted.get()).isFalse();
		assertThat(interlockingTransform).containsOnly(1, 2);
	}

	@Test(timeout = 200)
	public void shouldFilterInParallel() {
		final AtomicBoolean hasBeenInterrupted = new AtomicBoolean(false);
		final CountDownLatch lock = new CountDownLatch(2);
		FluentParallelIterable<Integer> fluentParallelIterable = FluentParallelIterable.from(Arrays.asList(1, 2));

		FluentIterable<Integer> interlockingTransform = fluentParallelIterable.parallelFilter(new Predicate<Integer>() {
			public boolean apply(Integer input) {
				lock.countDown();
				try {
					lock.await();
				} catch (InterruptedException e) {
					hasBeenInterrupted.set(true);
					throw new RuntimeException(e);
				}
				return true;
			}
		}, 2);

		assertThat(hasBeenInterrupted.get()).isFalse();
		assertThat(interlockingTransform).containsOnly(1, 2);
	}
}

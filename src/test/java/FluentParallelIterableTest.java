import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.mathieubolla.guava.FluentParallelIterable;
import org.junit.Test;

import java.util.Arrays;

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
}

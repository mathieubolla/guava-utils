Guava Utils (0.0.1-SNAPSHOT)
---------------

**Main features**

This tool extends Guava's FluentIterable and provided transform(Function) / filter(Predicate) methods by adding:

  - parallelTransform(Function, Int) to spawn a thread pool and process the Function in parallel
  - parallelTransform(Function, Int, ExecutorService) to process the Function in parallel using provided ExecutorService
  - parallelFilter(Predicate, Int) to spawn a thread pool and process the Predicate in parallel
  - parallelFilter(Predicate, Int, ExecutorService) to process the Predicate in parallel using provided ExecutorService

**Getting started**

You can get the source by cloning git repository like that:

    git clone git://github.com/mathieubolla/guava-utils.git

Then, build using Maven 3:

    mvn clean install

OR

You can use our pre-built binaries using the following Maven repository:

    <repository>
		<id>maven-mathieu-bolla-com-s3-snapshot-repo</id>
		<name>www.mathieu-bolla.com S3 Snapshot Repository</name>
		<url>s3://www.mathieu-bolla.com/maven//snapshot</url>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>

**Code sample**

Lazily get each of {1!, 2!, 3!, 4!} which is a prime (fortunately, all of them), given FACTORIAL the factorial function Function<Integer, Integer> and IS_PRIME the prime predicate Predicate<Integer>, using 4 compute threads:

    FluentParallelIterable.from(Arrays.asList(1, 2, 3, 4)).parallelTransform(FACTORIAL, 4).parallelFilter(IS_PRIME, 4);
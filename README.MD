# Running the benchmark:

* in the root directory run `mvn install`
* `cd bench`
* `mvn clean verify`
* `java -jar target/benchmarks.jar`

Output
======

Result "org.sample.MyBenchmark.benchPrunedMinimaxWithoutCaching":
3922661.357 ±(99.9%) 243755.756 us/op [Average]
(min, avg, max) = (3868598.263, 3922661.357, 4002611.063), stdev = 63302.596
CI (99.9%): [3678905.601, 4166417.113] (assumes normal distribution)


# Run complete. Total time: 01:29:30

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

NOTE: Current JVM experimentally supports Compiler Blackholes, and they are in use. Please exercise
extra caution when trusting the results, look into the generated code to check the benchmark still
works, and factor in a small probability of new VM bugs. Additionally, while comparisons between
different JVMs are already problematic, the performance difference caused by different Blackhole
modes can be very significant. Please make sure you use the consistent Blackhole mode for comparisons.

Benchmark                                     (depth)  Mode  Cnt         Score          Error  Units
MyBenchmark.benchMinimaxWithCaching                 2  avgt    5         6.748 ±        2.784  us/op
MyBenchmark.benchMinimaxWithCaching                 3  avgt    5         4.821 ±        0.222  us/op
MyBenchmark.benchMinimaxWithCaching                 4  avgt    5         5.154 ±        2.420  us/op
MyBenchmark.benchMinimaxWithCaching                 5  avgt    5         6.626 ±        3.270  us/op
MyBenchmark.benchMinimaxWithCaching                 6  avgt    5         5.712 ±        3.681  us/op
MyBenchmark.benchMinimaxWithoutCaching              2  avgt    5      7710.349 ±      196.005  us/op
MyBenchmark.benchMinimaxWithoutCaching              3  avgt    5    109336.760 ±      822.311  us/op
MyBenchmark.benchMinimaxWithoutCaching              4  avgt    5    976180.349 ±   140082.720  us/op
MyBenchmark.benchMinimaxWithoutCaching              5  avgt    5   6645707.950 ±   300126.108  us/op
MyBenchmark.benchMinimaxWithoutCaching              6  avgt    5  66193965.693 ± 25194108.212  us/op
MyBenchmark.benchPrunedMinimaxWithCaching           2  avgt    5      3097.477 ±      227.209  us/op
MyBenchmark.benchPrunedMinimaxWithCaching           3  avgt    5     16606.179 ±     1913.252  us/op
MyBenchmark.benchPrunedMinimaxWithCaching           4  avgt    5    165020.085 ±    14879.083  us/op
MyBenchmark.benchPrunedMinimaxWithCaching           5  avgt    5   1008567.521 ±    87508.312  us/op
MyBenchmark.benchPrunedMinimaxWithCaching           6  avgt    5   1255918.591 ±    69296.760  us/op
MyBenchmark.benchPrunedMinimaxWithoutCaching        2  avgt    5      3094.707 ±      261.289  us/op
MyBenchmark.benchPrunedMinimaxWithoutCaching        3  avgt    5     46019.454 ±     3582.397  us/op
MyBenchmark.benchPrunedMinimaxWithoutCaching        4  avgt    5    471451.834 ±    48043.841  us/op
MyBenchmark.benchPrunedMinimaxWithoutCaching        5  avgt    5   3126092.208 ±   144552.464  us/op
MyBenchmark.benchPrunedMinimaxWithoutCaching        6  avgt    5   3922661.357 ±   243755.756  us/op

enablePlugins(JmhPlugin)

addCommandAlias("lang-run-performance-test", "; lang/clean; lang/jmh:run -i 10 -wi 10 -f1 -t1 com.wavesplatform.lang.benchmark.CryptoBenchmark")

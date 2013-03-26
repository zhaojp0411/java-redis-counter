java-redis-counter
==================

背景介绍：
新浪微博平台架构官方帐号发起了一个计数器擂台赛，需求如下：

阅读计数器，实现类似新浪微博的阅读数

提供一个解决方案，可以用任意语言及及依赖开源软件(如Redis)实现，但是需要运行在Linux环境；
封装成简单的library/sdk，可供在php或Java调用，支持 inc()/multiInc() or/and get()/multiGet()；
具备高可用性，要求 99.9% 以上，不能有数据丢失；

假定系统有1亿用户，每天1000万新增计数，支持20万QPS+支撑能力
计数允许有一定粗略度，要求99%的准确度，可以理解假定阅读100次，计数器存成99或101>；
另外从inc到get生效之间可允许有5秒左右的延迟。

合适的方案：综合QPS、可用性、硬件TCO、粗略度、延迟等因素，取得最佳收益的方案。

解决方案：

java-redis-counter设计用来满足以上需求，初步考虑如下：

Thrift RPC: 20W+ QPS 
JVM-in Memory Counter:100 billion key in memory，about 10day 
TimeOut:LRU
AppendLog: Recovery in memory data
HBase:All Data Storage

说明：

1、为什么不采用Redis？
Redis采用C实现（虽然代码行只有2万多），驾驭不了；
Redis要求所有数据都在内存中，满足不了每天1000万新增计数的存储需求；
如果冷热数据分离的话，增加了调用方的复杂度，并且需要运维定期进行数据清理；

2、为什么选HBase？
支持海量数据存储，单个RegionServer incr写入操纵能够5000+QPS。

3、Jvm-In Memory 的数据结构？
ConcurrentHashMap<String,AtomicInteger>
NonBlockingHashMapLong<AtomicInteger> 解决并发写入的原子性问题，以及内存空间占用问题。
3000万 key-value占用JVM内存8GB（80%）

4、In Memory数据宕机恢复？
所有写入操纵将会生成appendlog（类似redis的aof文件），重启从log恢复数据（1亿数据恢复时间：6分钟），定期进行日志重写（类似redis的bgrewrite）；


一些基本的性能：

in-Memory NonBlockingHashMapLong<AtomicInteger>  (8cpu 12GB RAM 8G JVM)
get  17w+
add 17w+

hbase Increment (3 region server)
get 1.1w+
add 0.8w+

10% in hbase 90% in memory
get 9w+
add 7w+

Memory Capcaity : JVM 8G  3000w Key 内存占用82%（读取性能无明显衰减）
 S0     S1     E      O      P     YGC     YGCT    FGC    FGCT     GCT   
 75.00   0.00  71.34  82.10  68.21    270   34.272     0    0.000   34.272
 75.00   0.00  96.44  82.10  68.21    270   34.272     0    0.000   34.272
  0.00  75.00  20.01  82.10  68.21    271   34.361     0    0.000   34.361
  0.00  75.00  45.39  82.10  68.21    271   34.361     0    0.000   34.361

recoverylog load 




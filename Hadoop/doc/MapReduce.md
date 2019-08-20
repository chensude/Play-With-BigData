### MapReduce 概述
#### 1,定义
##### 1.1 mapreduce是一个分布式程序的编程框架。主要是一个计算框架。
```
优点：
1,易于编程
2,良好的扩展性
3,高容错性
4,适合PB级别以上海量数据的离线处理
缺点：
1,不擅长实时计算
2,不擅长流式计算
3,不擅长DAG(有向图）计算
```
##### 1.2 MapReduce阶段
```
1) MrAppMaster: 负责整个程序的过程调度及状态协调
2）MapTask: 负责Map阶段的整个数据处理流程
3）ReduceTask: 负责Reduce阶段的整个数据处理流程
```
##### 1.3 常用序列化类型对比
| java类型 |Hadoop Writable  |
| --- | --- |
| boolean|BooleanWritable|
| byte |ByteWritable|
| int |IntWritable|
| float |FloatWritable|
| long |LongWritable|
| double|DoubleWritable|
| String|Text|
| map |MapWritable|
| array |ArrayWritable|

#### 2,MapReduce框架原理

数据切片：数据切片只是在逻辑上对输入进行分片，并不会在磁盘上将其切分成片进行存储。
```
1)一个job的Map阶段并行度由客户端在提交job的切片数决定的
2)每个split切片分配一个maptask并行实例处理
3）默认情况下，切片大小=BlockSize
4）切片不考虑整体，而是单独对一个文件进行
```
##### 2.1,job提交源码分析
```
waitForCompletion()

submit();

// 1建立连接
connect();	
// 1）创建提交Job的代理
new Cluster(getConfiguration());
// （1）判断是本地yarn还是远程
initialize(jobTrackAddr, conf); 

// 2 提交job
submitter.submitJobInternal(Job.this, cluster)
// 1）创建给集群提交数据的Stag路径
Path jobStagingArea = JobSubmissionFiles.getStagingDir(cluster, conf);

// 2）获取jobid ，并创建Job路径
JobID jobId = submitClient.getNewJobID();

// 3）拷贝jar包到集群
copyAndConfigureFiles(job, submitJobDir);	
rUploader.uploadFiles(job, jobSubmitDir);

// 4）计算切片，生成切片规划文件
writeSplits(job, submitJobDir);
maps = writeNewSplits(job, jobSubmitDir);
input.getSplits(job);

// 5）向Stag路径写XML配置文件
writeConf(conf, submitJobFile);
conf.writeXml(out);

// 6）提交Job,返回提交状态
status = submitClient.submitJob(jobId, submitJobDir.toString(), job.getCredentials());

```
下面结合图来看大概流程
![job提交](../doc/img/job.jpg)
这里我们可以深入了解一下生产切片规划（FileInPutFormat)，我们来进入代码看一下发生了什么(我截取了核心)？
```
//protected long getFormatMinSplitSize() {
//   return 1;
//  }
//public static long getMinSplitSize(JobContext job) {
//    return job.getConfiguration().getLong(SPLIT_MINSIZE, 1L);
//}
// public static final String SPLIT_MINSIZE = 
//    "mapreduce.input.fileinputformat.split.minsize"
//上面注释可以看出来，如果不配置minsize参数，默认为1
long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));


// public static long getMaxSplitSize(JobContext context) {
//    return context.getConfiguration().getLong(SPLIT_MAXSIZE, 
//                                              Long.MAX_VALUE);
// }
//上面注释可以看出来，如果不配置，默认为Long.Max_VALUE
long maxSize = getMaxSplitSize(job);

for (FileStatus file: files) {
  Path path = file.getPath();
  long length = file.getLen();
  if (length != 0) {
    if (isSplitable(job, path)) {
      long blockSize = file.getBlockSize();
      long splitSize = computeSplitSize(blockSize, minSize, maxSize);

      long bytesRemaining = length;
      //SPLIT_SLOP=1.1
      while (((double) bytesRemaining)/splitSize > SPLIT_SLOP) {
        int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
        splits.add(makeSplit(path, length-bytesRemaining, splitSize,
                    blkLocations[blkIndex].getHosts(),
                    blkLocations[blkIndex].getCachedHosts()));
        bytesRemaining -= splitSize;
      }

      if (bytesRemaining != 0) {
        int blkIndex = getBlockIndex(blkLocations, length-bytesRemaining);
        splits.add(makeSplit(path, length-bytesRemaining, bytesRemaining,
                   blkLocations[blkIndex].getHosts(),
                   blkLocations[blkIndex].getCachedHosts()));
      }
    } else { // not splitable
      splits.add(makeSplit(path, 0, length, blkLocations[0].getHosts(),
                  blkLocations[0].getCachedHosts()));
    }
  }
}

补充一下computeSplitSize的代码
Math.max(minSize, Math.min(maxSize, blockSize));
```
总结以上代码：
* 程序先找到你数据存储的目录
* 开始遍历目录下的每一个文件
* 遍历第一个文件ss.txt
* 计算切片的大小，公式：Math.max(minSize, Math.min(maxSize, blockSize));默认等于blockSize
* 开始切，形成第一个切片：ss.txt-0:128M第二个切片128M:256M,第三个切片256M:300M
（每次切片时，都要判断切完剩下的部分是否大于1.1倍，不大于1.1倍就划分为最后一块切片）
* 将切片写入一个切片规划文件中
* InputSplit只记录了切片的元数据信息，比如原始位置，长度已经所在列表等。
* 提交整个规划文件到YARN上,YARN上MrAPPMaster可以根据切片规划文件计算开启MapTask的个数<br/>

##### 2.2,补充CombineTextInputFormat切片类
CombineTextInputFormat:用于小文件过多的场景,可以从逻辑上规划多个小文件到一个文件中。<br/>
1，虚拟存储切片最大值设置
CombineTextInputFormat.setMaxInputSplitSize(job, 4194304);// 4m
注意：虚拟存储切片最大值设置最好根据实际的小文件大小情况来设置具体的值。<br/>
首先用图片看一下整体过程：
![job提交](../doc/img/combine.jpg)
（1）虚拟存储过程：
将输入目录下所有文件大小，依次和设置的setMaxInputSplitSize值比较，如果不大于设置的最大值，逻辑上划分一个块。如果输入文件大于设置的最大值且大于两倍，那么以最大值切割一块；当剩余数据大小超过设置的最大值且不大于最大值2倍，此时将文件均分成2个虚拟存储块（防止出现太小切片）。
（2）切片过程：
（a）判断虚拟存储的文件大小是否大于setMaxInputSplitSize值，大于等于则单独形成一个切片。
（b）如果不大于则跟下一个虚拟存储文件进行合并，共同形成一个切片。
```
例如setMaxInputSplitSize值为4M，输入文件大小为8.02M，则先逻辑上分成一个4M。剩余的大小为4.02M，如果按照4M逻辑划分，就会出现0.02M的小的虚拟存储文件，所以将剩余的4.02M文件切分成（2.01M和2.01M）两个文件。
增加如下代码
// 如果不设置InputFormat，它默认用的是TextInputFormat.class
job.setInputFormatClass(CombineTextInputFormat.class);

//虚拟存储切片最大值设置20m
CombineTextInputFormat.setMaxInputSplitSize(job, 20971520);
运行如果为1个切片。
```
##### 2.3,FileInputFormat实现类
2.3.1 FileInputFormat<br/>
默认实现类是TextInputFormat。按行读取，建是存储该行在整个文件中
的起始偏移量，longWritable类型。值是这行的内容（不包括换行符和回车符）,Text类型。<br/>
2.3.2 KeyValueTextInputFormat<br/>
每一行均为一条记录,被分隔符分割为key，value。可以通过在驱动类中设置
conf.set(KeyValueLineRecordReader.Key_Value_Seperator,"\t")来设定分隔符。默认分隔符是tab("\t")<br/>
2.3.3 NlineInputFormat<bnr/>
如果使用NlineInputFormat，代表分片按照指定的N来划分。即输入文件的总行数/N=切片数。
这里的key和value和TextInputFormat一样。
##### 2.4,MapReduce工作流程
2.4.1 mr的工作流程图
![mr1](../doc/img/mr1.jpg)
![mr2](../doc/img/mr2.jpg)
```
1.流程详解
上面的流程是整个MapReduce最全工作流程，但是Shuffle过程只是从第7步开始到第16步结束，具体Shuffle过程详解，如下：
1）MapTask收集我们的map()方法输出的kv对，放到内存缓冲区中
2）从内存缓冲区不断溢出本地磁盘文件，可能会溢出多个文件
3）多个溢出文件会被合并成大的溢出文件
4）在溢出过程及合并的过程中，都要调用Partitioner进行分区和针对key进行排序
5）ReduceTask根据自己的分区号，去各个MapTask机器上取相应的结果分区数据
6）ReduceTask会取到同一个分区的来自不同MapTask的结果文件，ReduceTask会将这些文件再进行合并（归并排序）
7）合并成大文件后，Shuffle的过程也就结束了，后面进入ReduceTask的逻辑运算过程（从文件中取出一个一个的键值对Group，调用用户自定义的reduce()方法）
2．注意
Shuffle中的缓冲区大小会影响到MapReduce程序的执行效率，原则上说，缓冲区越大，磁盘io的次数越少，执行速度就越快。
缓冲区的大小可以通过参数调整，参数：io.sort.mb默认100M。
```
2.4.2 mr的shuffle机制
![mr1](../doc/img/mr1.jpg)
2.4.3 mr的patition分区
按照条件输出不同文件中（分区），分区这个概念影响着输出文件的个数，默认分区为HashPatitioner,用户没法控制分到哪个区
```
 /** Use {@link Object#hashCode()} to partition. */
  public int getPartition(K2 key, V2 value,
                          int numReduceTasks) {
    return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
  }
```
需要自定义分区
```
1)extends Partitioner,重写getPartition()方法
2）设置自定义partitioner
  job.setPartitionerClass(CustomPartition.class);
3）设置自定义partitioner的相应的reduceTask
 job.setNumReduceTasks(4);
分区总结：
（1)如果ReduceTask的数量>getPartition的结果数，则会多产生几个空的输出文件part-r-000xx;
 (2)如果1<ReduceTask的数量<getPartition的结果数，则有一部分分区数据无处安放，会exception
（3)如果ReduceTask的数量=1，则不管MapTask输出多少分区文件，最终结果都交给一个ReduceTask，最终也只会有一个输出文件part-r-000xx;
（4)分区号必须从零开始，逐一累加。
```
2.4.4 排序<br/>
排序是MapReduce框架中最重要的操作之一
mapReduce和ReduceTask均会对数据按照key进行排序。属于Hadoop默认行为。默认排序是字典顺序排序，且实现该排序的方法是快速排序。
![mr1](../doc/img/sort.jpg)
自定义排序需要实现WritableComparable，实现接口compareTo方法。
```
@Override
public int compareTo(FlowBean o) {

	int result;
		
	// 按照总流量大小，倒序排列
	if (conditon) {
		result = -1;
	}else if (condition) {
		result = 1;
	}else {
		result = 0;
	}

	return result;
}
```





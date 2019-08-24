### hive
#### 1,hive简介
1.1 Hive是基于Hadoop的一个数据仓库工具，可以将结构化的数据文件映射为一张表，并提供类SQL查询功能。
本质是：将HQL转化成MapReduce程序。
* 1）Hive处理的数据存储在HDFS<br/>
* 2）Hive分析数据底层的实现是MapReduce<br/>
* 3）执行程序运行在Yarn上<br/>

1.2.1 优点
* 1)操作接口采用类SQL语法，提供快速开发的能力（简单、容易上手）。
* 2)避免了去写MapReduce，减少开发人员的学习成本。
* 3)Hive的执行延迟比较高，因此Hive常用于数据分析，对实时性要求不高的场合。
* 4)Hive优势在于处理大数据，对于处理小数据没有优势，因为Hive的执行延迟比较高。
Hive支持用户自定义函数，用户可以根据自己的需求来实现自己的函数

1.2.2 缺点
* 1．Hive的HQL表达能力有限<br/>
（1）迭代式算法无法表达<br/>
（2）数据挖掘方面不擅长
* 2．Hive的效率比较低<br/>
（1）Hive自动生成的MapReduce作业，通常情况下不够智能化<br/>
（2）Hive调优比较困难，粒度较粗

1.3 Hive架构原理
```
1．用户接口：Client
CLI（hive shell）、JDBC/ODBC(java访问hive)、WEBUI（浏览器访问hive）
2．元数据：Metastore
元数据包括：表名、表所属的数据库（默认是default）、表的拥有者、列/分区字段、表的类型（是否是外部表）、表的数据所在目录等；
--默认存储在自带的derby数据库中，推荐使用MySQL存储Metastore--
3．Hadoop
使用HDFS进行存储，使用MapReduce进行计算。
4．驱动器：Driver
1）解析器（SQL Parser）：将SQL字符串转换成抽象语法树AST，这一步一般都用第三方工具库完成，比如antlr；对AST进行语法分析，比如表是否存在、字段是否存在、SQL语义是否有误。
2）编译器（Physical Plan）：将AST编译生成逻辑执行计划。
3）优化器（Query Optimizer）：对逻辑执行计划进行优化。
4）执行器（Execution）：把逻辑执行计划转换成可以运行的物理计划。对于Hive来说，就是MR/Spark。
```
1.4 Hive和数据库的比较<br/>
1.4.1 数据存储位置<br/>
数据存储在HDFS上,而数据库则可以将数据保存在块设备或者本地文件系统中。<br/>
1.4.2 数据更新<br/>
数据仓库的内容是读多写少的。因此，Hive中不建议对数据的改写，所有的数据都是在加载的时候确定好的。<br/>
1.4.3 索引<br/>
Hive没有索引，mapreduce有优势，数据库需要建立索引，整体上Hive不适合在线数据查询。mapreduce的延迟使hive执行有延迟，
高规模数据能体现优势。
#### 2,Hive安装
2.1 Hive安装与配置
```
1）把apache-hive-1.2.1-bin.tar.gz上传到linux的/opt/software目录下
2）解压apache-hive-1.2.1-bin.tar.gz到/opt/module/目录下面
tar -zxvf apache-hive-1.2.1-bin.tar.gz -C /opt/module/
3）修改apache-hive-1.2.1-bin.tar.gz的名称为hive
mv apache-hive-1.2.1-bin/ hive
4）修改/opt/module/hive/conf目录下的hive-env.sh.template名称为hive-env.sh
mv hive-env.sh.template hive-env.sh
5）配置hive-env.sh文件
a）配置HADOOP_HOME路径
export HADOOP_HOME=/opt/module/hadoop-2.7.2
b）配置HIVE_CONF_DIR路径
export HIVE_CONF_DIR=/opt/module/hive/conf
2．Hadoop集群配置
1）必须启动hdfs和yarn
sbin/start-dfs.sh
sbin/start-yarn.sh
2）在HDFS上创建/tmp和/user/hive/warehouse两个目录并修改他们的同组权限可写
bin/hadoop fs -mkdir /tmp
bin/hadoop fs -mkdir -p /user/hive/warehouse
bin/hadoop fs -chmod g+w /tmp
bin/hadoop fs -chmod g+w /user/hive/warehouse
```
2.2 将本地文件倒入Hive<br/>
将本地/opt/module/datas/student.txt这个目录下的数据导入到hive的student(id int, name string)表中。
```
1．数据准备
在/opt/module/datas这个目录下准备数据
1）在/opt/module/目录下创建datas
mkdir datas
2）在/opt/module/datas/目录下创建student.txt文件并添加数据
touch student.txt
vi student.txt
1001	zhangshan
1002	lishi
1003	zhaoliu
注意以tab键间隔。
2．Hive实际操作
1）启动hive
bin/hive
2）显示数据库
hive> show databases;
3）使用default数据库
hive> use default;
4）显示default数据库中的表
hive> show tables;
5）删除已创建的student表
hive> drop table student;
6）创建student表, 并声明文件分隔符’\t’
hive> create table student(id int, name string) ROW FORMAT DELIMITED FIELDS TERMINATED
 BY '\t';
7）加载/opt/module/datas/student.txt 文件到student数据库表中。
hive> load data local inpath '/opt/module/datas/student.txt' into table student;
8）Hive查询结果
hive> select * from student;
OK
1001	zhangshan
1002	lishi
1003	zhaoliu

再打开一个客户端窗口启动hive，会产生java.sql.SQLException异常。
原因是，Metastore默认存储在自带的derby数据库中，推荐使用MySQL存储Metastore;
```
2.3 安装Mysql<br/>
可以去参考我的mysql文章
2.4 Hive元数据配置到Mysql
```
1．在/opt/module/hive/conf目录下创建一个hive-site.xml
touch hive-site.xml
vi hive-site.xml
2．根据官方文档配置参数，拷贝数据到hive-site.xml文件中
https://cwiki.apache.org/confluence/display/Hive/AdminManual+MetastoreAdmin
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
	<property>
	  <name>javax.jdo.option.ConnectionURL</name>
	  <value>jdbc:mysql://centos101:3306/metastore?createDatabaseIfNotExist=true</value>
	  <description>JDBC connect string for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionDriverName</name>
	  <value>com.mysql.jdbc.Driver</value>
	  <description>Driver class name for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionUserName</name>
	  <value>root</value>
	  <description>username to use against metastore database</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionPassword</name>
	  <value>000000</value>
	  <description>password to use against metastore database</description>
	</property>
</configuration>
3．配置完毕后，如果启动hive异常，可以重新启动虚拟机。（重启后，别忘了启动hadoop集群）
```
2.4 Hive常见属性配置
```
1,Hive数据仓库位置配置
1）Default数据仓库的最原始位置是在hdfs上的：/user/hive/warehouse路径下
2）修改default数据仓库原始位置（将hive-default.xml.template如下配置信息拷贝到hive-site.xml文件中）。
<property>
<name>hive.metastore.warehouse.dir</name>
<value>/user/hive/warehouse</value>
<description>location of default database for the warehouse</description>
</property>
配置同组用户有执行权限
bin/hdfs dfs -chmod g+w /user/hive/warehouse
2,查询后信息显示配置
1）在hive-site.xml文件中添加如下配置信息，就可以实现显示当前数据库，以及查询表的头信息配置。
<property>
	<name>hive.cli.print.header</name>
	<value>true</value>
</property>

<property>
	<name>hive.cli.print.current.db</name>
	<value>true</value>
</property>
3,Hive运行日志信息配置
1．Hive的log默认存放在/tmp/atguigu/hive.log目录下（当前用户名下）
2．修改hive的log存放日志到/opt/module/hive/logs
1）修改/opt/module/hive/conf/hive-log4j.properties.template文件名称为hive-log4j.properties
mv hive-log4j.properties.template hive-log4j.properties
2）在hive-log4j.properties文件中修改log存放位置
hive.log.dir=/opt/module/hive/logs
4.参数配置方式
1．查看当前所有的配置信息
hive>set;
2．参数的配置三种方式
1）配置文件方式
默认配置文件：hive-default.xml 
用户自定义配置文件：hive-site.xml
注意：用户自定义配置会覆盖默认配置。另外，Hive也会读入Hadoop的配置，因为Hive是作为Hadoop的客户端启动的，Hive的配置会覆盖Hadoop的配置。配置文件的设定对本机启动的所有Hive进程都有效。
2）命令行参数方式
启动Hive时，可以在命令行添加-hiveconf param=value来设定参数。
bin/hive -hiveconf mapred.reduce.tasks=10;
注意：仅对本次hive启动有效
查看参数设置：
hive (default)> set mapred.reduce.tasks;
3）参数声明方式
可以在HQL中使用SET关键字设定参数
例如：
hive (default)> set mapred.reduce.tasks=100;
注意：仅对本次hive启动有效。
查看参数设置
hive (default)> set mapred.reduce.tasks;
```
#### 3,Hive数据库操作
3.1 创建数据库
```
create database db_hive;
create database if not exists db_hive;
数据库在HDFS上的默认存储路径是/user/hive/warehouse/*.db。
create database db_hive2 location '/db_hive2.db';
```
3.2 查询数据库
```
show databases;
desc database db_hive;
desc database extended db_hive;
use db_hive;
```
3.3 删除数据库
```
删除空数据库
drop database if exists db_hive2;
如果数据库不为空，可以采用cascade命令，强制删除
drop database db_hive;
```
3.4 创建表
```
CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name 
[(col_name data_type [COMMENT col_comment], ...)] 
[COMMENT table_comment] 
[PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)] 
[CLUSTERED BY (col_name, col_name, ...) 
[SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS] 
[ROW FORMAT row_format] 
[STORED AS file_format] 
[LOCATION hdfs_path]
```
* 1)CREATE TABLE 创建一个指定名字的表。
* 2）EXTERNAL关键字创建一个外部表，Hive创建内部表时，会将数据移动到数据仓库指向的路径；若创建外部表，仅记录数据所在的路径，不对数据的位置做任何改变。在删除表的时候，内部表的元数据和数据会被一起删除，而外部表只删除元数据，不删除数据。
* 3）COMMENT：为表和列添加注释。
* 4）PARTITIONED BY创建分区表
* 5）CLUSTERED BY创建分桶表 
* 6）SORTED BY不常用
* 7）ROW FORMAT <br/>
DELIMITED [FIELDS TERMINATED BY char] [COLLECTION ITEMS TERMINATED BY char]
[MAP KEYS TERMINATED BY char] [LINES TERMINATED BY char] 
|SERDE serde_name [WITH SERDEPROPERTIES (property_name=property_value, property_name=property_value, ...)]
如果没有指定ROW FORMAT 或者ROW FORMAT DELIMITED，将会使用自带的SerDe。
SerDe是Serialize/Deserilize的简称，目的是用于序列化和反序列化。
* 8）STORED AS指定存储文件类型
常用的存储文件类型：SEQUENCEFILE（二进制序列文件）、TEXTFILE（文本）、RCFILE（列式存储格式文件）
如果文件数据是纯文本，可以使用STORED AS TEXTFILE。如果数据需要压缩，使用 STORED AS SEQUENCEFILE。
* 9）LOCATION ：指定表在HDFS上的存储位置。
* 10）LIKE允许用户复制现有的表结构，但是不复制数据。
参考：
```
create external table if not exists default.dept(
deptno int,
dname string,
loc int
)
partitioned by (month string)
row format delimited fields terminated by '\t';

load data local inpath '/opt/module/datas/dept.txt' into table default.dept_partition partition(month='201709');
load data local inpath '/opt/module/datas/dept.txt' into table default.dept;
修改内部表student2为外部表
注意：('EXTERNAL'='TRUE')和('EXTERNAL'='FALSE')为固定写法，区分大小写！
alter table student2 set tblproperties('EXTERNAL'='TRUE');
```
3.5 修改表<br/>
* 1．语法<br/>
更新列
ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]
* 增加和替换列
ALTER TABLE table_name ADD|REPLACE COLUMNS (col_name data_type [COMMENT col_comment], ...) 
注：ADD是代表新增一字段，字段位置在所有列后面(partition列前)，REPLACE则是表示替换表中所有字段。<br/>
3.6 删除表
```
drop table dept_partition;
```
#### 4,DML数据操作
4.1 导入数据
```
1．语法
hive> load data [local] inpath '/opt/module/datas/student.txt' overwrite | into table student [partition (partcol1=val1,…)];
（1）load data:表示加载数据
（2）local:表示从本地加载数据到hive表；否则从HDFS加载数据到hive表
（3）inpath:表示加载数据的路径
（4）overwrite:表示覆盖表中已有数据，否则表示追加
（5）into table:表示加载到哪张表
（6）student:表示具体的表
（7）partition:表示上传到指定分区
```
4.2 导出数据
```
4.2.1 Insert导出
1．将查询的结果导出到本地
insert overwrite local directory '/opt/module/datas/export/student' select * from student;
2．将查询的结果格式化导出到本地
insert overwrite local directory '/opt/module/datas/export/student1' ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'             select * from student;
3．将查询的结果导出到HDFS上(没有local)
hiveinsert overwrite directory '/user/atguigu/student2'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' select * from student;
4.2.2 Hadoop命令导出到本地
dfs -get /user/hive/warehouse/student/month=201709/000000_0
/opt/module/datas/export/student3.txt;
4.2.3 Hive Shell 命令导出
基本语法：（hive -f/-e 执行语句或者脚本 > file）
bin/hive -e 'select * from default.student;' > /opt/module/datas/export/student4.txt;
4.2.4 Export导出到HDFS上
export table default.student to '/user/hive/warehouse/export/student';
4.2.5 Sqoop导出
后续课程专门讲。
4.3 清除表中数据（Truncate）
注意：Truncate只能删除管理表，不能删除外部表中数据
truncate table student;
```

#### 5,查询
查询的语句和mysql有点相似，这里只说不同的地方
```
注意，Hive要求DISTRIBUTE BY语句要写在SORT BY语句之前。
对于distribute by进行测试，一定要分配多reduce进行处理，否则无法看到distribute by的效果。
案例实操：
先按照部门编号分区，再按照员工编号降序排序。
hive (default)> set mapreduce.job.reduces=3;
hive (default)> insert overwrite local directory '/opt/module/datas/distribute-result' select * from emp distribute by deptno sort by empno desc;
当distribute by和sorts by字段相同时，可以使用cluster by方式。
分桶表数据存储
分区针对的是数据的存储路径；分桶针对的是数据文件。
对于非常大的数据集，有时用户需要使用的是一个具有代表性的查询结果而不是全部结果。Hive可以通过对表进行抽样来满足这个需求。
查询表stu_buck中的数据。
select * from stu_buck tablesample(bucket 1 out of 4 on id);
注：tablesample是抽样语句，语法：TABLESAMPLE(BUCKET x OUT OF y) 。
y必须是table总bucket数的倍数或者因子。hive根据y的大小，决定抽样的比例。例如，table总共分了4份，当y=2时，抽取(4/2=)2个bucket的数据，当y=8时，抽取(4/8=)1/2个bucket的数据。
x表示从哪个bucket开始抽取，如果需要取多个分区，以后的分区号为当前分区号加上y。例如，table总bucket数为4，tablesample(bucket 1 out of 2)，表示总共抽取（4/2=）2个bucket的数据，抽取第1(x)个和第3(x+y)个bucket的数据。
注意：x的值必须小于等于y的值，否则
```
几个常用的函数
```
select nvl(comm,-1) from emp;
select 
  dept_id,
  sum(case sex when '男' then 1 else 0 end) male_count,
  sum(case sex when '女' then 1 else 0 end) female_count
from 
  emp_sex
group by
  dept_id;
CONCAT(string A/col, string B/col…)：返回输入字符串连接后的结果，支持任意个输入字符串;
CONCAT_WS(separator, str1, str2,...)：它是一个特殊形式的 CONCAT()。
1．函数说明
EXPLODE(col)：将hive一列中复杂的array或者map结构拆分成多行。
LATERAL VIEW
用法：LATERAL VIEW udtf(expression) tableAlias AS columnAlias
解释：用于和split, explode等UDTF一起使用，它能够将一列数据拆成多行数据，在此基础上可以对拆分后的数据进行聚合。
select
    movie,
    category_name
from 
    movie_info lateral view explode(category) table_tmp as category_name;
    
窗口函数
1．相关函数说明
OVER()：指定分析函数工作的数据窗口大小，这个数据窗口大小可能会随着行的变而变化
CURRENT ROW：当前行
n PRECEDING：往前n行数据
n FOLLOWING：往后n行数据
UNBOUNDED：起点，UNBOUNDED PRECEDING 表示从前面的起点， UNBOUNDED FOLLOWING表示到后面的终点
LAG(col,n)：往前第n行数据
LEAD(col,n)：往后第n行数据
NTILE(n)：把有序分区中的行分发到指定数据的组中，各个组有编号，编号从1开始，对于每一行，NTILE返回此行所属的组的编号。注意：n必须为int类型
select name,orderdate,cost, 
sum(cost) over() as sample1,--所有行相加 
sum(cost) over(partition by name) as sample2,--按name分组，组内数据相加 
sum(cost) over(partition by name order by orderdate) as sample3,--按name分组，组内数据累加 
sum(cost) over(partition by name order by orderdate rows between UNBOUNDED PRECEDING and current row ) as sample4 ,--和sample3一样,由起点到当前行的聚合 
sum(cost) over(partition by name order by orderdate rows between 1 PRECEDING and current row) as sample5, --当前行和前面一行做聚合 
sum(cost) over(partition by name order by orderdate rows between 1 PRECEDING AND 1 FOLLOWING ) as sample6,--当前行和前边一行及后面一行 
sum(cost) over(partition by name order by orderdate rows between current row and UNBOUNDED FOLLOWING ) as sample7 --当前行及后面所有行 
from business;
（4）查看顾客上次的购买时间
select name,orderdate,cost, 
lag(orderdate,1,'1900-01-01') over(partition by name order by orderdate ) as time1, lag(orderdate,2) over (partition by name order by orderdate) as time2 
from business;
 Rank
1．函数说明
RANK() 排序相同时会重复，总数不会变
DENSE_RANK() 排序相同时会重复，总数会减少
ROW_NUMBER() 会根据顺序计算
select name,
subject,
score,
rank() over(partition by subject order by score desc) rp,
dense_rank() over(partition by subject order by score desc) drp,
row_number() over(partition by subject order by score desc) rmp
from score;
```
6,压缩和存储
在Hadoop配置好压缩参数后，Hiveshell端开启压缩，Map端压缩和Reduce端压缩
```
1．开启hive最终输出数据压缩功能
hive (default)>set hive.exec.compress.output=true;
2．开启mapreduce最终输出数据压缩
hive (default)>set mapreduce.output.fileoutputformat.compress=true;
3．设置mapreduce最终数据输出压缩方式
hive (default)> set mapreduce.output.fileoutputformat.compress.codec =
 org.apache.hadoop.io.compress.SnappyCodec;
4．设置mapreduce最终数据输出压缩为块压缩
hive (default)> set mapreduce.output.fileoutputformat.compress.type=BLOCK;
5．测试一下输出结果是否是压缩文件
hive (default)> insert overwrite local directory
'/opt/module/datas/distribute-result' select * from emp distribute by deptno sort by empno desc;
```
文件存储
Hive支持的存储数的格式有：TEXTFILE 、SEQUENCEFILE、ORC、PARQUET。
```
TEXTFILE和SEQUENCEFILE的存储格式都是基于行存储的；
ORC和PARQUET是基于列式存储的。
TextFile格式
默认格式，数据不做压缩，磁盘开销大，数据解析开销大。可结合Gzip、Bzip2使用，但使用Gzip这种方式，hive不会对数据进行切分，从而无法对数据进行并行操作
Parquet格式
Parquet是面向分析型业务的列式存储格式，由Twitter和Cloudera合作开发，2015年5月从Apache的孵化器里毕业成为Apache顶级项目。
Parquet文件是以二进制方式存储的，所以是不可以直接读取的，文件中包括该文件的数据和元数据，因此Parquet格式文件是自解析的。
通常情况下，在存储Parquet数据的时候会按照Block大小设置行组的大小，由于一般情况下每一个Mapper任务处理数据的最小单位是一个Block，这样可以把每一个行组由一个Mapper任务处理，增大任务执行并行度。
存储文件的压缩比总结：
ORC >  Parquet >  textFile
在实际的项目开发当中，hive表的数据存储格式一般选择：orc或parquet。压缩方式一般选择snappy，lzo。
```
#### 8,企业调优
8.1 fetch抓取
Hive中对某些情况的查询可以不必使用
MapReduce计算。例如：SELECT * FROM employees;
在hive-default.xml.template文件中hive.fetch.task.conversion默认是more，老版本hive默认是minimal，
该属性修改为more以后，在全局查找、字段查找、limit查找等都不走mapreduce。
```
<property>
    <name>hive.fetch.task.conversion</name>
    <value>more</value>
    <description>
      Expects one of [none, minimal, more].
      Some select queries can be converted to single FETCH task minimizing latency.
      Currently the query should be single sourced not having any subquery and should not have
      any aggregations or distincts (which incurs RS), lateral views and joins.
      0. none : disable hive.fetch.task.conversion
      1. minimal : SELECT STAR, FILTER on partition columns, LIMIT only
      2. more  : SELECT, FILTER, LIMIT only (support TABLESAMPLE and virtual columns)
    </description>
  </property>
```
8.2 本地模式<br/>
Hive可以通过本地模式在单台机器上处理所有的任务。对于小数据集，执行时间可以明显被缩短。
```
set hive.exec.mode.local.auto=true;  //开启本地mr
//设置local mr的最大输入数据量，当输入数据量小于这个值时采用local  mr的方式，默认为134217728，即128M
set hive.exec.mode.local.auto.inputbytes.max=50000000;
//设置local mr的最大输入文件个数，当输入文件个数小于这个值时采用local mr的方式，默认为4
set hive.exec.mode.local.auto.input.files.max=10;
```
8.3 JVM重用
JVM重用可以使得JVM实例在同一个job中重新使用N次。缺点是job中ReduceTask消耗时间多的任务会占用插槽
```
N的值可以在Hadoop的mapred-site.xml文件中进行配置。通常在10-20之间，具体多少需要根据具体业务场景测试得出。
<property>
  <name>mapreduce.job.jvm.numtasks</name>
  <value>10</value>
  <description>How many tasks to run per jvm. If set to -1, there is
  no limit. 
  </description>
</property>
```
8.4 严格模式<br/>
1,对于分区表，除非where语句中含有分区字段过滤条件来限制范围，否则不允许执行。<br/>
2,对于使用了order by语句的查询，要求必须使用limit语句<br/>
3,限制笛卡尔积的查询。
```
<property>
    <name>hive.mapred.mode</name>
    <value>strict</value>
    <description>
      The mode in which the Hive operations are being performed. 
      In strict mode, some risky queries are not allowed to run. They include:
        Cartesian Product.
        No partition being picked up for a query.
        Comparing bigints and strings.
        Comparing bigints and doubles.
        Orderby without limit.
</description>
</property>
```
8.5 并行执行
```
通过设置参数hive.exec.parallel值为true，就可以开启并发执行。不过，在共享集群中，需要注意下，如果job中并行阶段增多，那么集群利用率就会增加。
set hive.exec.parallel=true;              //打开任务并行执行
set hive.exec.parallel.thread.number=16;  //同一个sql允许最大并行度，默认为8。
```

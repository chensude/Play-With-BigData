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


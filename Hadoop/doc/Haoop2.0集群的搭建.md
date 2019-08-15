Hadoop2.0版本比1.0版本增加了yarn，yarn只负责资源调度，mapreduce只负责运算。
## 1，虚拟机准备
```
1）准备3台客户机（关闭防火墙、静态ip、主机名称）
2）安装JDK
3）配置环境变量
4）安装Hadoop
5）配置环境变量
6）配置集群
7）单点启动
8）配置ssh
9）群起并测试集群
```
## 2,安装JDK
```
tar -zxvf jdk-8u144-linux-x64.tar.gz -C /opt/module/
```
## 3，配置JDK环境
```
[csd@centos101 opt]sudo vi /etc/profile
#JAVA_HOME
export JAVA_HOME=/opt/module/jdk1.8.0_144
export PATH=$PATH:$JAVA_HOME/bin
:wq保存退出
[csd@centos101 opt]source /etc/profile
```
## 4，安装Hadoop
```
tar -zxvf hadoop-2.7.2.tar.gz -C /opt/module/
```
### 4.1 添加Hadoop环境变量
```
##HADOOP_HOME
export HADOOP_HOME=/opt/module/hadoop-2.7.2
export PATH=$PATH:$HADOOP_HOME/bin
export PATH=$PATH:$HADOOP_HOME/sbin
```
### 4.2 编写集群分发脚步xsync
```
（1）基本语法
rsync    -rvl       $pdir/$fname    $user@centos101:$pdir/$fname          
命令   选项参数   要拷贝的文件路径/名称    目的用户@主机:目的路径/名称
选项参数说明. -r递归 -v 显示复制过程  -l拷贝符号连接
（2）脚本实现
    在家目录/bin下，创建脚本。xsync，csd用户可以在任何地方执行。(如果不能调用，可以防止/usr/local/)
    [csd@centos101 bin]$ vi xsync
    在该文件中编写如下代码:
!/bin/bash
pcount=$#
if((pcount==0)); then
echo no args;
exit;
fi
#2 获取文件名称
p1=$1
fname=`basename $p1`
echo fname=$fname
#3 获取上级目录到绝对路径
pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir
#4 获取当前用户名称
user=`whoami`
#5 循环
for((host=102; host<104; host++)); do
        echo ------------------- hadoop$host --------------
        rsync -rvl $pdir/$fname $user@centos$host:$pdir
done
```
#### 4.3 集群的规划

|----|centos101|centos102|centos103 |
| --- | --- | --- | --- |
|HDFS|NameNode DataNode|DataNode|SecondaryNameNode DataNode|
|YARN| NodeManager| ResourceManager NodeManager| NodeManager|
#### 4.4 配置集群
##### 4.4.1 配置core-site.xml
```
在该文件中编写如下配置
<!-- 指定HDFS中NameNode的地址 -->
<property>
		<name>fs.defaultFS</name>
      <value>hdfs://centos101:9000</value>
</property>

<!-- 指定Hadoop运行时产生文件的存储目录 -->
<property>
		<name>hadoop.tmp.dir</name>
		<value>/opt/module/hadoop-2.7.2/data/tmp</value>
</property>
```
##### 4.4.2 配置hadoop-env.sh
```
export JAVA_HOME=/opt/module/jdk1.8.0_144
```
##### 4.4.3配置hdfs-site.xml
```
<property>
		<name>dfs.replication</name>
		<value>3</value>
</property>

<!-- 指定Hadoop辅助名称节点主机配置 -->
<property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>centos103:50090</value>
</property>
```
##### 4.4.4 配置YARN文件

###### 4.4.4.1 配置yarn-env.sh
```
export JAVA_HOME=/opt/module/jdk1.8.0_144
```
###### 4.4.4.2 配置yarn-site.xml
```
在该文件中增加如下配置
<!-- Reducer获取数据的方式 -->
<property>
		<name>yarn.nodemanager.aux-services</name>
		<value>mapreduce_shuffle</value>
</property>

<!-- 指定YARN的ResourceManager的地址 -->
<property>
		<name>yarn.resourcemanager.hostname</name>
		<value>centos102</value>
</property>
```
###### 4.4.4.3 配置mapred-env.sh
```
export JAVA_HOME=/opt/module/jdk1.8.0_144
```
###### 4.4.4.4 配置mapred-site.xml
```
cp mapred-site.xml.template mapred-site.xml
在该文件中增加如下配置
<!-- 指定MR运行在Yarn上 -->
<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
</property>
```
##### 4.4.5 分发配置
```
xsync /opt/module/hadoop-2.7.2/
```
#### 4.4 SSH无密登录
```
首先要配置centos101到centos102、centos103
在centos01上生产一对钥匙
  ssh-keygen -t rsa
将公钥拷贝到其他节点，包括自己
  ssh-copy-id centos101
  ssh-copy-id centos102
  ssh-copy-id centos103
centos102 也要同样的配置
 ssh-keygen -t rsa
将公钥拷贝到其他节点，包括自己
  ssh-copy-id centos101
  ssh-copy-id centos102
  ssh-copy-id centos103
```
#### 4.5 群起集群
```
在slaves文件下加入如下内容
centos101
centos102
centos103
同步所有节点配置
xsync slaves
启动集群
（1）如果集群是第一次启动，需要格式化NameNode:
    bin/hdfs namenode -format
（2）启动HDFS:
    sbin/start-dfs.sh
 (3)启动YARN
    sbin/start-yarn.sh
注意：NameNode和ResourceManger如果不是同一台机器，不能在NameNode上启动 YARN，应该在ResouceManager所在的机器上启动YARN。
（4）Web端查看SecondaryNameNode
 （a）浏览器中输入：http://centos103:50090/status.html
停止集群
（1）整体启动/停止HDFS
		start-dfs.sh   /  stop-dfs.sh
（2）整体启动/停止YARN
		start-yarn.sh  /  stop-yarn.sh  
```

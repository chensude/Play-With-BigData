#### 1,shell概述
什么是shell？shell是一个命令行解释器。
#### 2,shell解释器
1）Linux提供的Shell解析器有：
```
/bin/sh
/bin/bash
/sbin/nologin
/bin/dash
/bin/tcsh
/bin/csh
```
2）Centos默认的解析器是bash
```
echo $SHELL
```
#### 3,shell基本语法
##### 3.1 脚本的变量
* 常用的系统变量
```
$HOME、$PWD、$SHELL、$USER等
```
* 自定义变量
```
1.基本语法
  1）定义变量：变量=值
  2）撤销变量：unset 变量
  3）声明静态变量：readonly变量，注意：不能unset
  4）变量的值如果有空格，需要使用双引号或单引号括起来
  5）变量默认类型都是字符串类型，无法直接进行数值运算
2．变量定义规则
  1）变量名称可以由字母、数字和下划线组成，但是不能以数字开头，环境变量名建议大写。
  2）等号两侧不能有空格
  3）在bash中，变量默认类型都是字符串类型，无法直接进行数值运算。
  4）变量的值如果有空格，需要使用双引号或单引号括起来。 
```
* 特殊变量 $n
```
1)$n
功能描述：n为数字，$0代表该脚本名称，$1-$9代表第一到第九个参数，
十以上的参数，十以上的参数需要用大括号包含，如${10}
2)$# 
功能描述：获取所有输入参数个数，常用于循环。
3)$*、$@
$* 功能描述：这个变量代表命令行中所有的参数，$*把所有的参数看成一个整体
$@ 功能描述：这个变量也代表命令行中所有的参数，不过$@把每个参数区分对待
4)$？
最后一次执行的命令的返回状态。如果这个变量的值为0，证明上一个命令正确执行；如果这个变量的值为非0
```
##### 3.2 运算符
```
1．基本语法
（1）“$((运算式))”或“$[运算式]”
（2）expr  + , - , \*,  /,  %    加，减，乘，除，取余
注意：expr运算符间要有空格
```
##### 3.3 条件判断
```[ condition ]（注意condition前后要有空格）
   注意：条件非空即为true，[ atguigu ]返回true，[] 返回false。
```
##### 3.4 流程控制
```
1．if基本语法
if [ 条件判断式 ];then 
  程序 
fi 
或者 
if [ 条件判断式 ] 
  then 
    程序 
fi
注意事项：
（1）[ 条件判断式 ]，中括号和条件判断式之间必须有空格
（2）if后要有空格

2．case基本语法
case $变量名 in 
  "值1"） 
    如果变量的值等于值1，则执行程序1 
    ;; 
  "值2"） 
    如果变量的值等于值2，则执行程序2 
    ;; 
  …省略其他分支… 
  *） 
    如果变量的值都不是以上的值，则执行此程序 
    ;; 
esac
注意事项：
1)case行尾必须为单词“in”，每一个模式匹配必须以右括号“）”结束。
2)双分号“;;”表示命令序列结束，相当于java中的break。
最后的“*）”表示默认模式，相当于java中的default。

3.for循环
1．基本语法1
	for (( 初始值;循环控制条件;变量变化 )) 
  do 
    程序 
  done
  
4．while基本语法
while [ 条件判断式 ] 
  do 
    程序
  done
```
##### 3.5 read读取控制台输入
```
1．基本语法
	read(选项)(参数)
	选项：
-p：指定读取值时的提示符；
-t：指定读取值时等待的时间（秒）。
参数
	变量：指定读取值的变量名
#!/bin/bash

read -t 7 -p "Enter your name in 7 seconds " NAME
echo $NAME
```
#### 3.6 函数
##### 3.6.1 系统函数
```
1．basename基本语法
basename [string / pathname] [suffix]  	（功能描述：basename命令会删掉所有的前缀包括最后一个（‘/’）字符，然后将字符串显示出来。
选项：
suffix为后缀，如果suffix被指定了，basename会将pathname或string中的suffix去掉。
2.dirname基本语法
dirname 文件绝对路径	
功能描述：从给定的包含绝对路径的文件名中去除文件名（非目录的部分），然后返回剩下的路径（目录的部分）
```
##### 3.6.2 自定义函数 
```
1．基本语法
[ function ] funname[()]
{
	Action;
	[return int;]
}
functionName
函数返回值，只能通过$?系统变量获得，可以显示加：return返回，如果不加，将以最后一条命令运行结果，作为返回值。return后跟数值n(0-255)
```
#### 4,shell工具
##### cut
```
1.基本用法
cut [选项参数]  filename
说明：默认分隔符是制表符
2.选项参数说明
-f : 列号，提取第几列
-d : 分隔符
cat cut.txt | grep "guan" | cut -d " " -f 1
``` 
##### sed
```
sed是一种流编辑器，它一次处理一行内容。
文件内容并没有改变，把当前处理的行存储在临时缓冲区中，称为“模式空间”.
1.基本用法
sed [选项参数]  ‘command’  filename
2.选项参数说明
-e : 直接在指令列模式上进行sed的动作编辑。
3.命令功能描述
a : 新增，a的后面可以接字符串，在下一行出现
d : 删除
s : 查找并替换
1)将“mei nv”这个单词插入到sed.txt第二行下，打印。
sed '2a mei nv' sed.txt 
注意：文件并没有改变
2)删除sed.txt文件所有包含wo的行
sed '/wo/d' sed.txt
3)将sed.txt文件中wo替换为ni
sed 's/wo/ni/g' sed.txt 
注意：‘g’表示global，全部替换
将sed.txt文件中的第二行删除并将wo替换为ni
sed -e '2d' -e 's/wo/ni/g' sed.txt 
```
##### awk
```
一个强大的文本分析工具，把文件逐行的读入，以空格为默认分隔符将每行切片，切开的部分再进行分析处理。
1.基本用法
awk [选项参数] ‘pattern1{action1}  pattern2{action2}...’ filename
pattern：表示AWK在数据中查找的内容，就是匹配模式
action：在找到匹配内容时所执行的一系列命令
2.选项参数说明
-F:	指定输入文件折分隔符
-v:	赋值一个用户定义变量
awk -v i=1 -F: '{print $3+i}' passwd
awk的内置变量
FILENAME：文件名
NR：	已读的记录数
NF：	浏览记录的域的个数（切割后，列的个数）
awk -F: '{print "filename:"  FILENAME ", linenumber:" NR  ",columns:" NF}' passwd 
查询sed.txt中空行所在的行号
awk '/^$/{print NR}' sed.txt 
```
##### sort
```
sort命令是在Linux里非常有用，它将文件进行排序，并将排序结果标准输出。
1.基本语法
sort(选项)(参数)
表1-57
选项	说明
-n:依照数值的大小排序
-r:以相反的顺序来排序
-t:设置排序时所用的分隔字符
-k:指定需要排序的列
参数:指定待排序的文件列表
按照“：”分割后的第三列倒序排序。
sort -t : -nrk 3  sort.sh 
```
##### 5,企业面试真题
```
问题1：使用Linux命令查询file1中空行所在的行号
awk '/^$/{print NR}' sed.txt 
问题2：有文件chengji.txt内容如下:
张三 40
李四 50
王五 60
使用Linux命令计算第二列的和并输出
cat chengji.txt | awk -F " " '{sum+=$2} END{print sum}'
问题3：Shell脚本里如何检查一个文件是否存在？如果不存在该如何处理？
#!/bin/bash
if [ -f file.txt ]; then
   echo "文件存在!"
else
   echo "文件不存在!"
fi
问题4：用shell写一个脚本，对文本中无序的一列数字排序
sort -n test.txt|awk '{a+=$0;print $0}END{print "SUM="a}'
问题5：请用shell脚本写出查找当前文件夹（/home）下所有的文本文件内容中包含有字符”shen”的文件名称
grep -r "shen" /home | cut -d ":" -f 1
```
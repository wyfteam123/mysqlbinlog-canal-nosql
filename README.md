
<img src="https://github.com/wyfteam123/mysqlbinlog-canal-nosql/blob/master/readmesource/binlog.png"/><br>
## 原理概述
### binlog:<br>
  mysql在运行过程中执行的DML（增删改）操作都会以二进制形式记录在binlog中<br>
### canal server：<br>
  canal server模拟作为从数据库（slave）向主数据库（master）发送dump命令获取binlog数据<br>
### canal client(provider):<br>
  从canal server拿到数据解析成json格式，存储到文件或者推送至mq缓冲，或者直接导入redis<br>
### consummer:<br>
  如果是推送至mq或者文件的，则需要一个消费者来处理数据并更新至nosql服务器，用自己擅长的语言来编写消费者端即可，推荐python<br>


## 实践

### 开启mysql binlog功能
以5.7版本为例，找到/etc/mysql/mysql.conf.d/mysqld.cnf
```
[mysqld]
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
#log-error      = /var/log/mysql/error.log
# By default we only accept connections from localhost
#bind-address   = 127.0.0.1
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

# 开启binlog
log-bin=mysql-bin 
binlog-format=ROW
server_id=1	
# 开启binlog
```
配置完毕重启mysql服务，重启完成后，查看是否已经成功开启
```
show variables like 'log_%';
```
<img src="https://github.com/wyfteam123/mysqlbinlog-canal-nosql/blob/master/readmesource/showvariableslog.png"/>

### canal-server服务
<a href="https://github.com/alibaba/canal/releases">点击下载ali-canal<a/>
配置canal-server，修改\canal.deployer-1.0.22\conf\example\instance.properties
```
## mysql serverId 务必与master数据库不同
canal.instance.mysql.slaveId = 1234

需要追踪复制的master数据库信息
canal.instance.master.address = 127.0.0.1:3307
canal.instance.master.journal.name = 
canal.instance.master.position = 
canal.instance.master.timestamp = 

#canal.instance.standby.address = 
#canal.instance.standby.journal.name =
#canal.instance.standby.position = 
#canal.instance.standby.timestamp = 

username/password
canal.instance.dbUsername = root
canal.instance.dbPassword = root
canal.instance.defaultDatabaseName = test
canal.instance.connectionCharset = UTF-8
  ```
启动项目<br>
\canal.deployer-1.0.22\bin\

### canal-client服务<br>
https://github.com/wyfteam123/mysqlbinlog-canal-nosql/tree/master/canal-client<br>
修改对应配置，使用maven打包启动项目。<br>

### mq-consummer服务
java实现<br>
https://github.com/wyfteam123/mysqlbinlog-canal-nosql/tree/master/sync-server/java_sync_nosql<br>
或<br>
python实现<br>
https://github.com/wyfteam123/mysqlbinlog-canal-nosql/tree/master/sync-server/python_sync_nosql<br>

### rabbit/redis/mysql
推荐docker容器安装


binlog:
mysql在运行过程中执行的DML（增删改）操作都会以二进制形式记录在binlog中
canal server：
canal server作为从数据库（slave）向主数据库发送dump命令获取binlog数据
canal client(provider):
从canal server拿到数据解析成json格式，存储到文件或者推送至mq缓冲，或者直接导入redis
consummer:
如果是推送至mq或者文件的，则需要一个消费者来处理数据并更新至nosql服务器，用自己擅长的语言来编写消费者端即可，推荐python

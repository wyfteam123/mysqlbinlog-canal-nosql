#!/usr/bin/env python3
# -*- coding: utf-8 -*-


# rabbitmq connect
rabbitmq_host = '127.0.0.1'
rabbitmq_port = 5672
# rabbitmq 账号密码（远程访问禁止使用 guest账号）
rabbitmq_user = 'test'
rabbitmq_pass = '123456'
# 与canal里面配置的队列名一样
rabbitmq_queue_name = 'canal_binlog_data'


binlog_dir = '/Users/liukelin/Desktop/canal-otter-mycat-cobar/canal_object/data' # 文件路径
binlog_file = '' # 开始文件
binlog_prefix = 'binlog_' # 文件前缀

# 同步目标 redis/mongo
sync_db = 'redis'


# redis
redis_host = '127.0.0.1'
redis_port = 6379
# redis_password = ''

# 指定保存到redis的key，指定每个db-table的 key
# 这里的demo是将db表数据映射到 redis hash 结构，key=db:table:primary_id
redis_cache_map = {
    # db
	'test':{
	        # table
			'user':'id'
		  }
}


# mongoDB
mongo_host = '127.0.0.1'
mongo_port = 27017
# 指定表唯一主键（根据此值更新）
mongo_cache_map = {
    # db
    'test':{
            # table1
            'users':'uid',
            # table2
            'users_info':'tid'
          },
    # db2
    'test2':{
            # table
            'users':'tid'
          }
}










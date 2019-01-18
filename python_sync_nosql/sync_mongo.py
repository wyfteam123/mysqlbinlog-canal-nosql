#!/usr/bin/env python3
# -*- coding: utf-8 -*-


'''

 消费 数据 写入到mongodb

'''
import os
import config
import json
import pymongo
from pymongo import MongoClient

client=False
def Conn():
    client = MongoClient(config.mongo_host, config.mongo_port)
    print(client)
    return client

'''
 ·将数据写入到 mongo
  这里的demo是将表数据映射到 mongodb 结构
  db    => db
  table => 集合
  column=> coll

  body={
        "binlog": "mysql-bin.000009:1235",
        "db": "test",
        "table": "users",
        "eventType": "UPDATE",
        "before": {
            "uid": "8",
            "username": "duobao153713223"
        },
        "after": {
            "uid": "8",
            "username": "duobao153713223"
        },
        "time": "2016-08-22 17:47:25"
    }
'''
def set_data(body, client=None):

    if not body or body=='':
        return False

    try:
        # 如果是bytes
        body = str(body, encoding = "utf-8")
    except:
        pass
    # 这个位置粗略的处理了下单引号json 实际可以再做处理
    # 有可能是单引号json
    body = body.replace("'", "\"")

    data = json.loads(body)
    if isinstance(data, (dict)) == False:
        return False

    if 'eventType' in data and 'after' in data and 'db' in data and 'table' in data:

        if not client:
            client = Conn()

        mongo_cache_map = config.mongo_cache_map
        db = data.get('db')
        table = data.get('table')

        # 指定数据库(db)
        # dbc = client.(db)
        dbc = client.get_database(db)
        # 指定集合(表)
        # posts = dbc.(table)
        posts = dbc.get_collection(table)

        if not posts:
            return False

        if data.get('eventType') in ['UPDATE', 'INSERT', 'DELETE'] and isinstance(data.get('after'), (dict)):

            coll = '';  # 唯一字段名
            pid = 0;    # 值
            # 获取更新条件唯一值
            if mongo_cache_map.get(db) and mongo_cache_map.get(db).get(table):
                coll = mongo_cache_map.get(db).get(table)
                pid = data.get('after').get(coll)

            else:
                return False

            if data.get('eventType')=='INSERT':
                posts.insert( data.get('after') )
                # posts.save(data.get('after'))

            elif data.get('eventType')=='UPDATE':
                posts.update( { coll:pid } , {'$set': data.get('after') } )

            elif data.get('eventType')=='DELETE':
                posts.remove( { coll:pid } )

            # try:
            #   redisConn.hmset(key, data['after'])
            # except:
            #   conn_redis()
            #   redisConn.hmset(key, data['after'])
            return True

    return False

'''
body={
        "binlog": "mysql-bin.000009:1235",
        "db": "test",
        "table": "users",
        "eventType": "UPDATE",
        "before": {
            "uid": "8",
            "username": "duobao153713223"
        },
        "after": {
            "uid": "8",
            "username": "duobao153713223"
        },
        "time": "2016-08-22 17:47:25"
    }
body = json.dumps(body)
set_data(body)
'''







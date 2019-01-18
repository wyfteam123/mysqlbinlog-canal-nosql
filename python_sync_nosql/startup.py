#!/usr/bin/env python3
# -*- coding: utf-8 -*-


'''
  将数据写入到 nosql
  startup

'''
import os
import config
import pika
import queue_rabbitmq


print(' [*] Waiting for messages. To exit press CTRL+C')
queue_rabbitmq.consumer_data()

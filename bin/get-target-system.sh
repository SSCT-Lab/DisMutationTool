#!/bin/bash

# 定义下载链接
urls=(
    "https://archive.apache.org/dist/zookeeper/zookeeper-3.5.8/apache-zookeeper-3.5.8.tar.gz"
    "https://archive.apache.org/dist/cassandra/3.11.6/apache-cassandra-3.11.6-src.tar.gz"
    "https://archive.apache.org/dist/hbase/2.5.8/hbase-2.5.8-src.tar.gz"
)

# 遍历 URL 数组
for url in "${urls[@]}"; do
    # 获取文件名
    filename=$(basename "$url")

    # 下载文件
    echo "正在下载 $filename..."
    wget "$url"

    # 解压文件
    echo "正在解压 $filename..."
    tar -xzvf "$filename"

    # 删除压缩文件（如果不需要保留压缩文件的话）
     echo "正在删除压缩文件 $filename..."
     rm "$filename"
done

echo "所有文件下载并解压完毕！"

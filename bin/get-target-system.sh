#!/bin/bash

# urls
urls=(
    "https://archive.apache.org/dist/zookeeper/zookeeper-3.5.8/apache-zookeeper-3.5.8.tar.gz"
    "https://archive.apache.org/dist/cassandra/3.11.6/apache-cassandra-3.11.6-src.tar.gz"
    "https://archive.apache.org/dist/hbase/2.5.8/hbase-2.5.8-src.tar.gz"
    "https://dist.apache.org/repos/dist/release/rocketmq/5.2.0/rocketmq-all-5.2.0-source-release.zip"
    "https://archive.apache.org/dist/skywalking/10.0.0/apache-skywalking-apm-10.0.0-src.tgz"
    "https://archive.apache.org/dist/kafka/3.6.0/kafka-3.6.0-src.tgz"
    "https://archive.apache.org/dist/kafka/2.4.0/kafka-2.4.0-src.tgz"
    "https://dlcdn.apache.org/hadoop/common/hadoop-3.4.0/hadoop-3.4.0-src.tar.gz"
)

filenames=(
    "apache-zookeeper-3.5.8.tar.gz"
    "apache-cassandra-3.11.6-src.tar.gz"
    "hbase-2.5.8-src.tar.gz"
    "rocketmq-all-5.2.0-source-release.zip"
    "apache-skywalking-apm-10.0.0-src.tgz"
    "kafka-3.6.0-src.tgz"
    "kafka-2.4.0-src.tgz"
)

# folders name
folders=(
    "apache-zookeeper-3.5.8"
    "apache-cassandra-3.11.6-src"
    "hbase-2.5.8"
    "rocketmq-all-5.2.0-source-release"
    "apache-skywalking-apm-10.0.0"
    "kafka-3.6.0-src"
    "kafka-2.4.0-src"
    "hadoop-3.4.0-src"
)

for index in "${!urls[@]}"; do
    url="${urls[$index]}"
    filename="${filenames[$index]}"
    folder="${folders[$index]}"

    if [ -d "$folder" ]; then
        echo "Folder $folder already exists. Skipping download and decompression..."
    else

        echo "Downloading $filename..."
        wget "$url"


        extension="${filename##*.}"


        echo "Decompressing $filename..."
        if [ "$extension" == "gz" ]; then
            tar -xzvf "$filename"
        elif [ "$extension" == "zip" ]; then
            unzip "$filename"
            elif [ "$extension" == "tgz" ]; then
                        tar -xzvf "$filename"
        else
            echo "Unknown file extension: $extension. Skipping decompression..."
        fi


        echo "Deleting $filename..."
        rm "$filename"
    fi
done

echo "All files are downloaded and decompressed!"

#!/bin/bash

# Write download urls
urls=(
    "https://archive.apache.org/dist/zookeeper/zookeeper-3.5.8/apache-zookeeper-3.5.8.tar.gz"
    "https://archive.apache.org/dist/cassandra/3.11.6/apache-cassandra-3.11.6-src.tar.gz"
    "https://archive.apache.org/dist/hbase/2.5.8/hbase-2.5.8-src.tar.gz"
)

for url in "${urls[@]}"; do
    # Get filename
    filename=$(basename "$url")

    # Download
    echo "Download $filename..."
    wget "$url"

    # Decompress
    echo "Decompress $filename..."
    tar -xzvf "$filename"

    # Delete（If u don't need it）
     echo "Delete $filename..."
     rm "$filename"
done

echo "All files are downloaded and decompressed!"

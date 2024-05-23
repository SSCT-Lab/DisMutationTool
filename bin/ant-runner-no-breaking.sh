#!/bin/bash

kill_ant_processes() {
  local target_dir=$1
  # 查找访问指定目录的mvn进程的PID
  local pids=$(lsof +D "$target_dir" | grep 'mvn' | awk '{print $2}')

  # 如果找到了进程，就中止它们
  if [ -n "$pids" ]; then
    echo "正在中止以下mvn进程: $pids"
    kill -9 $pids
  else
    echo "没有找到访问 $target_dir 的mvn进程。"
  fi
}

# 检查参数数量是否正确
if [ $# -lt 2 ]; then
    echo "用法: $0 <输出重定向文件绝对路径> <项目路径> [跳过的测试类名称]"
    exit 1
fi

# 获取参数
output_file="$1"
ant_path="$2"
ant_args="$3" #-Dtest.runners=7

# 设置超时时间
timeout=10  # 10 分钟

touch "$output_file"

# 切换到 ant 路径
cd "$ant_path" || exit 1

# 开始执行 ant clean test，并将输出重定向到临时文件
if [ -z "$ant_args" ]; then
    ant clean test "$ant_args" >> "$output_file" 2>&1 &
else
    ant clean test >> "$output_file" 2>&1 &
fi

# 获取 mvn 进程的 PID
ant_pid=$!

# 初始化最后一次修改时间
last_mod_time=$(stat -c %Y "$output_file")
max_inactive_time=$((10 * 60)) # 10 分钟的秒数

# 每 3 秒检查一次文件更新时间
while kill -0 $ant_pid 2>/dev/null; do
    sleep 3

    # 获取当前文件的最后修改时间
    current_mod_time=$(stat -c %Y "$output_file")

    # 检查文件是否更新
    if [ "$current_mod_time" -ne "$last_mod_time" ]; then
        last_mod_time=$current_mod_time
    fi

    # 计算自上次更新以来经过的时间
    inactive_time=$(( $(date +%s) - last_mod_time ))

    # 如果文件超过 10 分钟没有更新，杀死进程
    if [ "$inactive_time" -ge "$max_inactive_time" ]; then
        echo "输出文件 $output_file 在 10 分钟内没有更新，终止进程 $ant_pid" >> "$output_file"
        kill -9 $ant_pid
        exit 1
    fi
done



exit 0

#!/bin/bash

kill_mvn_processes() {
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
    echo "用法: $0 <输出重定向文件名称> <mvn路径> [跳过的测试类名称]"
    exit 1
fi

# 获取参数
output_file="$1"
mvn_path="$2"
skip_test_class="$3"

# 切换到 mvn 路径
cd "$mvn_path" || exit 1

# 拼接输出重定向文件路径
output_dir="/home/zdc/code/ideaRemote/distributed-mutation-tool/testOutputs"
project_dir="/home/zdc/code/distributedSystems/zk/apache-zookeeper-3.5.8/zookeeper-server"
output_path="$output_dir/$output_file"

# 创建输出目录
mkdir -p "$output_dir"

# 开始执行 mvn clean test，并将输出重定向到临时文件
if [ -z "$skip_test_class" ]; then
    mvn clean test > "$output_path" &
else
    mvn clean test -Dtest=!$skip_test_class > "$output_path" &
fi

# 获取 mvn 进程的 PID
mvn_pid=$!

# 设置超时时间
timeout=600  # 10 分钟

# 持续监控输出文件和 mvn 进程
while :
do
    if grep -q "<<< FAILURE!" "$output_path"; then
        echo "测试失败，请查看日志文件: $output_path"
        kill "$mvn_pid"  # 结束 mvn 进程
        sleep 30
        exit 1
    elif grep -q "BUILD FAILURE" "$output_path"; then
        echo "构建失败，请查看日志文件: $output_path"
        kill "$mvn_pid"  # 结束 mvn 进程
        sleep 30
        exit 1
    elif grep -q "BUILD SUCCESS" "$output_path"; then
        echo "构建成功"
        break
    elif [ "$(find "$output_path" -mmin +10)" ]; then
        echo "输出文件长时间未更新，结束 mvn 进程"
        kill "$mvn_pid"  # 结束 mvn 进程
        sleep 3
        kill_mvn_processes "$project_dir"  # 结束所有访问项目目录的 mvn 进程
        sleep 30
        exit 1
    elif [ "$(ps -p "$mvn_pid" -o etime= | grep -o '[0-9]*:[0-9]*:[0-9]*')" ]; then
        # 检查 mvn 进程的运行时间
        elapsed_time=$(ps -p "$mvn_pid" -o etime= | grep -o '[0-9]*:[0-9]*:[0-9]*')
        seconds=$(date -d "1970-01-01 $elapsed_time" +%s)
        if [ "$seconds" -gt "$timeout" ]; then
            echo "mvn 进程已超时，结束"
            kill "$mvn_pid"  # 结束 mvn 进程
            sleep 30
            exit 1
        fi
    fi
    sleep 3  # 休眠 3 秒后再次检查输出文件和 mvn 进程
done

echo "测试已完成，结果已经重定向到 $output_path"
exit 0

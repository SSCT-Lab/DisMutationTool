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
    echo "用法: $0 <输出重定向文件绝对路径> <项目路径> [跳过的测试类名称]"
    exit 1
fi

# 获取参数
output_file="$1"
mvn_path="$2"
mvn_args="$3"

# 设置超时时间
timeout=10  # 10 分钟

touch "$output_file"

# 切换到 mvn 路径
cd "$mvn_path" || exit 1

# 开始执行 mvn clean test，并将输出重定向到临时文件
mvn clean test "$mvn_args" > "$output_file" &


# 获取 mvn 进程的 PID
mvn_pid=$!

# 持续监控输出文件和 mvn 进程
while :
do
    if grep -q "BUILD SUCCESS" "$output_file"; then
        echo "构建成功"
        break
    elif grep -q "BUILD FAILURE" "$output_file"; then
        echo "构建失败，请查看日志文件: $output_file"
        kill "$mvn_pid"  # 结束 mvn 进程
        sleep 10
        exit 1
    elif [ "$(find "$output_file" -mmin +"$timeout")" ]; then
        echo "输出文件长时间未更新，结束 mvn 进程"
        kill "$mvn_pid"  # 结束 mvn 进程
        sleep 3
        kill_mvn_processes "$mvn_path"  # 结束所有访问项目目录的 mvn 进程
        sleep 30
        exit 1
    fi
    sleep 3  # 休眠 3 秒后再次检查输出文件和 mvn 进程
done

echo "测试已完成，结果已经重定向到 $output_file"
exit 0

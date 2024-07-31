from bs4 import BeautifulSoup
import os


def process_html_files(file_details):
    for details in file_details:
        output_filename, html_folder_path = details

        # 创建或打开输出文件
        with open(output_filename, 'w', encoding='utf-8') as output_file:
            # 递归遍历指定文件夹及其所有子文件夹内的所有HTML文件
            for root, dirs, files in os.walk(html_folder_path):
                for filename in files:
                    if filename.endswith(
                            '.html') and 'Test' not in filename and 'test' not in filename:  # 过滤HTML文件且不包含'Test'或'test'
                        file_path = os.path.join(root, filename)

                        # 读取并解析HTML文件
                        with open(file_path, 'r', encoding='utf-8') as file:
                            html_content = file.read()
                            soup = BeautifulSoup(html_content, 'html.parser')

                            # 根据指定的路径和id获取元素，并过滤出包含类'sortValue'的span元素
                            section = soup.find("section", id="contr-tests-dialog")
                            if section:
                                rows = section.select("div table tbody tr")
                                span_texts = set()  # 使用集合来自动去重
                                for row in rows:
                                    span = row.select_one("td:nth-child(3) span.sortValue")
                                    if span and span.text.strip().isdigit() == False:
                                        span_texts.add(span.text.strip())  # 只添加纯文本

                                # 写入文件名前缀、元素数量和元素列表
                                output_file.write(f"{os.path.splitext(filename)[0]}:\n")
                                output_file.write(f"Element count: {len(span_texts)}\n")
                                output_file.write("\n".join(span_texts) + "\n\n")


# 输入数组，每个元素包含输出文件名和HTML文件夹地址
file_details = [
    # ["zk-testlist.txt", "/Users/linzheyuan/zk-clover/clover/org/apache/zookeeper"],
    # ["rmq-testlist.txt", "/Users/linzheyuan/Downloads/site/clover/org/apache/rocketmq"],
    #     ["kafka-testlist.txt", "/Users/linzheyuan/Downloads/kafka-clover/html/org/apache/kafka"],
# ["skywalking-testlist.txt", "/Users/linzheyuan/Downloads/skywalking-clover/org/apache/skywalking"],
# ["hbase-testlist.txt","/Users/linzheyuan/Downloads/site/hbase-client site/clover/org/apache/hadoop/hbase"]
["cas-testlist.txt", "/Users/linzheyuan/Downloads/cas-coverage/org/apache/cassandra"],
]

process_html_files(file_details)

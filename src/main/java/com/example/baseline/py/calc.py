# 输入文件名列表，测试数量和文件数量
file_names = [("zk-testlist.txt",2599), ("zk-testlist.txt",2599)]


# 遍历每个文件，累加其中的元素数量
for file_tuple in file_names:
    file_count = 0
    total_element_count = 0
    file_name = file_tuple[0]
    test_count = file_tuple[1]
    try:
        with open(file_name, 'r', encoding='utf-8') as file:
            for line in file:
                if line.startswith("Element count:"):
                    # 提取元素数量并累加
                    file_count += 1
                    count = int(line.strip().split(":")[1].strip())
                    total_element_count += count
    except FileNotFoundError:
        print(f"File not found: {file_name}")
    except Exception as e:
        print(f"An error occurred while processing {file_name}: {e}")

    # 计算平均值
    if test_count * file_count != 0:
        average = total_element_count / (test_count * file_count)
    else:
        average = 0
        print("Test count and file count must be greater than zero to perform calculation.")

    print(f"Average Element Count: {average}")
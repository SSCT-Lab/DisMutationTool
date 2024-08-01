import json
import os
import re


def count_failed_strings_in_txt_files(root_dir):
    total_txt_files = 0
    total_test_failed = 0
    total_compile_failed = 0
    total_other_failed = 0

    for subdir, dirs, files in os.walk(root_dir):
        if subdir == root_dir:
            continue  # Skip the root directory itself

        txt_file_count = 0
        test_failed_count = 0
        compile_failed_count = 0
        other_failed_count = 0

        print(f"Processing folder: {subdir}")

        for file in files:
            if file.endswith('.txt'):
                txt_file_count += 1
                file_path = os.path.join(subdir, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    last_50_lines = content.splitlines()[-50:]
                    build_success = any("BUILD SUCCESSFUL" in line for line in last_50_lines)
                    build_failed = any("BUILD FAILED" in line for line in last_50_lines)
                    any_passed = re.search(r' > \w+ PASSED', content)
                    any_failed = re.search(r' > \w+ FAILED', content)

                    if not build_success and not build_failed:
                        other_failed_count += 1
                        print(f"\t Other failed in file: {file_path}")
                    elif not any_passed and not any_failed and build_failed:
                        compile_failed_count += 1
                        # print(f"\t Compile failed in file: {file_path}")
                    elif any_failed:
                        test_failed_count += 1
                        print(f"\t Test failed in file: {file_path}")

        total_txt_files += txt_file_count
        failed_file_count = test_failed_count + compile_failed_count + other_failed_count
        total_test_failed += test_failed_count
        total_compile_failed += compile_failed_count
        total_other_failed += other_failed_count
        print(
            f"\tFolder: {subdir} - Total .txt files: {txt_file_count}, Files with '> xxxx FAILED' or no 'BUILD SUCCESS': {failed_file_count}")

    total_failed_files = total_test_failed + total_compile_failed + total_other_failed

    print(
        f"\nOverall total .txt files: {total_txt_files}, Failed files: {total_failed_files}, Test failed: {total_test_failed}, Build failed: {total_compile_failed}, Other failed: {total_other_failed}")

    live = total_txt_files - total_failed_files
    compile_success = total_txt_files - total_compile_failed
    live_total = live / total_txt_files
    live_compile_success = live / compile_success
    print(
        f"\n live/Total: {live_total}, live/TotalCompileSuccess: {live_compile_success}"
    )


def print_test_coverage_for_mutants(root_dir, coverage_file):
    json_res = []
    for subdir, dirs, files in os.walk(root_dir):
        if subdir == root_dir:
            continue  # Skip the root directory itself

        print(f"Processing folder: {subdir}")

        for file in files:
            if file.endswith('.txt'):
                file_path = os.path.join(subdir, file)
                if not continue_process(file_path, 'org.apache.kafka.clients'):
                    continue
                with open(file_path, 'r', encoding='utf-8') as f:
                    res = {"id": file, "coverage_tests": [], "kill_tests": [], "result": ""}
                    content = f.read()
                    last_200_lines = content.splitlines()[-200:]
                    build_success = any("BUILD SUCCESSFUL" in line for line in last_200_lines)
                    build_failed = any("BUILD FAILED" in line for line in last_200_lines)
                    any_passed = re.search(r' > \w+ PASSED', content)
                    any_failed = re.search(r' > \w+ FAILED', content)

                    mutant_id = str(file)
                    res['id'] = mutant_id
                    original_file_name = mutant_id.split("_")[0]
                    # 写入coverage信息
                    res['coverage_tests'] = get_tests_for_class(original_file_name, coverage_file)

                    if not build_success and not build_failed:
                        res['result'] = "timeoutFailure"
                    elif not any_passed and not any_failed and build_failed:
                        res['result'] = "compileFailure"
                    elif any_failed:
                        res['kill_tests'] = extract_failed_methods(content)
                        res['result'] = "testFailure"
                    else:  # 编译成功，测试成功（可能跳过测试）
                        res['result'] = "notKilled"
                    json_res.append(res)
        save_json(json_res, coverage_file)


def extract_failed_methods(content):
    # 处理ANSI字符
    ansi_escape = re.compile(r'\x1B\[[0-?]*[ -/]*[@-~]')
    clean_content = ansi_escape.sub('', content)

    # 分割行
    log_lines = clean_content.splitlines()

    failed_methods = []
    failure_method_pattern = re.compile(r' > \w+ FAILED$')

    for line in log_lines:
        failure_match = failure_method_pattern.search(line)
        if failure_match:
            line_ls = line.split(' > ')
            test_method_name = line_ls[0] + "." + line_ls[1]
            test_method_name = test_method_name.strip(" FAILED")
            failed_methods.append(test_method_name)

    return failed_methods


def get_tests_for_class(class_name, file_path):
    # 读取文件内容
    with open(file_path, 'r') as file:
        file_content = file.read()

    # 分隔文件内容
    sections = re.split(r'\n\n+', file_content.strip())

    # 查找对应的类并返回测试列表
    for section in sections:
        lines = section.split('\n')
        category = lines[0].strip(':')
        if category == class_name:
            return lines[2:]

    # 如果没有找到对应的类，返回空列表
    return []



def continue_process(txt_path: str, package_name: str) -> bool:
    # package org.apache.kafka.clients
    # 将文件扩展名从 .txt 改为 .java
    java_path = os.path.splitext(txt_path)[0] + '.java'

    # 判断 .java 文件是否存在
    if not os.path.exists(java_path):
        return False

    # 读取文件内容
    with open(java_path, 'r') as file:
        content = file.read()

    # 查找 package 声明
    package_declaration = None
    for line in content.splitlines():
        if line.strip().startswith('package '):
            package_declaration = line.strip()
            break

    # 如果没有找到 package 声明，返回 False
    if not package_declaration:
        return False

    # 提取 package 名称
    package_name_in_file = package_declaration.split(' ')[1].rstrip(';')

    # 判断参数中的 package_name 是否包含在 package 名称中
    return package_name in package_name_in_file


def save_json(json_res, coverage_path: str):
    path_ls = coverage_path.split("/")
    json_file_name = path_ls[len(path_ls) - 1].split('-')[0] + "-res.json"
    json_file_path = '/'.join(path_ls[:-2]) + '/statisticsResults/'
    json_file = json_file_path + json_file_name

    with open(json_file, 'w') as file:
        json.dump(json_res, file, indent=4)
    return


if __name__ == "__main__":
    kafka_path = "/home/zdc/桌面/fromServer/kafka/kafkaMutant/testOutputs/"
    coverage_path = "/home/zdc/code/DisMutationTool/coverageInfo/kafka-testlist.txt"
    # count_failed_strings_in_txt_files(kafka_path)
    print_test_coverage_for_mutants(kafka_path, coverage_path)

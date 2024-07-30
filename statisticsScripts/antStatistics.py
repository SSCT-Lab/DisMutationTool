import json
import os
import re


def count_build_success_in_txt_files(directory, package_prefix):
    total_txt_files = 0
    total_build_success_count = 0
    total_compile_failure_count = 0
    total_test_failure_count = 0
    total_timeout_count = 0
    directory_stats = {}

    # Walk through the directory
    for root, dirs, files in os.walk(directory):
        txt_files_count = 0
        test_success_count = 0
        compile_failure_count = 0
        test_failure_count = 0

        for file in files:
            if file.endswith('.txt'):

                file_path = os.path.join(root, file)
                if not continue_process(file_path, package_prefix):
                    continue
                txt_files_count += 1
                try:
                    with (open(file_path, 'r', encoding='utf-8') as f):
                        content = f.read()
                        if "Compile failed" in content:
                            compile_failure_count += 1
                            continue
                        matches = re.findall(r'Test .+ FAILED', content)
                        if "Test org.apache.cassandra.tools.CompactionStressTest FAILED" in matches \
                                and len(matches) == 1:
                            test_success_count += 1
                        elif "Test org.apache.cassandra.tools.CompactionStressTest FAILED" in matches \
                                and "Test org.apache.cassandra.db.commitlog.CommitLogSegmentBackpressureTest FAILED" in matches \
                                and len(matches) == 2:
                            test_success_count += 1
                        elif len(matches) > 1:
                            test_failure_count += 1
                        else:
                            if "Total time" not in content:
                                print("Timeout" + file)
                                test_failure_count += 1
                            else:
                                print("Error " + file)

                except Exception as e:
                    print(f"Error reading file {file_path}: {e}")

        if txt_files_count > 0:
            directory_stats[root] = {
                'txt_files_count': txt_files_count,
                'test_success_count': test_success_count,
                'compile_failure_count': compile_failure_count,
                'test_failure_count': test_failure_count
            }

        total_txt_files += txt_files_count
        total_build_success_count += test_success_count
        total_compile_failure_count += compile_failure_count
        total_test_failure_count += test_failure_count

    return total_txt_files, total_build_success_count, total_compile_failure_count, total_test_failure_count, directory_stats


def continue_process(txt_path: str, package_name: str) -> bool:
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


def print_test_coverage_for_mutants(directory, package_prefix):
    json_res = []

    # Walk through the directory
    for root, dirs, files in os.walk(directory):

        for file in files:
            if file.endswith('.txt'):
                file_path = os.path.join(root, file)
                if not continue_process(file_path, package_prefix):
                    continue
                res = {"id": file, "coverage_tests": [], "kill_tests": [], "result": ""}
                try:
                    with (open(file_path, 'r', encoding='utf-8') as f):
                        content = f.read()
                        if "Compile failed" in content:
                            res['result'] = "compileFailure"
                        else:
                            matches = re.findall(r'Test .+ FAILED', content)
                            if "Test org.apache.cassandra.tools.CompactionStressTest FAILED" in matches \
                                    and len(matches) == 1:
                                res['result'] = "notKilled"
                            elif "Test org.apache.cassandra.tools.CompactionStressTest FAILED" in matches \
                                    and "Test org.apache.cassandra.db.commitlog.CommitLogSegmentBackpressureTest FAILED" in matches \
                                    and len(matches) == 2:
                                res['result'] = "notKilled"
                            else:
                                matches2 = re.findall(r'Test .+ FAILED', content)
                                lines = [x for x in matches2 if "org.apache.cassandra.tools.CompactionStressTest" not in x or "org.apache.cassandra.db.commitlog.CommitLogSegmentBackpressureTest" not in x]
                                if len(lines) > 0:
                                    for line in lines:
                                        test_method = line.split(r" ")[1]
                                        res['kill_tests'].append(test_method)
                                    res['result'] = "testFailure"
                                else:
                                    if "Total time" not in content:
                                        res['result'] = "compileFailure"
                                    else:
                                        res['result'] = "timeoutFailure"
                                # matches2 = re.findall(r'Testcase:.+(FAILED|ERROR)$', content)
                                # lines = [x for x in matches2 if "org.apache.cassandra.tools.CompactionStressTest" not in x or "org.apache.cassandra.db.commitlog.CommitLogSegmentBackpressureTest" not in x]
                                # if len(lines) > 0:
                                #     for line in lines:
                                #         test_method = line.split(r" ")[2]
                                #         res['kill_tests'].append(test_method)
                                #     res['result'] = "testFailure"
                                # else:
                                #     if "Total time" not in content:
                                #         res['result'] = "compileFailure"
                                #     else:
                                #         res['result'] = "timeoutFailure"

                            # elif len(matches) >= 1:
                            #     # 提取测试名
                            #     for line in matches:
                            #         test_method = line.split(" ")[1]
                            #         res['kill_tests'].append(test_method)
                            #     res['result'] = "testFailure"
                            # else:
                            #     if "Total time" not in content:
                            #         res['result'] = "compileFailure"
                            #     else:
                            #         res['result'] = "timeoutFailure"
                        json_res.append(res)
                except Exception as e:
                    print(f"Error reading file {file_path}: {e}")
                save_json(json_res, "/home/zdc/code/DisMutationTool/statisticsResults/cas-res.json")


def print_test_coverage_for_mutants_v2(directory, package_prefix):
    json_res = []

    # Walk through the directory
    for root, dirs, files in os.walk(directory):

        for file in files:
            if file.endswith('.txt'):
                file_path = os.path.join(root, file)
                if not continue_process(file_path, package_prefix):
                    continue
                res = {"id": file, "coverage_tests": [], "kill_tests": [], "result": ""}
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                        content = ''.join(lines)
                        if "Compile failed" in content:
                            res['result'] = "compileFailure"
                        else:
                            lines = [line.strip() for line in lines]
                            test_fail_or_error_ls = [x for x in lines if re.match(r'.*Testcase:.*(FAILED|ERROR)', x) and 'CompactionStressTest' not in x and 'CommitLogSegmentBackpressureTest' not in x]
                            if len(test_fail_or_error_ls) > 0:
                                for test_case in test_fail_or_error_ls:
                                    res['kill_tests'].append(re.split(r'\s+',test_case)[2].strip(":"))
                                res['result'] = "testFailure"
                            else:
                                if "Total time" not in content:
                                    res['result'] = "timeoutFailure"
                                else:
                                    res['result'] = "notKilled"

                        json_res.append(res)
                except Exception as e:
                    print(f"Error reading file {file_path}: {e}")
                save_json(json_res, "/home/zdc/code/DisMutationTool/statisticsResults/cas-res.json")


def save_json(json_res, path: str):
    with open(path, 'w') as file:
        json.dump(json_res, file, indent=4)
    return


if __name__ == "__main__":
    directory = "/home/zdc/桌面/fromServer/cas/testOutputs/testOutputs/"  # Current directory
    # total_txt_files, total_build_success_count, total_compile_failure_count, total_test_failure_count, directory_stats = count_build_success_in_txt_files(
    #     directory, "")

    # total_txt_files, total_build_success_count, total_compile_failure_count, total_test_failure_count, directory_stats = count_build_success_in_txt_files(
    #     directory, "org.apache.cassandra.db")

    # print(f"Total .txt files: {total_txt_files}")
    # print(f"Total .txt files with 'BUILD SUCCESS': {total_build_success_count}")
    # print(f"Total .txt files with 'COMPILATION ERROR': {total_compile_failure_count}")
    # print(f"Total .txt files with 'TEST FAILURE': {total_test_failure_count}")
    #
    # print("\nDetailed stats per directory:")
    # for dir_path, stats in directory_stats.items():
    #     print(f"Directory: {dir_path}")
    #     print(f"  .txt files: {stats['txt_files_count']}")
    #     print(f"  .txt files with 'BUILD SUCCESS': {stats['test_success_count']}")
    #     print(f"  .txt files with 'COMPILATION ERROR': {stats['compile_failure_count']}")
    #     print(f"  .txt files with 'TEST FAILURE': {stats['test_failure_count']}")
    print_test_coverage_for_mutants_v2(directory, "org.apache.cassandra.db")

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
                    tests_run_count = len(re.findall(r'Tests run:', content))
                    success_count = len(re.findall(r'Failures: 0, Errors: 0', content))
                    last_200_lines = content.splitlines()[-200:]
                    build_failure = any("BUILD FAILURE" in line for line in last_200_lines)
                    compile_failure = any(
                        re.search(r'Failed to execute goal .*:compile', line) for line in last_200_lines)
                    build_success = any("BUILD SUCCESS" in line for line in last_200_lines)

                    if tests_run_count != success_count:
                        test_failed_count += 1
                        print(f"\tTest failure in file: {file_path}")
                    elif build_failure and compile_failure:
                        compile_failed_count += 1
                        print(f"\tCompile failure in file: {file_path}")
                    elif tests_run_count == success_count and build_success:
                        continue  # Test success, do nothing
                    else:
                        other_failed_count += 1
                        print(f"\tOther failure in file: {file_path}")

        total_txt_files += txt_file_count
        failed_file_count = test_failed_count + compile_failed_count + other_failed_count
        total_test_failed += test_failed_count
        total_compile_failed += compile_failed_count
        total_other_failed += other_failed_count

        print(
            f"\tFolder: {subdir} - Total .txt files: {txt_file_count}, Failed files: {failed_file_count}, Test failed:{test_failed_count}, Build failed: {build_failure}, Other failed: {other_failed_count}")

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
                with open(file_path, 'r', encoding='utf-8') as f:
                    res = {"id": file, "coverage_tests": [], "kill_tests": [], "result": ""}
                    content = f.read()
                    tests_run_count = len(re.findall(r'Tests run:', content))
                    success_count = len(re.findall(r'Failures: 0, Errors: 0', content))
                    last_200_lines = content.splitlines()[-200:]
                    build_failure = any("BUILD FAILURE" in line for line in last_200_lines)
                    compile_failure = any(
                        re.search(r'Failed to execute goal .*:compile', line) for line in last_200_lines)
                    build_success = any("BUILD SUCCESS" in line for line in last_200_lines)

                    mutant_id = str(file)
                    res['id'] = mutant_id
                    original_file_name = mutant_id.split("_")[0]
                    # 写入coverage信息
                    res['coverage_tests'] = get_tests_for_class(original_file_name, coverage_file)

                    if tests_run_count != success_count:
                        res['kill_tests'] = extract_failed_methods(content)
                        res['result'] = "testFailure"
                    elif build_failure and compile_failure:  # 编译失败
                        res['result'] = "compileFailure"
                    elif tests_run_count == success_count and build_success:  # 编译成功，测试成功（可能跳过测试）
                        res['result'] = "notKilled"
                    else:  # 运行超时
                        res['result'] = "timeoutFailure"
                    json_res.append(res)
        save_json(json_res, coverage_file)


def extract_failed_methods(content):
    # 处理ANSI字符
    ansi_escape = re.compile(r'\x1B\[[0-?]*[ -/]*[@-~]')
    clean_content = ansi_escape.sub('', content)

    # 分割行
    log_lines = clean_content.splitlines()

    failed_methods = []
    failure_method_pattern = re.compile(r".*<<< (FAILURE|ERROR)!$")

    for line in log_lines:
        failure_match = failure_method_pattern.search(line)
        if failure_match:
            line_ls = line.split(r' ')
            failed_methods.append(line_ls[1])

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


def save_json(json_res, coverage_path: str):
    path_ls = coverage_path.split("/")
    json_file_name = path_ls[len(path_ls) - 1].split('-')[0] + "-res.json"
    json_file_path = '/'.join(path_ls[:-2]) + '/statisticsResults/'
    json_file = json_file_path + json_file_name

    with open(json_file, 'w') as file:
        json.dump(json_res, file, indent=4)
    return


if __name__ == "__main__":
    zk_path = "/home/zdc/桌面/fromServer/zk/zkMutation/testOutputs/, /home/zdc/code/DisMutationTool/coverageInfo/zk-testlist.txt"
    rmq_path = "/home/zdc/桌面/fromServer/rmq/rmqMutation/testOutputs/, /home/zdc/code/DisMutationTool/coverageInfo/rmq-testlist.txt"
    hbase_path = "/home/zdc/桌面/fromServer/hbase/hbaseMutant/testOutputs/, /home/zdc/code/DisMutationTool/coverageInfo/hbase-testlist.txt"
    cur = rmq_path
    # count_failed_strings_in_txt_files(cur_path)
    print_test_coverage_for_mutants(cur.split(", ")[0], cur.split(", ")[1])

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


if __name__ == "__main__":
    zk_path = "/home/zdc/桌面/fromServer/zk/zkMutation/testOutputs/"
    rmq_path = "/home/zdc/桌面/fromServer/rmq/rmqMutation/testOutputs/"
    hbase_path = "/home/zdc/桌面/fromServer/hbase/hbaseMutant/testOutputs/"
    cur_path = rmq_path
    count_failed_strings_in_txt_files(cur_path)

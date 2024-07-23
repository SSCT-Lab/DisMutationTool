import os
import re

def count_failed_strings_in_txt_files(root_dir):
    total_txt_files = 0
    total_failed_files = 0
    total_test_failed = 0
    total_compile_failed = 0
    total_other_failed = 0

    for subdir, dirs, files in os.walk(root_dir):
        if subdir == root_dir:
            continue  # Skip the root directory itself

        txt_file_count = 0
        failed_file_count = 0
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
                    last_50_lines = content.splitlines()[-200:]
                    build_failure = any("BUILD FAILURE" in line for line in last_50_lines)
                    compile_failure = any(re.search(r'Failed to execute goal .*:compile', line) for line in last_50_lines)
                    build_success = any("BUILD SUCCESS" in line for line in last_50_lines)

                    if tests_run_count != success_count:
                        failed_file_count += 1
                        test_failed_count += 1
                        # print(f"Test failure in file: {file_path}")
                    elif build_failure and compile_failure:
                        failed_file_count += 1
                        compile_failed_count += 1
                    elif tests_run_count == success_count and build_success:
                        continue  # Test success, do nothing
                    else:
                        failed_file_count += 1
                        other_failed_count += 1
                        # print(f"Other failure in file: {file_path}")

        total_txt_files += txt_file_count
        total_failed_files += failed_file_count
        total_test_failed += test_failed_count
        total_compile_failed += compile_failed_count
        total_other_failed += other_failed_count

        print(f"\t\tFolder: {subdir} - Total .txt files: {txt_file_count}, Failed files: {failed_file_count}, Test failed:{test_failed_count}, Build failed: {build_failure}, Other failed: {other_failed_count}")

    print(f"\nOverall total .txt files: {total_txt_files}, Failed files: {total_failed_files}, Test failed:{total_test_failed}, Build failed: {total_compile_failed}, Other failed: {total_other_failed}")

if __name__ == "__main__":
    count_failed_strings_in_txt_files("./data/rmqtestOutputs")

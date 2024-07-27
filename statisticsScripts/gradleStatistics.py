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
                        print(f"\t Compile failed in file: {file_path}")
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

if __name__ == "__main__":
    kafka_path = "/home/zdc/桌面/fromServer/kafka/kafkaMutant/testOutputs/"
    count_failed_strings_in_txt_files(kafka_path)

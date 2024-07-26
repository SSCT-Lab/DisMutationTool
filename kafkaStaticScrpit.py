import os
import re

def count_failed_strings_in_txt_files(root_dir):
    total_txt_files = 0
    total_failed_files = 0

    for subdir, dirs, files in os.walk(root_dir):
        if subdir == root_dir:
            continue  # Skip the root directory itself

        txt_file_count = 0
        failed_file_count = 0

        print(f"Processing folder: {subdir}")

        for file in files:
            if file.endswith('.txt'):
                txt_file_count += 1
                file_path = os.path.join(subdir, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    passed = re.search(r' > \w+ PASSED', content)
                    failed = re.search(r' > \w+ FAILED', content)

                    if not passed and not failed:
                        last_50_lines = content.splitlines()[-50:]
                        if not any("BUILD SUCCESS" in line for line in last_50_lines):
                            print(f"\t\t {file} compile failed")
                            failed_file_count += 1
                    elif failed:
                        print(f"\t\t {file} test failed")
                        failed_file_count += 1

        total_txt_files += txt_file_count
        total_failed_files += failed_file_count

        print(f"\t\tFolder: {subdir} - Total .txt files: {txt_file_count}, Files with '> xxxx FAILED' or no 'BUILD SUCCESS': {failed_file_count}")

    print(f"\nOverall total .txt files: {total_txt_files}, Files with '> xxxx FAILED' or no 'BUILD SUCCESS': {total_failed_files}")

if __name__ == "__main__":
    count_failed_strings_in_txt_files("./data/kafkaTestOutputs")

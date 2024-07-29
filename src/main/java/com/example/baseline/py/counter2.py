import os
import re


def count_build_success_in_txt_files(directory):
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
                txt_files_count += 1
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        if "Compile failed" in content:
                            compile_failure_count += 1
                            continue
                        matches = re.findall(r'Test .+ FAILED', content)
                        if "Test org.apache.cassandra.tools.CompactionStressTest FAILED" in matches and len(
                                matches) == 1:
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


if __name__ == "__main__":
    directory = "./testOutputs"  # Current directory
    total_txt_files, total_build_success_count, total_compile_failure_count, total_test_failure_count,directory_stats = count_build_success_in_txt_files(
        directory)

    print(f"Total .txt files: {total_txt_files}")
    print(f"Total .txt files with 'BUILD SUCCESS': {total_build_success_count}")
    print(f"Total .txt files with 'COMPILATION ERROR': {total_compile_failure_count}")
    print(f"Total .txt files with 'TEST FAILURE': {total_test_failure_count}")

    print("\nDetailed stats per directory:")
    for dir_path, stats in directory_stats.items():
        print(f"Directory: {dir_path}")
        print(f"  .txt files: {stats['txt_files_count']}")
        print(f"  .txt files with 'BUILD SUCCESS': {stats['test_success_count']}")
        print(f"  .txt files with 'COMPILATION ERROR': {stats['compile_failure_count']}")
        print(f"  .txt files with 'TEST FAILURE': {stats['test_failure_count']}")

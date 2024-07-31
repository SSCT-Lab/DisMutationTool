import json
# list_of_obj=["cas","hbase","kafka","rmq","sw","zk"]
list_of_obj=["cas-cov","hbase","kafka","rmq","sw","zk"]
def calculate_mutant_statistics(mutants):
    total_mutants = len(mutants)

    # 1. 计算覆盖测试编号数组为空的变异体占比
    no_coverage_count = sum(1 for mutant in mutants if len(mutant["coverage_tests"]) == 0)
    no_coverage_ratio = no_coverage_count / total_mutants

    # 4. 计算 easy-to-kill 变异体的数量
    easy_to_kill_count = sum(1 for mutant in mutants if mutant['coverage_tests'] and len(mutant['kill_tests']) / len(
        mutant['coverage_tests']) > 0.95 )
    easy_to_kill_ratio = easy_to_kill_count / total_mutants
    print(easy_to_kill_count,end="\t")


    # 2. 计算杀死测试编号数组完全相同的变异体占比
    from collections import defaultdict

    kill_tests_map = defaultdict(int)
    for mutant in mutants:
        if mutant['coverage_tests']:  # 排除覆盖测试编号数组为空的变异体
            kill_tests_tuple = tuple(sorted(mutant['kill_tests']))
            kill_tests_map[kill_tests_tuple] += 1

    duplicate_kill_tests_count = sum(1 for count in kill_tests_map.values() if count > 1)
    duplicate_kill_tests_ratio = duplicate_kill_tests_count / total_mutants

    # 3. 计算杀死测试编号数组为其他数组真子集的变异体占比
    subset_count = 0
    for i, mutant_i in enumerate(mutants):
        if mutant_i['coverage_tests'] and mutant_i['kill_tests']:  # 排除覆盖测试编号数组为空的变异体
            kill_tests_i = set(mutant_i['kill_tests'])
            for j, mutant_j in enumerate(mutants):
                if i != j and mutant_j['coverage_tests'] and mutant_j['kill_tests']!=[]:  # 排除覆盖测试编号数组为空的变异体
                    kill_tests_j = set(mutant_j['kill_tests'])
                    if kill_tests_i < kill_tests_j:  # 真子集判断
                        subset_count += 1
                        break
    print(duplicate_kill_tests_count,end="\t")
    print(subset_count,end="\t")
    subset_ratio = subset_count / total_mutants


    return no_coverage_ratio, duplicate_kill_tests_ratio, subset_ratio, easy_to_kill_ratio



for i in range(0,len(list_of_obj)):
    # 示例变异体数组
    # with open("./res/"+list_of_obj[i]+"-output.json", 'r') as file:
    with open("/Users/linzheyuan/code/DisMutationTool/statisticsResults/"+list_of_obj[i]+"-res.json", 'r') as file:
        mutants = json.load(file)
    print(list_of_obj[i]+":\t")
    no_coverage_ratio, duplicate_kill_tests_ratio, subset_ratio, easy_to_kill_count = calculate_mutant_statistics(mutants)

    print(f"覆盖测试编号数组为空的变异体占比: {no_coverage_ratio:.2f}")
    print(f"杀死测试编号数组完全相同的变异体占比: {duplicate_kill_tests_ratio:.2f}")
    print(f"杀死测试编号数组为其他数组真子集的变异体占比: {subset_ratio:.2f}")
    print(f"Easy-to-kill 变异体总数: {easy_to_kill_count}")
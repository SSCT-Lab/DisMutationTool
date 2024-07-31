import json
from collections import defaultdict


system_list = ["cas-cov","hbase", "kafka", "rmq", "sw", "zk"]
def dismute_table_data(sys):
    print(sys)
    # 读取 JSON 文件
    file_path = '/Users/linzheyuan/code/DisMutationTool/statisticsResults/'+sys+'-res.json'
    with open(file_path, 'r') as file:
        data = json.load(file)

    # 初始化统计字典
    operator_stats = defaultdict(lambda: {
        'mutation_count': 0,
        'coverage_count': 0,
        'not_killed_count': 0
    })

    # 定义算子列表
    operators = ["MNT", "MNR", "RRC", "UNE","RNE", "MCT", "BCS","RCS","NCS","SCS","RTS","UCE","RCE","RCF","UFE","RFE"]
    # 定义需要合并的算子对
    merge_map = {
        "UNE": "RNE",
        "UCE": "RCE",
        "UFE": "RFE"
    }
    # 处理每个变异体
    for entry in data:
        # 解析 id 获取算子名
        file_id = entry['id']
        operator_name = file_id.split('_')[1]

        # 如果算子需要合并，使用合并后的名称
        if operator_name in merge_map:
            operator_name = merge_map[operator_name]

        # 确保算子在定义的列表中
        if operator_name in operators:
            operator_stats[operator_name]['mutation_count'] += 1

            # 统计 coverage_tests 不为空的情况
            if entry['coverage_tests']:
                operator_stats[operator_name]['coverage_count'] += 1

            # 统计 kill_tests 为空的情况
            if entry['result']=="notKilled":
                operator_stats[operator_name]['not_killed_count'] += 1

    # 输出统计结果
    output = "Operator\tMutation Count\tCoverage Count\tNot Killed Count\n"
    for operator in operators:
        # 跳过需要合并的算子名称
        if operator in merge_map:
            continue
        stats = operator_stats[operator]
        # output += f"{operator}\t{stats['mutation_count']}\t{stats['coverage_count']}\t{stats['not_killed_count']}\n"
        output += f"{stats['mutation_count']}\t{stats['coverage_count']}\t{stats['not_killed_count']}\n"
    print(output)
    # # 对 rmq 和 zk 特殊处理 kill_tests 的情况
    # rmq_zk_test_methods = defaultdict(set)
    # for entry in data:
    #     file_id = entry['id']
    #     operator_name = file_id.split('_')[1]
    #
    #     if sys in ["rmq", "zk"] and entry['kill_tests']:
    #         for test in entry['kill_tests']:
    #             test_method = test.split('(')[0]
    #             rmq_zk_test_methods[operator_name].add(test_method)
    #
    # # 输出 rmq 和 zk 的测试方法
    # for operator, methods in rmq_zk_test_methods.items():
    #     print(f"Operator: {operator} - Test Methods")
    #     for method in methods:
    #         print(f"  {method}")
    #     print()

for sys in system_list:
    dismute_table_data(sys)
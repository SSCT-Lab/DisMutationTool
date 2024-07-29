import os
import json
from bs4 import BeautifulSoup

all_results = []
test_list = dict()
id_counter = 1
mutant_list = dict()
# directory = "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/rmq-pit-custom-output-dir"
list_of_obj=["hbase","kafka","rmq","sw","zk"]
list_of_pit_dirs=["/Users/linzheyuan/Downloads/DisMuTe-Result/pit/hbaseclient-pit-reports",
                  "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/kafka-reports/pitest",
                  "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/rmq-pit-custom-output-dir",
                  "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/skywalking-pit-custom-output-dir",
                  "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/zk-server-pit-reports"]

def extract_test_name(full_test_name):
    # 仅保留org.apache.rocketmq.common.CountDownLatch2Test.testReset部分
    return full_test_name.split('(')[0]


def process_all_java_html_files(directory):
    for root, dirs, files in os.walk(directory):
        for filename in files:
            if filename.endswith(".java.html"):
                filepath = os.path.join(root, filename)
                file_results = process_java_html_file(filepath, test_list)
                all_results.extend(file_results)
    return all_results, test_list

def process_java_html_file(filepath, test_list):
    global id_counter
    result = []
    with open(filepath, "r", encoding="utf-8") as file:
        soup = BeautifulSoup(file, "html.parser")
        # 提取test列表
        test_elements = soup.select("ul:nth-of-type(2) li")
        this_html_test=dict()
        coverage_tests = []
        for test in test_elements:
            test_name = extract_test_name(test.get_text())
            if test_name not in test_list:
                test_list[test_name] = len(test_list) + 1
            this_html_test[test_name] = test_list[test_name]


        # 查找包含<h2>Mutations</h2>的<tr>元素
        mutation_tr = soup.find_all('tr')
        for tr in mutation_tr:
            if tr.find('h2') and tr.find('h2').text == 'Mutations':
                # 查找所有后续的<tr>元素
                next_tr = tr.find_next_sibling('tr')
                while next_tr:
                    killed_elements = next_tr.find_all('p', class_=['KILLED', 'SURVIVED'])
                    for p in killed_elements:
                        kill_tests = []
                        if 'NO_COVERAGE' not in p.get('class', []):
                            span = p.find('span', class_='pop')
                            coverage_tests=this_html_test
                            if span:
                                span_text = span.get_text(separator=' ')
                                if 'Killed by :' in span_text:
                                    killed_by = span_text.split('Killed by : ')[1].split()[0]
                                    killed_by_test = extract_test_name(killed_by)
                                    if killed_by_test in test_list:
                                        killed_test_id = test_list[killed_by_test]
                                        kill_tests.append(killed_test_id)
                        else:
                            coverage_tests=[]
                            kill_tests=[]
                        result.append({'id': id_counter, 'coverage_tests': coverage_tests, 'kill_tests': kill_tests})
                    next_tr = next_tr.find_next_sibling('tr')
                    id_counter += 1
                    coverage_tests = []
    return result


for i in range(0,len(list_of_obj)):
    directory=list_of_pit_dirs[i]
    output_filepath = os.path.join("./res/"+list_of_obj[i]+"-output.json")
    if os.path.exists(output_filepath):
        print(f"{output_filepath} exists!")
    else:
        all_results, test_list = process_all_java_html_files(directory)
        output_data = {
            "results": all_results,
            "test_list": test_list
        }
        with open(output_filepath, "w", encoding="utf-8") as json_file:
            json.dump(output_data, json_file, ensure_ascii=False, indent=4)

        print(f"Results have been written to {output_filepath}")
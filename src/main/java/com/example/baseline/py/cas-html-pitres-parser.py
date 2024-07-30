import os
import json
from bs4 import BeautifulSoup

all_results = []
test_list = dict()
id_counter = 1
mutant_list = dict()
alived=0
killed=0
nocov=0
# directory = "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/rmq-pit-custom-output-dir"

def extract_test_name(full_test_name):
    return full_test_name.split('(')[0]

def process_all_java_html_files(directory):
    for root, dirs, files in os.walk(directory):
        for filename in files:
            if filename.endswith(".java.html"):
                filepath = os.path.join(root, filename)
                process_java_html_file(filepath)

    print(alived)
    print(killed)
    print(nocov)

def process_java_html_file(filepath):
    global alived
    global killed
    global nocov
    result = []
    with open(filepath, "r", encoding="utf-8") as file:
        soup = BeautifulSoup(file, "html.parser")
        # 提取test列表
        # test_elements = soup.select("ul:nth-of-type(2) li")
        # this_html_test = dict()
        # for test in test_elements:
        #     test_name = extract_test_name(test.get_text())
        #     if test_name not in test_list:
        #         test_list[test_name] = len(test_list) + 1
        #     this_html_test[test_name] = test_list[test_name]

        # 查找包含<h2>Mutations</h2>的<tr>元素
        mutation_tr = soup.find_all('tr')
        for tr in mutation_tr:
            if tr.find('h2') and tr.find('h2').text == 'Mutations':
                # 查找所有后续的<tr>元素
                next_tr = tr.find_next_sibling('tr')
                while next_tr:
                    tds = next_tr.find_all('td')
                    for td in tds:
                        ps = td.find_all('p')
                        for p in ps:
                            status = p.get('class', [])
                            if 'SURVIVED' in status or 'NO_COVERAGE' in status:
                                alived+=1
                                if 'NO_COVERAGE' in status:
                                    nocov+=1
                            else:
                                killed+=1
                    next_tr = next_tr.find_next_sibling('tr')


directory = "/Users/linzheyuan/Downloads/DisMuTe-Result/pit/util"
process_all_java_html_files(directory)
print(killed/(killed+alived))


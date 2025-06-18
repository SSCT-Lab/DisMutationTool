#!/bin/bash

# 设置临时目录
TMP_DIR="/tmp/dev_to_main_copy"
SRC_BRANCH="dev"
DEST_BRANCH="main"

echo "🚀 开始从 $SRC_BRANCH 拷贝文件到 $DEST_BRANCH"

# 1. 确保干净工作区
if [ -n "$(git status --porcelain)" ]; then
    echo "❗ 请先提交或清理当前工作目录的改动"
    exit 1
fi

# 2. 切换到源分支并拉取最新
git checkout $SRC_BRANCH || exit 1
git pull origin $SRC_BRANCH || exit 1

# 3. 清理并创建临时目录
rm -rf $TMP_DIR
mkdir -p $TMP_DIR

# 4. 拷贝所有需要的文件到临时目录
FILES=(
    "pom.xml"
    "src/main/java/DockerExample.java"
    "src/main/java/com/example/App.java"
    "src/main/java/com/example/MutantManager.java"
    "src/main/java/com/example/Project.java"
    "src/main/java/com/example/baseline/py/README.md"
    "src/main/java/com/example/baseline/py/calc.py"
    "src/main/java/com/example/baseline/py/cas-html-pitres-parser.py"
    "src/main/java/com/example/baseline/py/counter2.py"
    "src/main/java/com/example/baseline/py/dismute-table-data-calc.py"
    "src/main/java/com/example/baseline/py/extract.py"
    "src/main/java/com/example/baseline/py/paper_table_data_cal.py"
    "src/main/java/com/example/baseline/py/paper_table_data_cal_tool.py"
    "src/main/java/com/example/baseline/py/pit-html-parser.py"
    "src/main/java/com/example/baseline/py/requirements.txt"
    "src/main/java/com/example/baseline/script/MutationTestAnalysis.java"
    "src/main/java/com/example/baseline/script/TestClassCluster.java"
    "src/main/java/com/example/baseline/util/ExtractExcludedTestClasses.java"
    "src/main/java/com/example/baseline/util/SortAllTestClasses.java"
    "src/main/java/com/example/baseline/util/TestClassExtract.java"
    "src/main/java/com/example/coverage/CassandraCodeInstrumentation.java"
    "src/main/java/com/example/coverage/ZookeeperCodeInstrumentation.java"
    "src/main/java/com/example/mutantFilter/BytecodeFilter.java"
    "src/main/java/com/example/mutantFilter/IdenticalFilter.java"
    "src/main/java/com/example/mutantgen/EquivalentMutantFilter.java"
    "src/main/java/com/example/mutantgen/IdenticalMutantFilter.java"
    "src/main/java/com/example/mutantgen/MutantGenerator.java"
    "src/main/java/com/example/mutantrun/AntRunner.java"
    "src/main/java/com/example/mutantrun/MutantRunner.java"
    "src/main/java/com/example/mutantrun/MutantRunnerScript.java"
    "src/main/java/com/example/mutantrun/MutantRunnerScriptOld.java"
    "src/main/java/com/example/mutantrun/MvnRunner.java"
    "src/main/java/com/example/mutantrun/TestSuiteRunner.java"
    "src/main/java/com/example/mutator/DiscardExceptionOperator.java"
)

for FILE in "${FILES[@]}"; do
    DIR_PATH=$(dirname "$FILE")
    mkdir -p "$TMP_DIR/$DIR_PATH"
    cp "$FILE" "$TMP_DIR/$FILE"
done

# 5. 切换到目标分支 main 并更新
git checkout $DEST_BRANCH || exit 1
git pull origin $DEST_BRANCH || exit 1

# 6. 删除 main 分支中的 DOCS.md（已被删除）
git rm -f DOCS.md

# 7. 将文件复制回来
cp -r $TMP_DIR/* ./

# 8. 添加并提交
git add .
git commit -m "🛠 手动从 dev 分支引入所有模块与配置文件变更"
git push origin $DEST_BRANCH

echo "✅ 拷贝完成并已提交至 $DEST_BRANCH"

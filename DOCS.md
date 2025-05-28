# Dismute 文档

## 项目概述
本项目为一款面向分布式系统的变异测试工具框架，旨在支持对 Java 项目中的变异体生成、筛选、执行与评估，进而辅助测试人员识别测试用例的有效性，提升测试集质量。

系统设计以高可扩展性与模块化为目标，采用多种构建工具适配器（如 Maven、Gradle、Ant），集成多类变异算子与过滤机制，支持对变异体的多维度管理与分析。

好的，以下是项目文档第二部分内容「功能模块」：

---

## 包概述

本项目由多个独立模块组成，各模块职责分明，整体协同完成变异测试的自动化流程。主要包如下：

### 1. 构建工具适配模块（adapter）

位于 `AntAdapter.java`、`GradleAdapter.java`、`MavenAdapter.java`，统一通过 `BuildToolAdapter.java` 接口抽象构建操作。

这个模块封装了对不同构建工具的调用命令。比如使用maven运行部分测试时的命令为：
```bash
mvn clean test -Dtest=SomeTestClasses
```

而ant对应的命令则为：
```bash
ant clean test testSome -Dtest.name=SomeTestClasses
```
这个模块主要用于封装这些命令

### 2. 变异体生成与管理（mutantgen）

核心类：

* `MutatorFactory.java`：负责生成各类变异算子对象
* `Mutant.java`：定义变异体的元信息
* `MutatorType.java`：枚举各类支持的变异算子
* `MutantManager.java`：集中管理所有变异体的生成、记录与存储


### 3. 变异体筛选过滤（mutantfilter）

过滤器类位于 `*.MutantFilter.java` 相关文件中，支持多种变异体过滤策略：

* `IdenticalMutantFilter`：去除与原始代码完全相同的变异体
* `SyntacticMutantFilter`：过滤语法层面无意义的变异体（JavaParser能解析语法树即视为语法正确）
* `SemanticMutantFilter`：基于运行语义的冗余移除（采用增量编译）
* `BytecodeMutantFilter`：根据字节码一致性判断无效变异体

### 4. 变异算子库（mutator）

实现了全部13个变异算子

### 4. 其他单例类（singletion、engine）

- `Project.java`：管理整个项目的所有参数配置信息和路径信息
- `RunningEngine.java`：变异体运行引擎，编排了整个运行流程

## 运行流程

1. 通过传入参数构造Project单例
2. 构造RunningEngine单例，接下来的逻辑都在RunningEngine中编排（其实也可以把Project的构造放进来的）
3. 如果传入参数中制定了覆盖率信息的路径，构造CoverageManager单例，
    > 解析文件，生成coverageInfoMap，key为源码文件名，value为覆盖该源码文件的测试类的列表
4. 构造BuildToolAdapter单例和MutantManager单例
5. 编译原始项目，生成classpath.txt用于增量编译
    > classpath.txt记录了当前项目所有依赖的路径，用于增量编译时传给cp参数
6. 编译原始项目，拷贝编译后的字节码
    > 变异体增量编译后的字节码如果个原始项目字节码相同的话，则视为等效变异体
7. 生成最初的变异体列表，然后四层过滤
8. 运行测试套件，记录每个变异体测试执行过程中的输出

## 命令参数

分两种，命令行参数和配置文件，前者优先级更高

### 命令行参数
```bash
--basePath=/Path/To/Yout/Distributed/Projects
--mutators=All # MNT,MNR,RFE,.....
--projectType=mvn  # mvn|ant|gradle
--srcPattern=.*/src/main/.*\.java
--srcExcluded=InternalTopologyBuilder.java,SomeOtherClass.java
--buildOutputDirPattern=.*?(build|target)/classes$
--outputDir=/Path/To/Output/Dir
--coveragePath=/Path/To/Coverage/Info.txt
```

### 配置文件参数

```
project.base.path=/Users/username/Projects/projectname
project.src.pattern=.*/src/main/.*\.java
project.src.excluded=InternalTopologyBuilder.java,SomeOtherClass.java
project.type=mvn
project.mutators=MNT,MNR,RFE
# project.build.output.path=target/classes to delete
project.build.output.pattern=.*?(build|target)/classes$

app.output.path=/home/zdc/outputs/xxProject
app.coverage.path=/Path/To/Your/Coverage/Report

# config file only
execution.timeout.seconds=600
http.proxy=http://127.0.0.1:7890
https.proxy=http://127.0.0.1:7890
socks.proxy=socks5://127.0.0.1:7890
```

### 参数说明

- basePath(project.base.path): 待测项目的根目录
- mutators(project.src.pattern): 变异算子列表，支持逗号分隔的多选，如果为 All 则使用全部13个变异算子
- projectType(project.type): 待测项目的构建工具类型，支持 `mvn`、`ant`、`gradle` 三种
- srcPattern(project.src.pattern): 源码文件匹配正则表达式，默认匹配 `src/main` 目录下的 `.java` 文件
- srcExcluded(project.src.excluded):  源码排除列表，逗号分隔的类名列表，用于排除不参与变异测试的类
- buildOutputDirPattern(project.build.output.pattern): 构建输出目录匹配正则表达式，默认匹配 `build/classes` 或 `target/classes` 目录（用于构建后的字节码搜索，kafka等项目有多个模块，多个构建目录，因此采用正则匹配）
- outputDir(app.output.path): Dismute结果输出的根目录
- coveragePath(app.coverage.path): (可选)，覆盖率信息文件路径，如果传入这个参数则开启覆盖率模式

### 输出说明

outputDir(app.output.path) 为输出根目录，主要包含以下子目录

```
classpath.txt: 记录了项目的所有依赖路径，用于增量编译
mutantBytecode/  变异体源码以及编译后的字节码
mutants/  变异体源码
mutantsFiltered/ 变异体经过四层过滤后的源码
original/ 变异体对应的原始源码
originalBytecode/ 原始项目编译后的字节码
testOutputs/ 变异体测试执行的输出
```

### 其他

#### 变异体的命名规则

例如："MessagingService_MNT_1.java"，源代码文件名_变异算子_编号

### 关于测试

本项目包含了单元测试和集成测试，位于 `src/test/java` 目录下。测试覆盖了主要功能模块，包括变异体生成、过滤、执行等。
测试会解压zk或cas或kafka源码，然后生成变异体或运行测试，最后会清理掉
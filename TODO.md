- 关于覆盖率
  - 如果一个测试代码覆盖了被变异的类，就选择运行它
- 关于运行测试
  - maven invoker
- 报告收集
  - 每一份报告包含：
      - 变异后的文件（文件名包含算子名称）
      - 测试输出
- 关于等效变异体
  - Trivial Compiler Equivalence: A Large Scale Empirical Study of a Simple, Fast and Effective Equivalent Mutant Detection Technique
  

## TODO

fix:
- 两两比较变异体的字节码
- (done)RRC删除资源释放语句之后，导致try块为空，虚空catch会编译失败
- (done) zk重新运行RRC
- 源文件比较只需要比较同一源代码
- 改称命令行模式


- upcast方法签名编译失败问题
- (part done)编译器优化，直接比较编译后的字节码
- (done)完善变异体和测试报告收集代码
- (done)测试多个算子协同工作的情况

- 代码clean up & fix
  - (done) 初始化clean之前判断目录
  - (done) 写入变异体文件方法抽象
  - (done) 整理Upcast算子的重复代码
  - project的设计需要修改

- (done)ant脚本编写
- (done)mvn脚本修改

## 变异算子列表

- MST
  - modify socket timeout
  - 修改java.io.Socket方法中，.connect(peer, timeout)中的timeout
    - timeout 除以 10
- MWT
  - modify wait timeout
  - 修改Object.wait(timeout)中的timeout
    - timeout 除以 10
- RBF
  - remove finally block
  - 删除try..catch..finally中的finally块中的内容
- RFC
  - Remove File Existance Checks
  - 删除文件存在性检查
  - if(file.exists()){xxx} -> xxx
- RINT
  - Remove If Null's Throw statements
  - 删除if null检查中的throw语句
  - TODO 语法
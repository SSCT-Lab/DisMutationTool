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
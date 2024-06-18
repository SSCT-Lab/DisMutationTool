# DisMutationTool

 [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Overview

This tool is designed for mutation testing of distributed systems, aiming to ensure robustness and fault tolerance by introducing controlled mutations and observing the system's ability to handle them. This testing approach helps developers identify weaknesses in fault handling and recovery processes.

## Features

- **Automated Mutation Injection:** Automatically introduces faults into the system to test the effectiveness of fault detection and recovery mechanisms.
- **Real-Time Monitoring:** Tracks the system's response to mutations in real time, providing immediate feedback and mutation score calculation.
- **Detailed Reporting:** Generates comprehensive reports detailing the outcomes.

## Getting Started

### Prerequisites

- ubuntu 22.04
- Java 8 or higher
- Apache Maven/Ant/Gradle, depends on target system requirements
- Existing distributed system code and test suite for testing
- (optional) Docker if you want to run test suites in parallel in docker mode

### Installation

1. Clone the repository:

   ```
   git clone https://github.com/SSCT-Lab/DisMutationTool.git
   ```

2. Navigate to the project directory:

   ```
   cd DisMutationTool
   ```

3. Build project

   ```
   mvn clean package
   ```

### Usage

```
java jar DisMutationTool-1.0-SNAPSHOT-jar-with-dependencies.jar.jar [your args, for example: --projectPath="/path/to/target/project"]
```

### Args
- projectPath: path to target project.
- mutators: mutation operators to be used, split in comma, --mutators="RRC,MNT,NCS" for example.
- projectType: type of target project, mvn or ant.
- srcPattern: pattern of source code files.
- buildOutputDir: relative path of build output directory, from where we collect bytecodes and use them to filter equivalent mutants.
- outputDir: output directory for reports.
- (optional)dockerfile: dockerfile path if you want to run test suites in docker mode.
- (optional)projectPathInDocker: path to target project in docker container.

### Notices
- Make sure test suite of target system is GREEN.
- Please ensure you have built your target project in dockerfile, or the project's dependencies can be installed. 

## Contributing

We welcome contributions! If you're interested in improving the tool, please fork the repository and submit a pull request.

## License

This tool is available under the MIT License. See the LICENSE file for more details.

## Contact

For questions and support, please contact <zheyuanlin@smail.nju.edu.cn> .

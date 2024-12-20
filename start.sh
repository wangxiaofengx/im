#!/bin/bash
# 设置终端标题
echo -ne "\033]0;video-conference\007"

SCRIPT_DIR=$(cd "$(dirname "$0")"; pwd)
# 执行 Java 程序
java -Dfile.encoding=UTF-8 -Xms128m -Xmx128m -jar "$SCRIPT_DIR/target/video-conference.jar"
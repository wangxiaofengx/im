#!/bin/bash
# 设置终端标题
echo -ne "\033]0;video-conference\007"

# 执行 Java 程序
java -Xms128m -Xmx128m -jar ./target/video-conference.jar
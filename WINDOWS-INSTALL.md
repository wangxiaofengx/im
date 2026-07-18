# Windows 桌面版打包与使用

## 功能

- 启动桌面程序后自动启动视频会议服务。
- 主界面可设置端口、启动和停止服务，并显示局域网访问地址。
- 点击窗口右上角关闭按钮时，程序隐藏到 Windows 右下角托盘。
- 托盘右键菜单支持打开主界面、启动、停止和退出。
- 端口设置会保存，下次启动继续使用。

## 生成 EXE 安装包

1. 安装 Java 21，并配置 `JAVA_21_HOME`。
2. 安装 WiX Toolset 3.14，并将其目录加入 `PATH`；也可以把免安装版工具解压到 `.tools\wix314`。
3. 双击运行 `z-package-exe.bat`。
4. 安装包输出到 `dist\installer`。

安装包内置精简的 Java 运行时，目标电脑不需要另外安装 Java。默认按当前 Windows 用户安装，通常不需要管理员权限；安装时可以选择目录，完成后可通过桌面快捷方式或开始菜单启动。

免安装版位于 `target\jpackage-app-image\VideoConferenceManager`，可直接运行其中的 `VideoConferenceManager.exe`。`dist` 目录只用于存放最终安装包。

## 普通 JAR 方式

运行 `z-package.bat` 生成 JAR，再运行 `start.bat`。这两个脚本都只使用 `JAVA_21_HOME`，不会修改系统的全局 `JAVA_HOME`。

# mirai_menglei
![Github](https://img.shields.io/badge/Author-MengLei-blue)
![GitHub](https://img.shields.io/github/license/MengLeiFudge/mirai_menglei)
![GitHub repo size](https://img.shields.io/github/repo-size/MengLeiFudge/mirai_menglei)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/MengLeiFudge/mirai_menglei)
![GitHub last commit](https://img.shields.io/github/last-commit/MengLeiFudge/mirai_menglei)

一个简易的mirai(mirai-console)框架应用 包含kotlin或java版(在 `/src/main/java` 或 `/src/main/kotlin`目录下)

> 该bot主要用于Arc查询、Shapez查询等，推荐使用Intellij Idea打开本项目
> 
> [Mirai论坛](https://mirai.mamoe.net)

# 使用方法

## 1.更新项目版本配置文件

> 文件位置：\build.gradle.kts

plugins -> id("net.mamoe.mirai-console") 的版本可以从 [Mirai仓库](https://github.com/mamoe/mirai/releases) 获取最新版本号

dependencies 中各依赖版本可以从 [maven仓库](https://mvnrepository.com/) 获取最新版本号

## 2.配置想要登录的Bot的QQ和密码

> 文件位置：src\test\kotlin\RunMirai.kt

    MiraiConsole.addBot(qq号, "qq密码") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
    }.alsoLogin()

> 注：协议尽量选择 ANDROID_PHONE，可接受事件较多

## 3.确认 fix-protocol/unidbg-fetch-qsign 状态（用于解除code 235/45封禁）

> 以下是一些可能用到的相关链接：
>
> [fix-protocol仓库](https://github.com/cssxsh/fix-protocol-version)
>
> [unidbg-fetch-qsign仓库](https://github.com/fuqiuluo/unidbg-fetch-qsign)
>
> [unidbg-fetch-qsign windows 部署 wiki](https://github.com/fuqiuluo/unidbg-fetch-qsign/wiki/%E9%83%A8%E7%BD%B2%E5%9C%A8Windows)

具体操作步骤如下：

1) 运行项目，查看当前各登录协议版本日期，版本必须与 \KFCFactory.json 中内容保持一致（当前使用QQ协议为8.9.63版本）

2) 如果不一致，在 Mirai 控制台中执行以下指令，更新协议版本至最新

> protocol sync ANDROID_PHONE
> 
> protocol sync ANDROID_PAD

> PS：如果登录失败，程序会直接结束，无法输入指令。此时应先将 RunMirai.kt 中所有登录都注释掉，再重新启动项目，即可输入以上指令

3) 如果一致，使用 WinSW 开启 qsign 服务

以管理员模式打开 cmd，跳转至 qsign 目录（.\plugins\qsign\unidbg-fetch-qsign-1.1.9）

> PS：若之前未注册过 qsign 服务，执行以下命令注册服务（只需注册一次）：
>
> > .\qsign.exe install qsign.xml

执行以下命令启动服务：

> qsign.exe start

关闭 cmd 即可

也可以直接使用 idea 的内置控制台，建议使用绝对路径，例如：

> E:\project\java\miraiBot\mirai_menglei\plugins\qsign\unidbg-fetch-qsign-1.1.9\qsign.exe start
> 
> E:\project\java\miraiBot\mirai_menglei\plugins\qsign\unidbg-fetch-qsign-1.1.9\qsign.exe restart

## 4.重新启动项目，测试能否登录、读取消息
+
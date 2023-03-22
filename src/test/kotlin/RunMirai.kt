package org.example.mirai.plugin

import mirai.core.JavaPluginMain
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.BotConfiguration

@OptIn(ConsoleExperimentalApi::class)
suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    //如果是kotlin
//    PluginMain.load()
//    PluginMain.enable()
    //如果是java
    JavaPluginMain.INSTANCE.load()
    JavaPluginMain.INSTANCE.enable()

    //下面填机器人信息
    MiraiConsole.addBot(605738729, "mlj0909.") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.MACOS
    }
    MiraiConsole.addBot(1443944862, "mlj0909.") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.MACOS
    }
    MiraiConsole.addBot(2629227874, "mlj0909.") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.MACOS
    }.alsoLogin()
    MiraiConsole.addBot(3109326090, "mlj0909.") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.MACOS
    }
    MiraiConsole.addBot(3056830689, "mlj0909.") {
        fileBasedDeviceInfo()
        protocol = BotConfiguration.MiraiProtocol.MACOS
    }

    MiraiConsole.job.join()
}

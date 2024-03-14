package com.yifeplayte.maxfreeform.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * 指令 工具
 */
@Suppress("unused")
object Terminal {
    /**
     * 执行单条指令
     * @param command 指令
     * @return 指令运行输出
     */
    fun exec(command: String): String {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null
        var inputStreamReader: InputStreamReader? = null
        var outputStream: DataOutputStream? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            inputStreamReader = InputStreamReader(process.inputStream)
            bufferedReader = BufferedReader(inputStreamReader)
            outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes(
                command.trimIndent()
            )
            outputStream.writeBytes("\nexit\n")
            outputStream.flush()
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuilder()
            while (bufferedReader.read(buffer).also { read = it } > 0) {
                output.appendRange(buffer, 0, read)
            }
            process.waitFor()
            output.toString()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            try {
                outputStream?.close()
                inputStreamReader?.close()
                bufferedReader?.close()
                process?.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 执行多条指令
     * @param commands 指令
     * @return 指令运行输出
     */
    fun exec(commands: Array<String>): String {
        val stringBuilder = java.lang.StringBuilder()
        for (command in commands) {
            stringBuilder.append(exec(command))
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }

}

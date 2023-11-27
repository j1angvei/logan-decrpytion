package cn.j1angvei.logandecryption

import com.google.gson.GsonBuilder
import java.io.File

class LogFormatter(decryptedFile: File, logFile: File) {

    init {
        val gson = GsonBuilder().create()
        if (logFile.exists()) {
            logFile.delete()
        }
        logFile.createNewFile()
        decryptedFile.forEachLine { line ->
            try {
                val loganProtocol = gson.fromJson(line, LoganProtocol::class.java)
                logFile.appendText(loganProtocol.format() + "\n")
            } catch (e: Throwable) {
                logFile.appendText(line + "\n")
            }
        }
    }
}

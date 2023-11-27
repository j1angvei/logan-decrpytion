package cn.j1angvei.logandecryption

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("usage: java -jar logan-decryption.jar file")
        return
    }
    for (name in args) {
        val file = File(name)
        if (!file.exists()) {
            println("File not exist: \"$name\", exit.")
            continue
        }
        if (file.isFile && file.canRead()) {
            start(file)
        } else if (file.isDirectory && file.canRead()) {
            file.listFiles { pathname -> pathname?.isFile == true }?.forEach { child -> start(child) }
        } else {
            println("Check file permission of $name")
        }
    }
}

private fun start(encryptFile: File) {
    val parent = encryptFile.parent?.let { it + File.separator } ?: ""
    val outputPrefix = parent + encryptFile.nameWithoutExtension
    val decryptionFile = File(outputPrefix + "_json.log")
    LoganDecryption(encryptFile, decryptionFile)
    val logFile = File(outputPrefix + "_plain.log")
    LogFormatter(decryptionFile, logFile)
    println("Decrypt Success : ${logFile.absolutePath}")
}
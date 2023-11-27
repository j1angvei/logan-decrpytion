package cn.j1angvei.logandecryption

import com.google.gson.annotations.SerializedName
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Date

data class LoganProtocol(
    @SerializedName("c", alternate = ["log"]) val content: String,
    @SerializedName("f", alternate = ["type"]) val level: Int,
    @SerializedName("l", alternate = ["ct"]) val timestamp: Long,
    @SerializedName("n", alternate = ["tname"]) val threadName: String,
    @SerializedName("i", alternate = ["tid"]) val threadId: Int,
    @SerializedName("m", alternate = ["main"]) val isMainThread: Boolean
) {

    private fun levelString(): String = when (level) {
        2 -> "VERBOSE"
        3 -> "DEBUG"
        4 -> "INFO"
        5 -> "WARN"
        6 -> "ERROR"
        7 -> "ASSERT"
        else -> "UNKNOWN" // as Unknown
    }

    companion object {
        private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }

    fun format(): String {
        return StringBuilder(sdf.format(Date(timestamp*1000))).append("|").append(threadName).append(",").append(threadId)
            .append("|").append(levelString()).append("|")
            .append(content).toString()
    }

}
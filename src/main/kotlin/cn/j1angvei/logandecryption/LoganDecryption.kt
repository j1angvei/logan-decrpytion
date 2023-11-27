package cn.j1angvei.logandecryption

import com.google.gson.GsonBuilder
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.ArrayUtils
import java.io.*
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class LoganDecryption(encryptedFile: File, decryptedFile: File) {
    companion object {
        private const val ENCRYPT_CONTENT_START: Byte = 1
        private const val AES_NO_PADDING = "AES/CBC/NoPadding"
        private const val AES_WITH_PADDING = "AES/CBC/PKCS5Padding"
    }

    private val configPath by lazy {
        System.getProperty("user.home") + File.separator + ".logan_decryption.json"
    }

    private lateinit var wrap: ByteBuffer
    private lateinit var fos: FileOutputStream

    private val config: DecryptConfig = try {
        val configContent = FileUtils.readFileToString(File(configPath))
        GsonBuilder().create().fromJson(configContent, DecryptConfig::class.java)
    } catch (e: Exception) {
        println("Invalid config file $configPath, use \"0123456789012345\" as secretKey and iv.")
        DecryptConfig()
    }

    init {
        try {
            if (decryptedFile.exists()) {
                decryptedFile.delete()
            }
            wrap = ByteBuffer.wrap(IOUtils.toByteArray(encryptedFile.toURI()))
            fos = FileOutputStream(decryptedFile)
            start()
        } catch (_: IOException) {
        }
    }

    private fun start() {
        while (wrap.hasRemaining()) {
            while (wrap.hasRemaining() && wrap.get() == ENCRYPT_CONTENT_START) {
                val size = wrap.getInt()
                if (size < 0 || size > wrap.remaining()) {
                    continue
                }
                val encrypt = ByteArray(size)
                try {
                    wrap[encrypt]
                    decryptAndAppendFile(encrypt)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun decryptAndAppendFile(encrypt: ByteArray) {
        val secretKeySpec = SecretKeySpec(config.secretKey.toByteArray(), "AES")
        val ivParamSpec = IvParameterSpec(config.iv.toByteArray())
        var cipher: Cipher
        try {
            // 先尝试带 padding 模式解密末尾 16 字节
            cipher = Cipher.getInstance(AES_WITH_PADDING)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParamSpec)
            cipher.doFinal(encrypt, encrypt.size - 16, 16)
        } catch (e: BadPaddingException) {
            // 带 padding 模式解密失败，尝试无 padding 模式
            cipher = Cipher.getInstance(AES_NO_PADDING)
        }
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParamSpec)
        val compressed = cipher.doFinal(encrypt)
        val plainText = decompress(compressed)
        fos.write(plainText)
        fos.flush()
    }

    private fun decompress(contentBytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        try {
            GZIPInputStream(ByteArrayInputStream(contentBytes)).use { gzipInputStream ->
                IOUtils.copy(gzipInputStream, out)
                return out.toByteArray()
            }
        } catch (e: IOException) {
            // 虽然解压抛了异常，但前面已经解出来的内容还是可用的，由于多条日志使用 \n 分割，这里取最后一个 \n 前的内容
            var arr = out.toByteArray()
            val lastIndexOfLf = ArrayUtils.lastIndexOf(arr, '\n'.code.toByte())
            arr = if (lastIndexOfLf < 0) ByteArray(0) else ArrayUtils.subarray(arr, 0, lastIndexOfLf + 1)
            return arr
        }
    }
}
package se.zensum.ktorSentry

import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import kotlin.streams.toList

internal fun hostname(): String? {
    val envVarHostName: String? = System.getenv("HOSTNAME")
    val envVarComputerName: String? = System.getenv("COMPUTERNAME")
    val hostnameFile: String? = readHostnameFile()
    val getHostName: String? = InetAddress.getLocalHost().hostName

    return sequenceOf(envVarHostName, envVarComputerName, hostnameFile, getHostName)
        .filterNot { it.isNullOrBlank() }
        .firstOrNull()
}

private fun readHostnameFile(): String? {
    val hostnameFile = File("/etc/hostname")
    if(!hostnameFile.exists())
        return null
    val lines: List<String> = Files.lines(hostnameFile.toPath()).toList()
    if(lines.isEmpty())
        return null

    return lines.first()
}
package de.logpay

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrintDirectoryStructureTask : DefaultTask() {
    // Define an input property for the directory path
    @Input
    var directoryPath: String = ""

    @TaskAction
    fun execute() {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("The provided path is not a valid directory.")
        }

        printDirectory(directory, "", true)
    }

    // Recursive function to print the directory structure
    private fun printDirectory(file: File, indent: String, isLast: Boolean) {
        if (!file.exists()) {
            return
        }

        // Print the current file/directory name
        println("${indent}${if (isLast) "└── " else "├── "}${file.name}")

        // Recurse into subdirectories
        file.listFiles()?.let { files ->
            val subIndent = indent + if (isLast) "    " else "│   "
            files.forEachIndexed { index, subFile ->
                printDirectory(subFile, subIndent, index == files.size - 1)
            }
        }
    }
}

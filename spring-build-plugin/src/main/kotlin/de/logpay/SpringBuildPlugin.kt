package de.logpay


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import java.io.File
import java.util.*

class SpringBuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("Plugin ${this.javaClass.simpleName} applied on ${project.name}")

        val buildProperties = Properties().apply { load(File("build.properties").inputStream()) }

        println(buildProperties)

        project.tasks.register("my-project-task") { task ->
            task.doLast {
                println("Task executed on ${project.name} at ${(java.util.Date())}")
            }
        }

        project.tasks.register("bumpVersion") { task ->
            task.doLast {
                val oldVersion = project.rootProject.version.toString().replace("\"", "").split('.')
                val major = oldVersion[0].toInt()
                val minor = oldVersion[1].toInt()
                val patch = oldVersion[2].toInt() + 1
                val newVersion = listOf(major, minor, patch).joinToString(".")

                val lines = project.file("version.properties").readLines().map { line ->
                    if (line.trim().startsWith("version")) "version=$newVersion" else line
                }
                project.rootProject.file("version.properties").writeText(lines.joinToString("\n"))
                println("Bumped version to $newVersion")
            }
        }

        project.tasks.register<Copy>("copyDependencies", Copy::class.java) { task ->
            // find from runtimeClasspath (it finds artifacts) first opentelemetry-javaagent file (jar)
            val files = project.configurations.getByName("runtimeClasspath").filter { it.name.contains("opentelemetry-javaagent") }
            task.from(files.first())
            task.into(project.layout.buildDirectory.get().asFile.resolve("../src/main/jib"))
            task.rename { "opentelemetry-javaagent.jar" }
        }

        project.tasks.named("compileJava") { task ->
            task.dependsOn("copyDependencies")
        }

        project.tasks.named("processResources") { task ->
//            // Iterate over each file pattern individually
//            listOf("**/*.properties", "**/*.yml", "**/*.yaml").forEach {
//                filesMatching(it) {
//                    // skip vault file
//                    if (!Regex(".*vault.*").matches(this.name)) {
//                        filter { line ->
//                            line.replace("\${projectVersion}", project.version.toString())
//                        }
//                    }
//                }
//            }

            (task as? Copy)?.let { copyTask ->
                // Define the patterns for files to include in processing
                val includePatterns = setOf("**/*.properties", "**/*.yml", "**/*.yaml", "**/*.xml")
                // Configure each file matching the patterns
                copyTask.filesMatching(includePatterns) { fileCopyDetails ->
                    println("processResource > ${fileCopyDetails.name}")
                    // Exclude specific files by name pattern, for example, containing 'vault'
                    if (!fileCopyDetails.name.contains("vault")) {
                        // Modify the content of the files, replacing a placeholder with the project version
                        fileCopyDetails.filter { line ->
                            line.replace("\${projectVersion}", project.version.toString())
                        }
                    }
                }
            } ?: println("Task 'processResources' is not a Copy task.")
        }


    }
}
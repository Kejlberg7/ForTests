import org.gradle.api.tasks.testing.Test
import org.gradle.internal.impldep.com.google.common.collect.Multiset
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import javax.xml.XMLConstants
import org.w3c.dom.Node

plugins {
    java
    id("jacoco")
}

dependencies {
    // Example dependencies, adjust as needed
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/java")
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

// Assuming tests are in the standard 'src/test/java' directory
val testSourceSet: SourceSet = the<SourceSetContainer>()["test"]

val aggregateTestTask = tasks.register("runAllIndividualTests")
val testMap = mutableMapOf<String, MutableSet<String>>()
testSourceSet.allSource.srcDirs.forEach { srcDir ->
    File(srcDir, "hello").walkTopDown().forEach { file ->
        if (file.isFile && file.name.endsWith("Test.java")) {
            val className = file.nameWithoutExtension
            val taskName = "test$className"
            val updateMapTaskName= "updateMap$className"
            val jacocoFileName = "${project.buildDir}/reports/jacoco/$taskName/jacocoTestReport.xml";
            tasks.create(updateMapTaskName) {
                outputs.upToDateWhen { false }
                doLast{
                    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
                    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                    documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                    documentBuilderFactory.isValidating = false
                    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
                    println(jacocoFileName)
                    val document = documentBuilder.parse(jacocoFileName)
                    val root = document.documentElement

                    val classesWithCoverage = mutableListOf<String>()

                    val classElements = root.getElementsByTagName("class")
                    for (i in 0 until classElements.length) {
                        val classElement = classElements.item(i) as Element
                        val methods = classElement.getElementsByTagName("method")
                        for (j in 0 until methods.length) {
                            val method = methods.item(j) as Element
                            val counters = method.getElementsByTagName("counter")
                            for(k in 0 until counters.length) {
                                val counter = counters.item(k) as Element
                                val covered = counter.getAttribute("covered").toInt()
                                if (covered > 0) {
                                    classesWithCoverage.add(classElement.getAttribute("name"))
                                    break // Found coverage, no need to check further counters for this class
                                }
                            }
                        }
                    }
                    for (i in 0 until classesWithCoverage.size) {
                        val set = testMap[classesWithCoverage[1]];
                        println("Class with coverage: ${classesWithCoverage[i]}")
                        if (set != null) {
                            set.add(className)
                            testMap[classesWithCoverage[i]] = set;
                        } else {
                            val newSet = mutableSetOf<String>()
                            newSet.add(className)
                            testMap[classesWithCoverage[i]] = newSet;
                        }
                    }
                }
            }

            val jacocoTaskName = "jacocoTestReport$className"
            var jacocoTask = tasks.create<JacocoReport>(jacocoTaskName) {
                executionData.setFrom(fileTree("${project.buildDir}").include("/jacoco/$taskName.exec"))
                sourceSets(sourceSets["main"])
                outputs.upToDateWhen { false }
                finalizedBy(updateMapTaskName)
                reports {
                    xml.required.set(true)
                    xml.outputLocation.set(file(jacocoFileName))
                    html.required.set(false)
                }
            }

            val testTask = tasks.create<Test>(taskName) {
                useJUnitPlatform {
                    filter {
                        includeTestsMatching("hello.$className")
                    }
                }
                outputs.upToDateWhen { false }
                group = "coverage"
                testLogging {
                    events("passed", "skipped", "failed")
                }
                finalizedBy(jacocoTask)
                reports {
                    junitXml.required.set(true)
                    junitXml.outputLocation = file("${project.buildDir}/reports/junit/$taskName")
                    html.required.set(false)
                }
            }

            aggregateTestTask.configure {
                dependsOn(testTask)
                group = "coverage"
                doLast{
                    // loop through each value and key set in map and print
                    for ((key, value) in testMap) {
                        println("Class: $key")
                        for (v in value) {
                            println("Test: $v")
                        }
                    }
                }

            }
        }
    }
}


/**
 * Copyright (C) 2017 Jared Burrows, 2018-2020 Martin Kellogg
 *
 * Modifications copyright (C) 2020 Benjamin Kushigian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mutationtesting.major.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * This task generates a manifest file in the style of
 * https://checkerframework.org/manual/#checker-auto-discovery.
 * Once the plugin places this directory containing it on the annotation processor path,
 * javac can find the checkers via annotation processor discovery.
 */
class CreateManifestTask extends DefaultTask {

    private final static String manifestDirectoryName = "checkerframework/META-INF/services/"
    private final static String manifestFileName = "javax.annotation.processing.Processor"

    @Input
    String[] checkers = []

    /**
     * Creates a manifest file listing all the checkers to run.
     */
    @TaskAction
    def generateManifest() {
        def manifestDir = project.mkdir "${project.buildDir}/${manifestDirectoryName}"
        def manifestFile = project.file("${manifestDir.absolutePath}/${manifestFileName}")
        if (!manifestFile.createNewFile()) {
            manifestFile.delete()
            manifestFile.createNewFile()
        }
        manifestFile << this.checkers.join('\n')
    }

    /**
     * Required so that this can incrementalized correctly, and so that `gradle clean build` behaves
     * the same as `gradle clean ; gradle build`.
     */
    @OutputFile
    def getManifestLocation() {
        return "${project.buildDir}/${manifestDirectoryName}/${manifestFileName}"
    }
}

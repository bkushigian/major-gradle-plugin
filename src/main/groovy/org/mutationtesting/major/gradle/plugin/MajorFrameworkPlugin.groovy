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


import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency
import org.gradle.util.GradleVersion
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.compile.AbstractCompile

final class MajorFrameworkPlugin implements Plugin<Project> {
  // Major Framework configurations and dependencies

  // Whenever this line is changed, you need to change all occurrences in README.md.
  private final static def LIBRARY_VERSION = "2.0.0"
  private final static def CONFIGURATION = "majorFramework"
  private final static def CONFIGURATION_DESCRIPTION = "The Major Mutation Framework: mutation analysis for Java."
  private final static def JAVA_COMPILE_CONFIGURATION = "compileOnly"
  private final static def TEST_COMPILE_CONFIGURATION = "testCompileOnly"
  private final static def TEST_RUNTIME_CONFIGURATION = "testRuntimeOnly"
  private final static def MAJOR_DEPENDENCY = "org.mutationtesting:major:${LIBRARY_VERSION}"
  private final static def MAJOR_QUAL_DEPENDENCY = "org.mutationtesting:major:${LIBRARY_VERSION}"

  private final static Logger LOG = Logging.getLogger(MajorFrameworkPlugin)

  /**
   * Configure each task in {@code project} with the given {@code taskType}.
   * <p>
   * We prefer to configure with {@link
   * org.gradle.api.tasks.TaskCollection#configureEach(org.gradle.api.Action)}
   * rather than {@link
   * org.gradle.api.tasks.TaskCollection#all(org.gradle.api.Action)}, but {@code
   * configureEach} is only available on Gradle 4.9 and newer, so this method
   * dynamically picks the better candidate based on the current Gradle version.
   * <p>
   * See also: <a href="https://docs.gradle.org/current/userguide/task_configuration_avoidance.html">
   * Gradle documentation: Task Configuration Avoidance</a>
   */
  private static <S extends Task> void configureTasks(Project project, Class<S> taskType, Action<? super S> configure) {
    // TODO: why does lazy configuration fail on Java 8 JVMs? https://github.com/typetools/checker-framework/pull/3557
    if (GradleVersion.current() < GradleVersion.version("4.9")
            || !JavaVersion.current().isJava9Compatible()) {
      project.tasks.withType(taskType).all configure
    } else {
      project.tasks.withType(taskType).configureEach configure
    }
  }

  @Override void apply(Project project) {
    LOG.info("Applying Major Plugin");
    // Either get an existing Major config, or create a new one if none exists
    MajorFrameworkExtension userConfig = project.extensions.findByType(MajorFrameworkExtension.class)?:
            project.extensions.create("majorFramework", MajorFrameworkExtension)
    boolean applied = false
    project.pluginManager.withPlugin("java") {
        configureProject(project, userConfig)
        applyToProject(project, userConfig)
        applied = true
    }

    if (!applied) {
      // Ensure that dependencies and configurations are available, even if no Java/Android plugins were found,
      // to support configuration in a super project with many Java/Android subprojects.
      configureProject(project, userConfig)
    }

    project.afterEvaluate {
      if (!applied) LOG.warn('No android or java plugins found in the project {}, major compiler options will not be applied.', project.name)
    }
  }

  private static configureProject(Project project, MajorFrameworkExtension userConfig) {

    // Create a map of the correct configurations with dependencies
    def dependencyMap = [
            [name: "${CONFIGURATION}", description: "${CONFIGURATION_DESCRIPTION}"]                : "${MAJOR_DEPENDENCY}",
            [name: "${JAVA_COMPILE_CONFIGURATION}", description: "${CONFIGURATION_DESCRIPTION}"]   : "${MAJOR_QUAL_DEPENDENCY}",
            [name: "${TEST_COMPILE_CONFIGURATION}", description: "${CONFIGURATION_DESCRIPTION}"]   : "${MAJOR_QUAL_DEPENDENCY}",
            [name: "${TEST_RUNTIME_CONFIGURATION}", description: "${CONFIGURATION_DESCRIPTION}"]   : "${MAJOR_QUAL_DEPENDENCY}",
    ]

    // Add the configurations, if they don't exist, so that users can add to them.

    dependencyMap.each { configuration, dependency ->
      L:{
        LOG.info("        (name:$configuration.name, description:$configuration.description)")
        LOG.info("        dependency: $dependency")
        if (!project.configurations.find { it.name == "$configuration.name".toString() }) {
          project.configurations.create(configuration.name) { files ->
            files.description = configuration.descripion
            files.visible = false
          }
        }
      }
    }

    // Immediately before resolving dependencies, add the dependencies to the relevant
    // configurations.
    project.getGradle().addListener(new DependencyResolutionListener() {
      @Override
      void beforeResolve(ResolvableDependencies resolvableDependencies) {
        dependencyMap.each { configuration, dependency ->
          def depGroup = dependency.tokenize(':')[0]
          def depName = dependency.tokenize(':')[1]
          // Only add the dependency if it isn't already present, to avoid overwriting user configuration.
          if (project.configurations."$configuration.name".dependencies.matching({
            if (it instanceof DefaultExternalModuleDependency) {
              it.name == depName && it.group == depGroup
            } else if (it instanceof DefaultSelfResolvingDependency) {
              it.getFiles().any { file ->
                file.toString().endsWith(depName + ".jar")
              }
            } else {
              // not sure what to do in the default case...
              false
            }
          }).isEmpty()) {
            project.configurations."$configuration.name".dependencies.add(
                    project.dependencies.create(dependency))
          }
        };

        // Only attempt to add each dependency once.
        project.getGradle().removeListener(this)
      }

      @Override
      void afterResolve(ResolvableDependencies resolvableDependencies) {}
    })

    configureTasks(project, AbstractCompile, { AbstractCompile compile ->
      // TODO: Fix?
      def ext = compile.extensions.findByName("majorFramework")
      if (ext == null) {
        compile.extensions.create("majorFramework", MajorFrameworkTaskExtension)
      } else if (ext instanceof MajorFrameworkTaskExtension) {
        LOG.debug("Task {} in project {} already has majorFramework added to it;" +
                " make sure you're applying the org.majorframework plugin after the Java plugin", compile.name,
                compile.project)
      } else {
        throw new IllegalStateException("Task " + compile.name + " in project " + compile.project +
            " already has a majorFramework extension, but it's of an incorrect type " + ext.class)
      }
    })
  }

  /**
   * Always call this method rather than using the skipMajorFramework property directly.
   * Allows the user to set the property from the command line instead of in the build file,
   * if desired.
   */
  private static boolean skipMajorFramework(Project project, MajorFrameworkExtension userConfig) {
    if (project.hasProperty('skipMajorFramework')) {
      userConfig.skipMajorFramework = project['skipMajorFramework'] != "false"
    }
    return userConfig.skipMajorFramework
  }

    // Interesting stuff starts!
  private static applyToProject(Project project, MajorFrameworkExtension userConfig) {

    // Apply Major to project
    project.afterEvaluate {
      // Only time Martin knows of that I can modify compiler options

      if (skipMajorFramework(project, userConfig)) {
        LOG.info("skipping the Major Framework because skipMajorFramework property is set")
        return
      }

      // This looks for AbstractCompiler interface. This is safe cuz we checked for java plugin already
      configureTasks(project, AbstractCompile, { AbstractCompile compile ->
        if(userConfig.excludeTests && compile.name.toLowerCase().contains("test")) {
          LOG.info("skipping the Major Framework for task {} because excludeTests property is set", compile.name)
          return
        }
        // Bulk of code I want
        if (compile.hasProperty('options')) {
          compile.options.annotationProcessorPath = compile.options.annotationProcessorPath == null ?
              project.configurations.majorFramework :
              project.configurations.majorFramework.plus(compile.options.annotationProcessorPath)
          // Check whether to use the Error Prone javac

          // When running on Java 9+ code, the Major Framework needs reflective access
          // to some JDK classes. Pass the arguments that make that possible.
          // I'm Commenting the following out for now since I'm not checking jvmVersion anymore, but saving for later
          /*
          if (jvmVersion.isJava9Compatible()) {
            compile.options.forkOptions.jvmArgs += [
                    "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED"
            ]
          }
          */

          // TODO: this adds jvm args:
          //  compile.options.forkOptions.jvmArgs += [
          //                    "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED"

          compile.options.compilerArgs += "-Xplugin:MajorPlugin mutator:ALL"
          compile.options.fork = true
          LOG.info("Compiler args = $compile.options.compilerArgs")
          LOG.info("Compiler options = $compile.options")
        }
      })
    }
  }
}

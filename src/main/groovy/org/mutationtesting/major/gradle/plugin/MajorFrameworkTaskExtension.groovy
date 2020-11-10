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

/**
 * A Gradle extension for Checker Framework configuration for compile tasks.
 */
class MajorFrameworkTaskExtension {
  /**
   * If the Checker Framework should be skipped for this compile task.
   */
  Boolean skipCheckerFramework = false
}

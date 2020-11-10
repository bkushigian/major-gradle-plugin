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

class MajorFrameworkExtension {
  // A list of extra options to pass directly to javac when running Major
  List<String> extraJavacArgs = []

  Boolean excludeTests = false

  // Flag to disable the CF easily, from e.g. the command-line.
  Boolean skipMajorFramework = false
}

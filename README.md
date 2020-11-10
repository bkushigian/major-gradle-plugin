# Gradle Major Framework Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This plugin configures `JavaCompile` tasks to use the [Major Mutation Framework](https://mutation-testing.org) for mutation analysis.

## Download

Add the following to your `build.gradle` file:

```groovy
plugins {
    // Major Framework pluggable type-checking
    id 'org.mutationtesting' version '0.0.1'
}

apply plugin: 'org.mutationtesting'
```

The `org.mutationtesting` plugin modifies existing Java compilation tasks. You
should apply it *after* whatever plugins introduce your Java compilation tasks
(usually the `java` or `java-library` plugin for non-Android builds).


## Java 9+ compatibility

Major doesn't currently build on Java 9+. This is
[an issue][https://github.com/rjust/major/issues/20] in the current Major
repository.


## Credits
This project started as a fork of [a plugin built by
kelloggm](https://github.com/kelloggm/checkerframework-gradle-plugin). Special
thanks to Martin for all the help he provided while I adapted this plugin.


## License

    Copyright (C) 2017 Jared Burrows, 2018-2020 Martin Kellogg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


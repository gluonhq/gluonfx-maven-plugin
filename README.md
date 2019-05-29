# client-gradle-plugin

The Gluon Client plugin for gradle projects leverages GraalVM, OpenJDK and JavaFX 11+, 
by compiling into native code the Java Client application and all its required dependencies, 
so it can directly be executed as a native application on the target platform.


## Getting started

To use the plugin, apply the following steps:

### 1. Apply the plugin

Using the `plugins` DSL, add:


    plugins {
        id 'com.gluonhq.client-gradle-plugin' version '0.0.1'
    }
    
This requires adding the plugin repository to the `settings.gradle` file:

    pluginManagement {
        repositories {
            maven {
                url "https://nexus.gluonhq.com/nexus/content/repositories/releases"
            }
            gradlePluginPortal()
        }
    }
    rootProject.name = ...

Alternatively, you can use the `buildscript` DSL:

    buildscript {
        repositories {
            maven {
                url "https://nexus.gluonhq.com/nexus/content/repositories/releases"
            }
            maven {
                url "https://plugins.gradle.org/m2/"
            }
        }
        dependencies {
            classpath 'com.gluonhq:client-gradle-plugin:0.0.1'
        }
    }
    apply plugin: 'com.gluonhq.client-gradle-plugin'

### 2. Tasks

You can run the regular tasks to build and run your project as a regular VM project:

    ./gradlew clean build
    ./gradlew run
    
Once the project is ready, the plugin has these three main tasks:    

#### `nativeCompile`

This tasks does the AOT compilation. It is a very intensive and lengthy task (several minutes, depending on your project and CPU), 
so it should be called only when the project is ready and runs fine on a VM.

Run:

    ./gradlew clean build nativeCompile

The results will be available at `$buildDir/client/gvm`.

#### `nativeLink`

When the object is created, this task will generate the native executable for the target platform.

Run:

    ./gradlew nativeLink
    
The results will be available at `$buildDir/client/$hostPlatform/$AppName`.
    
#### `nativeBuild`

This task simply combines `nativeCompile` and `nativeLink`.
    
#### `nativeRun`

Runs the executable in the target platform

Run:

    ./gradlew nativeRun
    
Or run the three tasks combined:

    ./gradlew clean build nativeBuild nativeRun
    
Or run directly the application from command line:

    build/client/$hostPlatform/$AppName/$AppName    
    
On Mac OS X it will create a distributable application.

### 3. Specify plugin extensions

Only if you need to, you can modify the default settings, by specifying
 the required extensions that the project uses in your `build.gradle` file:

    gluonClient {
        target = "host"
        backend = "lir"
        graalLibsVersion = "20.0.0-beta.01"
        graalLibsPath = ""
        javaStaticSdkVersion = "13-ea+2"
        javafxStaticSdkVersion = "13-ea+1"
        bundlesList = []
        resourcesList = []
        reflectionList = []
        jniList = []
        delayInitList = []
        releaseSymbolsList = []
    }
    
### Requirements

Set `JAVA_HOME` to JDK 11.

For now, only Mac OS X is supported. Therefore, a Mac with MacOS X 10.13.2 or superior, and Xcode 9.2 or superior, available from the Mac App Store, are required.

Check the [documentation](https://docs.gluonhq.com/client) for more details about the plugin and running the [gradle samples](https://github.com/gluonhq/client-samples/tree/master/Gradle).


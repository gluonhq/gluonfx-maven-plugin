# Gluon Client plugin for Maven

The Gluon Client plugin for maven projects leverages GraalVM, OpenJDK and JavaFX 11+, 
by compiling into native code the Java Client application and all its required dependencies, 
so it can directly be executed as a native application on the target platform.

[![Gluon Nexus](https://img.shields.io/nexus/releases/com.gluonhq/client-maven-plugin?label=Gluon%20Nexus&server=https%3A%2F%2Fnexus.gluonhq.com%2Fnexus)](https://nexus.gluonhq.com/nexus/content/repositories/releases/com/gluonhq/client-maven-plugin/)
[![Travis CI](https://api.travis-ci.org/gluonhq/client-maven-plugin.svg?branch=master)](https://travis-ci.org/gluonhq/client-maven-plugin)
[![BSD-3 license](https://img.shields.io/badge/license-BSD--3-%230778B9.svg)](https://opensource.org/licenses/BSD-3-Clause)

## Getting started

To use the plugin, apply the following steps:

### 1. Apply the plugin

Edit your pom file and add the plugin:

    <plugin>
        <groupId>com.gluonhq</groupId>
        <artifactId>client-maven-plugin</artifactId>
        <version>0.1.22</version>
        <configuration>
            <mainClass>your.mainClass</mainClass>
        </configuration>
    </plugin>
    
    <pluginRepositories>
        <pluginRepository>
            <id>gluon-releases</id>
            <url>https://nexus.gluonhq.com/nexus/content/repositories/releases/</url>
        </pluginRepository>
    </pluginRepositories>

The plugin allows some options that can be set in `configuration`, to modify the default settings, and several goals, to build and run the native application.

### 2. Goals

You can run the regular goals to build and run your project as a regular Java project on the JVM, if you use the `javafx-maven-plugin` plugin:

    mvn clean javafx:run
    
Once the project is ready, the Client plugin has these main goals:    

#### `client:compile`

This goal does the AOT compilation. It is a very intensive and lengthy task (several minutes, depending on your project and CPU), so it should be called only when the project is ready and runs fine on a VM.

Run:

    mvn client:compile

The results will be available at `target/client/gvm`.

#### `client:link`

When the project is compiled for the target platform, this goal will generate the native executable.

Run:

    mvn client:link
    
The results will be available at `target/client/$targetPlatform/$AppName`.
    
#### `client:build`

This goal simply combines `client:compile` and `client:link`.
    
#### `client:run`

Runs the executable in the target platform.

Run:

    mvn client:run
    
Or run directly the application from command line:

    target/client/$targetPlatform/$AppName/$AppName    
    
It will create a distributable native application.

#### `client:package`

Create a package of the executable in the target platform.

Run:

    mvn client:package
    
For instance, on iOS, this can be used to create an IPA.

## Requirements

At this moment the plugin is in beta, and supports Linux, Mac OS X and iOS platforms for now.

To use the plugin to develop and deploy native applications on Mac or iOS platforms, you need a Mac with MacOS X 10.13.2 or superior, and Xcode 9.2 or superior, available from the Mac App Store. Once Xcode is downloaded and installed, open it and accept the license terms.

For now, only JDK 11 is supported. Any JDK 11 distribution that doesn't bundle JavaFX is valid, like:

- [OpenJDK 11.0.2](https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_osx-x64_bin.tar.gz)

- [AdoptOpenJDK 11.0.3](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.3%2B7/OpenJDK11U-jdk_x64_mac_hotspot_11.0.3_7.tar.gz)

Once downloaded and installed, set `JAVA_HOME` pointing to that JDK 11.

Check the [documentation](https://docs.gluonhq.com/client) for more details about the plugin and running the [maven samples](https://github.com/gluonhq/client-samples/tree/master/Maven).

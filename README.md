# The Gluon Client plugin for Maven

The Gluon Client plugin for maven projects leverages GraalVM, OpenJDK and JavaFX 11+, 
by compiling into native code the Java Client application and all its required dependencies, 
so it can directly be executed as a native application on the target platform.

## Getting started

To use the plugin, apply the following steps:

### 1. Apply the plugin

Edit your pom file and add the plugin:

    <plugin>
    
        <groupId>com.gluonhq</groupId>
        <artifactId>client-maven-plugin</artifactId>
        <version>0.0.1</version>
        <configuration>
            <mainClass>your.mainClass</mainClass>
        </configuration>
    </plugin>

The plugin allows some options that can be set in `configuration`, to modify the default settings, and several goals, to build and run the native application.

### 2. Goals

You can run the regular goals to build and run your project as a regular Java project on the JVM, if you use the `javafx-maven-plugin` plugin:

    mvn clean javafx:run
    
Once the project is ready, the Client plugin has these main goals:    

#### `client:compile`

This goal does the AOT compilation. It is a very intensive and lengthy task (several minutes, depending on your project and CPU), so it should be called only when the project is ready and runs fine on a VM.

Run:

    mvn clean client:compile

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
    
On Mac OS X it will create a distributable application.

## Requirements

At this moment the plugin is in beta, and only supports Mac OS X for now.

# Gluon Client plugin for Maven

The Gluon Client plugin for maven projects leverages GraalVM, OpenJDK and JavaFX 11+, 
by compiling into native code the Java Client application and all its required dependencies, 
so it can directly be executed as a native application on the target platform.

[![Maven Central](https://img.shields.io/maven-central/v/com.gluonhq/client-maven-plugin)](https://search.maven.org/search?q=g:com.gluonhq%20AND%20a:client-maven-plugin)
[![Travis CI](https://api.travis-ci.org/gluonhq/client-maven-plugin.svg?branch=master)](https://travis-ci.org/gluonhq/client-maven-plugin)
[![BSD-3 license](https://img.shields.io/badge/license-BSD--3-%230778B9.svg)](https://opensource.org/licenses/BSD-3-Clause)

## Requirements

### Mac OS X and iOS

* Download the latest release version of GraalVM: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0 by choosing `graalvm-ce-java11-darwin-amd64-20.2.0.tar.gz` from the list of assets and unpack it to a preferred location on your system (e.g. in `/opt`)

* Configure the runtime environment. Set `GRAALVM_HOME` environment variable to the GraalVM installation directory.

  For example:

      export GRAALVM_HOME=/opt/graalvm-ce-java11-20.2.0/Contents/Home

* Set `JAVA_HOME` to point to the GraalVM installation directory:

      export JAVA_HOME=$GRAALVM_HOME

#### Additional requirements

* iOS can be built only on Mac OS X

* Xcode 11+ is required to build for iOS 13+. Install `Xcode` from the [Mac App Store](https://apps.apple.com/us/app/xcode/id497799835?mt=12) if you haven't already. 

* Install `Homebrew`, if you haven't already. Please refer to https://brew.sh/ for more information.

* Install `libusbmuxd`

  Using `brew`:

      brew install --HEAD libusbmuxd

* Install `libimobiledevice`

  Using `brew`:

      brew install --HEAD libimobiledevice

### Linux and Android

* Download the latest release version of GraalVM: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0 by choosing `graalvm-ce-java11-linux-amd64-20.2.0.tar.gz` from the list of assets and unpack it to a preferred location on your system (e.g. in `/opt`)

* Configure the runtime environment. Set `GRAALVM_HOME` environment variable to the GraalVM installation directory.

  For example:

      export GRAALVM_HOME=/opt/graalvm-ce-java11-20.2.0

* Set `JAVA_HOME` to point to the GraalVM installation directory:

      export JAVA_HOME=$GRAALVM_HOME

#### Additional requirements

* Android can be built only on Linux OS

The client plugin will download the Android SDK and install the required packages. 

Alternatively, you can define a custom location to the Android SDK by setting the `ANDROID_SDK` environment variable, making sure that you have installed all the packages from the following list:

* platforms;android-28
* platform-tools
* build-tools;29.0.2
* extras;android;m2repository
* extras;google;m2repository
* ndk-bundle (in case you opt to skip this bundle and download Android NDK package separately, set the `ANDROID_NDK` environment variable to its location)

### Windows

* Download the latest release version of GraalVM: https://github.com/graalvm/graalvm-ce-builds/releases/tag/vm-20.2.0 by choosing `graalvm-ce-java11-windows-amd64-20.2.0.zip` from the list of assets and unzip it to a preferred location on your system.

* Make sure you have installed Visual Studio 2019 with the following components:
  - Choose the English Language Pack
  - C++/CLI support for v142 build tools (14.25 or later)
  - MSVC v142 - VS 2019 C++ x64/x86 build tools (v14.25 or later)
  - Windows Universal CRT SDK
  - Windows 10 SDK (10.0.19041.0 or later)

* Run the maven commands mentioned below in a `x64 Native Tools Command Prompt for VS 2019`. This command prompt can be accessed
from the start menu.

* Configure the runtime environment. Set `GRAALVM_HOME` environment variable to the GraalVM installation directory.

  For example:

      set GRAALVM_HOME=C:\tools\graalvm-ce-java11-20.2.0

* Set `JAVA_HOME` to point to the GraalVM installation directory:

      set JAVA_HOME=%GRAALVM_HOME%

## Getting started

To use the plugin, apply the following steps:

### 1. Apply the plugin

Edit your pom file and add the plugin:

    <plugin>
        <groupId>com.gluonhq</groupId>
        <artifactId>client-maven-plugin</artifactId>
        <version>0.1.36</version>
        <configuration>
            <mainClass>your.mainClass</mainClass>
        </configuration>
    </plugin>
    
The plugin allows some options that can be set in `configuration`, to modify the default settings, and several goals, to build and run the native application.

### 2. Goals

#### Desktop

The following goals apply to Linux, Mac OS X and Windows.

To build the native image:

    mvn clean client:build

To run the native image:

    mvn client:run

or simply run the native executable found in `target/client`.

#### iOS

Set the target to `ios` (for iOS devices) in the `pom.xml`:

```
<artifactId>client-maven-plugin</artifactId>
<configuration>
    <target>ios</target>
    <mainClass>${mainClassName}</mainClass>
</configuration>
```

Build the native image:

```
mvn clean client:build
```

**Note**: Since all java bytecode is translated to native code, the compilation step can take a long time, and it requires a fair amount of memory.

Run the app on the connected iOS device:

```
mvn client:run
```

Or package and create an IPA file to submit to TestFlight or to the App Store:

```
mvn client:package
```

**Note**: In order to deploy apps to an iOS device, you need a valid iOS provisioning profile, as explained in the [documentation](https://docs.gluonhq.com/client/#_ios_deployment).


#### Android

Set the target to `android` (for android devices) in `pom.xml`:

```
<artifactId>client-maven-plugin</artifactId>
<configuration>
    <target>android</target>
    <mainClass>${mainClassName}</mainClass>
</configuration>
```

Build the native image:

```
mvn clean client:build
```

**Note**: Since all java bytecode is translated to native code, the compilation step can take a long time, and it requires a fair amount of memory.

Package and create an APK file:

```
mvn client:package
```

Install the APK file on a connected Android device:

```
mvn client:install
```

Run the installed app on the connected Android device:

```
mvn client:run
```

## Documentation and samples

Check the [documentation](https://docs.gluonhq.com/client) for more details about the plugin and running the [samples](https://github.com/gluonhq/gluon-samples/).

## Issues and Contributions ##

Issues can be reported to the [Issue tracker](https://github.com/gluonhq/client-maven-plugin/issues)

Contributions can be submitted via [Pull requests](https://github.com/gluonhq/client-maven-plugin/pulls), 
providing you have signed the [Gluon Individual Contributor License Agreement (CLA)](https://docs.google.com/forms/d/16aoFTmzs8lZTfiyrEm8YgMqMYaGQl0J8wA0VJE2LCCY).

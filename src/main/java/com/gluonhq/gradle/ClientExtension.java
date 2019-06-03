/*
 * Copyright (c) 2019, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.gradle;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class ClientExtension {

    private static final String DEFAULT_GRAAL_LIBS_VERSION = "20.0.0-beta.01";
    private static final String DEFAULT_JAVA_STATIC_SDK_VERSION = "13-ea+2";
    private static final String DEFAULT_JAVAFX_STATIC_SDK_VERSION = "13-ea+3";
    private static final String DEFAULT_TARGET = "host";
    private static final String DEFAULT_BACKEND = "lir";

    /**
     * Defines the target platform. Default is host, which refers to the platform
     * that currently hosts the process (macosx or linux), but could be also set to
     * ios (either simulator or device), if the host is a Mac.
     * Default is "host"
     */
    private String target;

    /**
     * Defines the intermediate representation.
     * It can be set to LLVM or LIR.
     * Default is "LIR"
     */
    private String backend;

    /**
     * List of additional full qualified bundle resources that will be added to
     * the default bundles list, that already includes:
     * - "com/sun/javafx/scene/control/skin/resources/controls",
     * - "com.sun.javafx.tk.quantum.QuantumMessagesBundle"
     */
    private final List<String> bundlesList;

    /**
     * List of additional resource patterns or extensions that will be added
     * to the default resource list that already includes:
     * - png, gif, jpg, jpeg, bmp,
     * - ttf, css, fxml, json
     * - frag, gls, license
     */
    private final List<String> resourcesList;

    /**
     * List of additional full qualified classes that will be added to the default
     * reflection list, that already includes most of the JavaFX classes.
     */
    private final List<String> reflectionList;

    /**
     * List of additional full qualified classes that will be added to the default
     * jni list, that already includes most of the JavaFX classes.
     */
    private final List<String> jniList;

    /**
     * List of additional full qualified classes that will be added to the default
     * delayed initialization list
     */
    private final List<String> delayInitList;

    /**
     * List of additional JNI functions that will be added to the default
     * release symbols list, that already includes most of the JNI methods.
     */
    private final List<String> releaseSymbolsList;

    /**
     * List of additional runtime arguments that could be required to run the
     * application
     */
    private final List<String> runtimeArgsList;

    /**
     * The Java static SDK version
     */
    private String javaStaticSdkVersion;

    /**
     * The JavaFX static SDK version
     */
    private String javafxStaticSdkVersion;

    /**
     * The Graal libs version
     */
    private String graalLibsVersion;

    /**
     * The omega dependencies directory.
     * By default it is set to $userHome/.gluon/omega/graalLibs/$version/bundle/lib)
     */
    private String graalLibsPath;

    /**
     * Enables hash checking to verify integrity of Graal and Java/JavaFX files
     * By default is true
     */
    private boolean enableCheckHash;

    /**
     * Enables verbose output
     * By default is false
     */
    private boolean verbose;

    public ClientExtension(Project project) {
        this.graalLibsVersion = DEFAULT_GRAAL_LIBS_VERSION;
        this.javaStaticSdkVersion = DEFAULT_JAVA_STATIC_SDK_VERSION;
        this.javafxStaticSdkVersion = DEFAULT_JAVAFX_STATIC_SDK_VERSION;
        this.target = DEFAULT_TARGET;
        this.backend = DEFAULT_BACKEND;
        this.bundlesList = new ArrayList<>();
        this.resourcesList = new ArrayList<>();
        this.reflectionList = new ArrayList<>();
        this.jniList = new ArrayList<>();
        this.delayInitList = new ArrayList<>();
        this.runtimeArgsList = new ArrayList<>();
        this.releaseSymbolsList = new ArrayList<>();

        this.enableCheckHash = true;
    }

    public String getGraalLibsVersion() {
        return graalLibsVersion;
    }

    public void setGraalLibsVersion(String graalLibsVersion) {
        this.graalLibsVersion = graalLibsVersion;
    }

    public String getGraalLibsPath() {
        return graalLibsPath;
    }

    public void setGraalLibsPath(String graalLibsPath) {
        this.graalLibsPath = graalLibsPath;
    }

    public String getJavaStaticSdkVersion() {
        return javaStaticSdkVersion;
    }

    public void setJavaStaticSdkVersion(String javaStaticSdkVersion) {
        this.javaStaticSdkVersion = javaStaticSdkVersion;
    }

    public String getJavafxStaticSdkVersion() {
        return javafxStaticSdkVersion;
    }

    public void setJavafxStaticSdkVersion(String javafxStaticSdkVersion) {
        this.javafxStaticSdkVersion = javafxStaticSdkVersion;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public List<String> getBundlesList() {
        return bundlesList;
    }

    public void setBundlesList(List<String> bundlesList) {
        this.bundlesList.clear();
        this.bundlesList.addAll(bundlesList);
    }

    public List<String> getResourcesList() {
        return resourcesList;
    }

    public void setResourcesList(List<String> resourcesList) {
        this.resourcesList.clear();
        this.resourcesList.addAll(resourcesList);
    }

    public List<String> getReflectionList() {
        return reflectionList;
    }

    public void setReflectionList(List<String> reflectionList) {
        this.reflectionList.clear();
        this.reflectionList.addAll(reflectionList);
    }

    public void setJniList(List<String> jniList) {
        this.jniList.clear();
        this.jniList.addAll(jniList);
    }

    public List<String> getJniList() {
        return jniList;
    }


    public List<String> getDelayInitList() {
        return delayInitList;
    }

    public void setDelayInitList(List<String> delayInitList) {
        this.delayInitList.clear();
        this.delayInitList.addAll(delayInitList);
    }

    public List<String> getRuntimeArgsList() {
        return runtimeArgsList;
    }

    public void setRuntimeArgsList(List<String> runtimeArgsList) {
        this.runtimeArgsList.clear();
        this.runtimeArgsList.addAll(runtimeArgsList);
    }

    public List<String> getReleaseSymbolsList() {
        return releaseSymbolsList;
    }

    public void setReleaseSymbolsList(List<String> releaseSymbolsList) {
        this.releaseSymbolsList.clear();
        this.releaseSymbolsList.addAll(releaseSymbolsList);
    }

    public boolean isEnableCheckHash() {
        return enableCheckHash;
    }

    public void setEnableCheckHash(boolean enableCheckHash) {
        this.enableCheckHash = enableCheckHash;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

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

package com.gluonhq;

import com.gluonhq.omega.Configuration;
import com.gluonhq.omega.model.TargetTriplet;
import com.gluonhq.omega.util.Constants;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class NativeBaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Component
    BuildPluginManager pluginManager;

    @Parameter(readonly = true, required = true, defaultValue = "${basedir}")
    File basedir;

    @Parameter(property = "client.graalLibsPath")
    String graalLibsPath;

    @Parameter(property = "client.llcPath")
    String llcPath;

    @Parameter(property = "client.graalLibsVersion", defaultValue = "20.0.0-ea+10")
    String graalLibsVersion;

    @Parameter(property = "client.javaStaticSdkVersion", defaultValue = "11-ea+6")
    String javaStaticSdkVersion;

    @Parameter(property = "client.javafxStaticSdkVersion", defaultValue = "13-ea+7")
    String javafxStaticSdkVersion;

    @Parameter(property = "client.target", defaultValue = "host")
    String target;

    @Parameter(property = "client.backend")
    String backend;

    @Parameter(property = "client.bundlesList")
    List<String> bundlesList;

    @Parameter(property = "client.resourcesList")
    List<String> resourcesList;

    @Parameter(property = "client.reflectionList")
    List<String> reflectionList;

    @Parameter(property = "client.jniList")
    List<String> jniList;

    @Parameter(property = "client.delayInitList")
    List<String> delayInitList;

    @Parameter(property = "client.runtimeArgsList")
    List<String> runtimeArgsList;

    @Parameter(property = "client.releaseSymbolsList")
    List<String> releaseSymbolsList;

    @Parameter(readonly = true, required = true, defaultValue = "${project.build.directory}/client")
    File outputDir;

    @Parameter(property = "client.mainClass", required = true)
    String mainClass;

    @Parameter(property = "client.executable", defaultValue = "java")
    String executable;

    @Parameter(property = "client.enableCheckHash", defaultValue = "true")
    String enableCheckHash;

    @Parameter(property = "client.useJNI", defaultValue = "true")
    String useJNI;

    @Parameter(property = "client.verbose", defaultValue = "false")
    String verbose;

    private ProcessDestroyer processDestroyer;

    Configuration clientConfig;

    public void execute() throws MojoExecutionException {
        configOmega();
    }

    private void configOmega() {
        clientConfig = new Configuration();
        clientConfig.setGraalLibsVersion(graalLibsVersion);
        clientConfig.setJavaStaticSdkVersion(javaStaticSdkVersion);
        clientConfig.setJavafxStaticSdkVersion(javafxStaticSdkVersion);

        String osname = System.getProperty("os.name", "Mac OS X").toLowerCase(Locale.ROOT);
        TargetTriplet hostTriplet;
        if (osname.contains("mac")) {
            hostTriplet = new TargetTriplet(Constants.AMD64_ARCH, Constants.HOST_MAC, Constants.TARGET_MAC);
        } else if (osname.contains("nux")) {
            hostTriplet = new TargetTriplet(Constants.AMD64_ARCH, Constants.HOST_LINUX, Constants.TARGET_LINUX);
        } else {
            throw new RuntimeException("OS " + osname + " not supported");
        }
        clientConfig.setHost(hostTriplet);

        TargetTriplet targetTriplet = null;
        switch (target) {
            case Constants.TARGET_HOST:
                if (osname.contains("mac")) {
                    targetTriplet = new TargetTriplet(Constants.AMD64_ARCH, Constants.HOST_MAC, Constants.TARGET_MAC);
                } else if (osname.contains("nux")) {
                    targetTriplet = new TargetTriplet(Constants.AMD64_ARCH, Constants.HOST_LINUX, Constants.TARGET_LINUX);
                }
                break;
            case Constants.TARGET_IOS:
                targetTriplet = new TargetTriplet(Constants.ARM64_ARCH, Constants.HOST_MAC, Constants.TARGET_IOS);
                break;
            case Constants.TARGET_IOS_SIM:
                targetTriplet = new TargetTriplet(Constants.AMD64_ARCH, Constants.HOST_MAC, Constants.TARGET_IOS);
                break;
            default:
                throw new RuntimeException("No valid target found for " + target);
        }
        clientConfig.setTarget(targetTriplet);

        if (backend != null && ! backend.isEmpty()) {
            clientConfig.setBackend(backend.toLowerCase(Locale.ROOT));
        }
        clientConfig.setBundlesList(bundlesList);
        clientConfig.setResourcesList(resourcesList);
        clientConfig.setDelayInitList(delayInitList);
        clientConfig.setJniList(jniList);
        clientConfig.setReflectionList(reflectionList);
        clientConfig.setRuntimeArgsList(runtimeArgsList);
        clientConfig.setReleaseSymbolsList(releaseSymbolsList);

        clientConfig.setMainClassName(mainClass);
        clientConfig.setAppName(project.getName());

        List<File> classPath = getCompileClasspathElements(project);
        clientConfig.setUseJavaFX(classPath.stream().anyMatch(f -> f.getName().contains("javafx")));
        clientConfig.setGraalLibsUserPath(graalLibsPath);

        clientConfig.setLlcPath(llcPath);
        clientConfig.setEnableCheckHash("true".equals(enableCheckHash));
        clientConfig.setUseJNI("true".equals(useJNI));
        clientConfig.setVerbose("true".equals(verbose));
    }

    ProcessDestroyer getProcessDestroyer() {
        if (processDestroyer == null) {
            processDestroyer = new ShutdownHookProcessDestroyer();
        }
        return processDestroyer;
    }

    List<File> getCompileClasspathElements(MavenProject project) {
        List<File> list = project.getArtifacts().stream()
                .sorted((a1, a2) -> {
                    int compare = a1.compareTo(a2);
                    if (compare == 0) {
                        // give precedence to classifiers
                        return a1.hasClassifier() ? 1 : (a2.hasClassifier() ? -1 : 0);
                    }
                    return compare;
                })
                .map(Artifact::getFile)
                .collect(Collectors.toList());
        list.add(0, new File(project.getBuild().getOutputDirectory()));
        return list;
    }
}

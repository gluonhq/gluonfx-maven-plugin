/*
 * Copyright (c) 2019, 2020, Gluon
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

import com.gluonhq.attach.AttachArtifactResolver;
import com.gluonhq.substrate.Constants;
import com.gluonhq.substrate.ProjectConfiguration;
import com.gluonhq.substrate.model.IosSigningConfiguration;
import com.gluonhq.substrate.model.Triplet;
import com.gluonhq.utils.MavenArtifactResolver;
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
import org.eclipse.aether.artifact.DefaultArtifact;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class NativeBaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Component
    BuildPluginManager pluginManager;

    @Parameter(readonly = true, required = true, defaultValue = "${basedir}")
    File basedir;

    @Parameter(property = "client.graalvmHome")
    String graalvmHome;

    @Parameter(property = "client.javaStaticSdkVersion")
    String javaStaticSdkVersion;

    @Parameter(property = "client.javafxStaticSdkVersion")
    String javafxStaticSdkVersion;

    @Parameter(property = "client.target", defaultValue = "host")
    String target;

    @Parameter(property = "client.bundlesList")
    List<String> bundlesList;

    @Parameter(property = "client.resourcesList")
    List<String> resourcesList;

    @Parameter(property = "client.reflectionList")
    List<String> reflectionList;

    @Parameter(property = "client.jniList")
    List<String> jniList;

    @Parameter(property = "client.nativeImageArgs")
    List<String> nativeImageArgs;

    @Parameter(readonly = true, required = true, defaultValue = "${project.build.directory}/client")
    File outputDir;

    @Parameter(property = "client.mainClass", required = true)
    String mainClass;

    @Parameter(property = "client.executable", defaultValue = "java")
    String executable;

    @Parameter(property = "client.verbose", defaultValue = "false")
    String verbose;

    @Parameter(property = "client.attachList")
    List<String> attachList;

    @Parameter(property = "client.IOSSigningIdentity")
    String IOSSigningIdentity;

    @Parameter(property = "client.IOSProvisioningProfile")
    String IOSProvisioningProfile;

    @Parameter(property = "client.IOSSkipSigning")
    String IOSSkipSigning;

    private ProcessDestroyer processDestroyer;

    ProjectConfiguration clientConfig;

    public void execute() throws MojoExecutionException {
        if (!getGraalvmHome().isPresent()) {
            throw new MojoExecutionException("GraalVM installation directory not found." +
                    " Either set GRAALVM_HOME as an environment variable or" +
                    " set graalvmHome in client-plugin configuration");
        }
        configSubstrate();
    }

    private void configSubstrate() {
        clientConfig = new ProjectConfiguration(mainClass);
        clientConfig.setGraalPath(Path.of(getGraalvmHome().get()));
        clientConfig.setJavaStaticSdkVersion(javaStaticSdkVersion);
        clientConfig.setJavafxStaticSdkVersion(javafxStaticSdkVersion);

        Triplet targetTriplet;
        switch (target) {
            case Constants.PROFILE_HOST:
                targetTriplet = Triplet.fromCurrentOS();
                break;
            case Constants.PROFILE_IOS:
                targetTriplet = new Triplet(Constants.Profile.IOS);
                IosSigningConfiguration signingConfiguration = new IosSigningConfiguration();
                signingConfiguration.setProvidedSigningIdentity(IOSSigningIdentity);
                signingConfiguration.setProvidedProvisioningProfile(IOSProvisioningProfile);
                signingConfiguration.setSkipSigning(IOSSkipSigning != null && "true".equals(IOSSkipSigning));
                clientConfig.setIosSigningConfiguration(signingConfiguration);
                break;
            case Constants.PROFILE_IOS_SIM:
                targetTriplet = new Triplet(Constants.Profile.IOS_SIM);
                break;
            case Constants.PROFILE_ANDROID:
                targetTriplet = new Triplet(Constants.Profile.ANDROID);
            case Constants.PROFILE_LINUX_AARCH64:
                targetTriplet = new Triplet(Constants.Profile.LINUX_AARCH64);
                break;
            default:
                throw new RuntimeException("No valid target found for " + target);
        }
        clientConfig.setTarget(targetTriplet);

        clientConfig.setBundlesList(bundlesList);
        clientConfig.setResourcesList(resourcesList);
        clientConfig.setJniList(jniList);
        clientConfig.setCompilerArgs(nativeImageArgs);
        clientConfig.setReflectionList(reflectionList);
        clientConfig.setAppName(project.getName());
        clientConfig.setVerbose("true".equals(verbose));
    }

    ProcessDestroyer getProcessDestroyer() {
        if (processDestroyer == null) {
            processDestroyer = new ShutdownHookProcessDestroyer();
        }
        return processDestroyer;
    }

    String getProjectClasspath() {
        List<File> classPath = getClasspathElements(project);
        getLog().debug("classPath = " + classPath);
        return classPath.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private List<File> getClasspathElements(MavenProject project) {
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

        getRuntimeDependencies().stream()
                .filter(d -> !list.contains(d))
                .forEach(list::add);

        return list;
    }

    List<Artifact> getAttachDependencies() {
        Map<String, Artifact> attachMap = AttachArtifactResolver.findArtifactsForTarget(project.getDependencies(), project.getRepositories(), target);
        if (attachList != null) {
            return attachList.stream()
                .map(attachMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<File> getRuntimeDependencies() {
        final MavenArtifactResolver resolver = new MavenArtifactResolver(project.getRepositories());
        return project.getDependencies().stream()
                .filter(d -> "runtime".equals(d.getScope()))
                .map(d -> new DefaultArtifact(d.getGroupId(), d.getArtifactId(),
                        d.getClassifier(), d.getType(), d.getVersion()))
                .flatMap(a -> {
                    Set<Artifact> resolve = resolver.resolve(a);
                    if (resolve == null) {
                        return Stream.empty();
                    }
                    return resolve.stream();
                })
                .distinct()
                .map(Artifact::getFile)
                .collect(Collectors.toList());
    }

    private Optional<String> getGraalvmHome() {
        if (graalvmHome != null) {
            return Optional.of(graalvmHome);
        } else if (System.getenv("GRAALVM_HOME") != null) {
            return Optional.of(System.getenv("GRAALVM_HOME"));
        }
        return Optional.empty();
    }
}

/*
 * Copyright (c) 2019, 2024, Gluon
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
import com.gluonhq.substrate.SubstrateDispatcher;
import com.gluonhq.substrate.model.Triplet;
import com.gluonhq.substrate.target.WebTargetConfiguration;
import com.gluonhq.utils.MavenArtifactResolver;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gluonhq.attach.AttachArtifactResolver.DEPENDENCY_GROUP;
import static com.gluonhq.attach.AttachArtifactResolver.UTIL_ARTIFACT;

public abstract class NativeBaseMojo extends AbstractMojo {

    private static final List<String> ALLOWED_DEPENDENCY_TYPES = Collections.singletonList("jar");

    Path outputDir;

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject project;

    /**
     * The entry point to Maven Artifact Resolver
     */
    @Component
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Component
    BuildPluginManager pluginManager;

    @Component
    private RuntimeInformation runtimeInformation;

    @Parameter(readonly = true, required = true, defaultValue = "${basedir}")
    File basedir;

    @Parameter(property = "gluonfx.graalvmHome")
    String graalvmHome;

    @Parameter(property = "gluonfx.javaStaticSdkVersion")
    String javaStaticSdkVersion;

    @Parameter(property = "gluonfx.javafxStaticSdkVersion")
    String javafxStaticSdkVersion;

    @Parameter(property = "gluonfx.target", defaultValue = "host")
    String target;

    @Parameter(property = "gluonfx.bundlesList")
    List<String> bundlesList;

    @Parameter(property = "gluonfx.resourcesList")
    List<String> resourcesList;

    @Parameter(property = "gluonfx.reflectionList")
    List<String> reflectionList;

    @Parameter(property = "gluonfx.jniList")
    List<String> jniList;

    @Parameter(property = "gluonfx.nativeImageArgs")
    List<String> nativeImageArgs;

    @Parameter(property = "gluonfx.linkerArgs")
    List<String> linkerArgs;

    @Parameter(property = "gluonfx.runtimeArgs")
    List<String> runtimeArgs;

    @Parameter(property = "gluonfx.mainClass", required = true)
    String mainClass;

    @Parameter(property = "gluonfx.executable", defaultValue = "java")
    String executable;

    @Parameter(property = "gluonfx.verbose", defaultValue = "false")
    String verbose;

    @Parameter(property = "gluonfx.attachList")
    List<String> attachList;

    @Parameter(property = "gluonfx.enableSWRendering", defaultValue = "false")
    String enableSWRendering;

    @Parameter(property = "gluonfx.remoteHostName")
    String remoteHostName;

    @Parameter(property = "gluonfx.remoteDir")
    String remoteDir;

    @Parameter(property = "gluonfx.appIdentifier")
    String appIdentifier;

    @Parameter(property = "gluonfx.releaseConfiguration")
    ReleaseConfiguration releaseConfiguration;

    private ProcessDestroyer processDestroyer;

    public SubstrateDispatcher createSubstrateDispatcher() throws IOException, MojoExecutionException {
        if (getGraalvmHome().isEmpty()) {
            throw new MojoExecutionException("GraalVM installation directory not found." +
                    " Either set GRAALVM_HOME as an environment variable or" +
                    " set graalvmHome in gluonfx-plugin configuration");
        }
        outputDir = Path.of(project.getBuild().getDirectory(), Constants.GLUONFX_PATH);
        ProjectConfiguration substrateConfiguration = createSubstrateConfiguration();
        return new SubstrateDispatcher(outputDir, substrateConfiguration);
    }

    private ProjectConfiguration createSubstrateConfiguration() {
        ProjectConfiguration clientConfig = new ProjectConfiguration(mainClass, getProjectClasspath());

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
                break;
            case Constants.PROFILE_IOS_SIM:
                targetTriplet = new Triplet(Constants.Profile.IOS_SIM);
                break;
            case Constants.PROFILE_ANDROID:
                targetTriplet = new Triplet(Constants.Profile.ANDROID);
                break;
            case Constants.PROFILE_LINUX_AARCH64:
                targetTriplet = new Triplet(Constants.Profile.LINUX_AARCH64);
                break;
            case Constants.PROFILE_WEB:
                targetTriplet = new Triplet(Constants.Profile.WEB);
                break;
            default:
                throw new RuntimeException("No valid target found for " + target);
        }
        if (releaseConfiguration != null) {
            clientConfig.setReleaseConfiguration(releaseConfiguration.toSubstrate());
        }
        clientConfig.setTarget(targetTriplet);

        clientConfig.setBundlesList(bundlesList);
        clientConfig.setResourcesList(resourcesList);
        clientConfig.setJniList(jniList);
        clientConfig.setCompilerArgs(nativeImageArgs);
        clientConfig.setLinkerArgs(linkerArgs);
        clientConfig.setRuntimeArgs(runtimeArgs);
        clientConfig.setReflectionList(reflectionList);
        clientConfig.setAppId(appIdentifier != null ? appIdentifier :
                project.getGroupId() + "." + project.getArtifactId());
        clientConfig.setAppName(project.getName());
        clientConfig.setVerbose("true".equals(verbose));
        clientConfig.setUsePrismSW("true".equals(enableSWRendering));
        clientConfig.setRemoteHostName(remoteHostName);
        clientConfig.setRemoteDir(remoteDir);

        return clientConfig;
    }

    ProcessDestroyer getProcessDestroyer() {
        if (processDestroyer == null) {
            processDestroyer = new ShutdownHookProcessDestroyer();
        }
        return processDestroyer;
    }

    private String getProjectClasspath() {
        List<File> classPath = getClasspathElements(project);
        getLog().debug("classPath = " + classPath);
        return classPath.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));
    }

    private List<File> getClasspathElements(MavenProject project) {
        MavenArtifactResolver.initRepositories(repoSystem, repoSession, remoteRepos);

        List<Artifact> attachDependencies = getAttachDependencies();
        List<File> list = Stream.concat(project.getArtifacts().stream(), attachDependencies.stream())
                .filter(d -> ALLOWED_DEPENDENCY_TYPES.stream().anyMatch(t -> t.equals(d.getType())))
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

        // include runtime dependencies
        getRuntimeDependencies().stream()
                .filter(d -> !list.contains(d))
                .forEach(list::add);

        // remove provided dependencies
        getProvidedDependencies().stream()
                .filter(list::contains)
                .forEach(list::remove);

        // WEB
        if (Constants.PROFILE_WEB.equals(target)) {
            project.getArtifacts().stream()
                    .filter(a -> "org.openjfx".equals(a.getGroupId()) && a.getClassifier() != null)
                    .map(a -> new DefaultArtifact(a.getGroupId(), a.getArtifactId(),
                            Constants.WEB_AOT_CLASSIFIER, "jar", Constants.DEFAULT_JAVAFX_JS_SDK_VERSION))
                    .flatMap(a -> {
                        DependencyFilter exclusions = (node, parents) ->
                                !node.getArtifact().getClassifier().equals(Constants.WEB_AOT_CLASSIFIER);
                        Set<Artifact> resolve = MavenArtifactResolver.getInstance().resolve(a, exclusions);
                        if (resolve == null) {
                            return Stream.empty();
                        }
                        return resolve.stream();
                    })
                    .distinct()
                    .map(Artifact::getFile)
                    .forEach(list::add);

            WebTargetConfiguration.WEB_AOT_DEPENDENCIES.stream()
                    .map(s -> s.split(":"))
                    .map(a -> new DefaultArtifact(a[0], a[1],
                            a.length == 4 ? a[3] : null, "jar", a[2]))
                    .flatMap(a -> {
                        Set<Artifact> resolve = MavenArtifactResolver.getInstance().resolve(a);
                        if (resolve == null) {
                            return Stream.empty();
                        }
                        return resolve.stream();
                    })
                    .distinct()
                    .map(Artifact::getFile)
                    .forEach(list::add);
        }
        return list;
    }

    List<Artifact> getAttachDependencies() {
        List<Dependency> dependencies = project.getDependencies();

        // include dependencies from project artifacts (transitive dependencies)
        project.getArtifacts().stream()
                .filter(a -> DEPENDENCY_GROUP.equals(a.getGroupId()))
                .map(a -> {
                    Dependency d = new Dependency();
                    d.setGroupId(a.getGroupId());
                    d.setArtifactId(a.getArtifactId());
                    d.setVersion(a.getVersion());
                    return d;
                })
                .forEach(dependencies::add);

        Map<String, Artifact> attachMap = AttachArtifactResolver.findArtifactsForTarget(dependencies, target);
        if (attachList != null) {
            return Stream.concat(attachList.stream(), Stream.of(UTIL_ARTIFACT))
                .distinct()
                .map(attachMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<File> getRuntimeDependencies() {
        return getDependencies("runtime");
    }

    private List<File> getProvidedDependencies() {
        return getDependencies("provided");
    }

    private List<File> getDependencies(String scope) {
        if (scope == null || scope.isEmpty()) {
            return new ArrayList<>();
        }
        return project.getDependencies().stream()
                .filter(d -> ALLOWED_DEPENDENCY_TYPES.stream().anyMatch(t -> t.equals(d.getType())))
                .filter(d -> scope.equals(d.getScope()))
                .map(d -> new DefaultArtifact(d.getGroupId(), d.getArtifactId(),
                        d.getClassifier(), d.getType(), d.getVersion()))
                .flatMap(a -> {
                    Set<Artifact> resolve = MavenArtifactResolver.getInstance().resolve(a);
                    if (resolve == null) {
                        return Stream.empty();
                    }
                    return resolve.stream();
                })
                .distinct()
                .map(Artifact::getFile)
                .collect(Collectors.toList());
    }

    Optional<String> getGraalvmHome() {
        return Optional.ofNullable(graalvmHome)
                .or(() -> Optional.ofNullable(System.getenv("GRAALVM_HOME")));
    }
}

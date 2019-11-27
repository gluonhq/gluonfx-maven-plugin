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

import com.gluonhq.substrate.SubstrateDispatcher;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class NativeCompileMojo extends NativeBaseMojo {

    public void execute() throws MojoExecutionException {
        super.execute();

        // Attach
        List<Artifact> attachDependencies = getAttachDependencies();
        project.getArtifacts().addAll(attachDependencies);

        // Compile
        Compile.compile(project, session, pluginManager);

        // Native Compile
        String mainClassName = mainClass;
        String name = project.getName();
        getLog().debug("mcn = "+mainClassName+" and name = "+name);

        List<File> classPath = getClasspathElements(project);
        getLog().debug("classPath = " + classPath);

        String cp0 = classPath.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(File.pathSeparator));

        String buildRoot = outputDir.toPath().toString();
        getLog().debug("BuildRoot: " + buildRoot);

        String cp = cp0 + File.pathSeparator;
        getLog().debug("cp = " + cp);

        try {
            SubstrateDispatcher dispatcher = new SubstrateDispatcher(Paths.get(buildRoot), clientConfig);
            dispatcher.nativeCompile(cp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error", e);
        }
    }
}

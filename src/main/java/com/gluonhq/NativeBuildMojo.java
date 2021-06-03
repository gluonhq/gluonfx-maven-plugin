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

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;


@Mojo(name = "build", defaultPhase = LifecyclePhase.COMPILE,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        aggregator = true)
public class NativeBuildMojo extends NativeBaseMojo {

    @Parameter(readonly = true, required = true, defaultValue = "${basedir}/pom.xml")
    String pom;

    public void execute() throws MojoExecutionException {
        getLog().debug("Start building");

        // prepare the execution:
        final InvocationRequest invocationRequest = new DefaultInvocationRequest();

        // apply profiles to both goals
        invocationRequest.setProfiles(project.getActiveProfiles().stream()
                .map(Profile::getId)
                .collect(Collectors.toList()));

        // apply properties to both goals that were set via the -Dkey=value
        // parameter on the command line
        invocationRequest.setProperties(session.getRequest().getUserProperties());

        invocationRequest.setPomFile(new File(pom));

        invocationRequest.setGoals(Arrays.asList("gluonfx:compile", "gluonfx:link"));

        final Invoker invoker = new DefaultInvoker();
        // execute:
        try {
            final InvocationResult invocationResult = invoker.execute(invocationRequest);
            if (invocationResult.getExitCode() != 0) {
                throw new MojoExecutionException("Error, gluonfx:build failed", invocationResult.getExecutionException());
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error", e);
        }

    }

}

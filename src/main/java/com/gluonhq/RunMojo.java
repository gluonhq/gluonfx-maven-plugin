/*
 * Copyright (c) 2021, Gluon
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
import com.gluonhq.attach.AttachService;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.PROCESS_CLASSES)
public class RunMojo extends NativeBaseMojo {

    private static final String POM_XML = "pom.xml";
    private static final String RUN_POM_XML = "runPom.xml";

    /**
     * The execution ID as defined in the POM.
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution execution;

    @Override
    public void execute() throws MojoExecutionException {

        final InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setProfiles(project.getActiveProfiles().stream()
                .map(Profile::getId)
                .collect(Collectors.toList()));
        invocationRequest.setProperties(session.getRequest().getUserProperties());

        // 1. Read pom
        File pomFile = new File(POM_XML);
        // 2. Create temporary pom file
        File runPomFile = new File(RUN_POM_XML);
        try (InputStream is = new FileInputStream(POM_XML)) {
            // 3. Create model from current pom
            Model model = new MavenXpp3Reader().read(is);

            model.getBuild().getPlugins().stream()
                    .filter(p -> p.getGroupId().equalsIgnoreCase("org.openjfx") &&
                            p.getArtifactId().equalsIgnoreCase("javafx-maven-plugin"))
                    .findFirst()
                    .orElseThrow(() -> new MojoExecutionException("No JavaFX plugin found"));

            // 4. Check for Attach Dependencies and if Desktop is supported, add the classifier
            model.getDependencies().stream()
                    .filter(p -> p.getGroupId().equalsIgnoreCase(AttachArtifactResolver.DEPENDENCY_GROUP))
                    .filter(p -> Arrays.stream(AttachService.values())
                            .filter(AttachService::isDesktopSupported)
                            .anyMatch(attach -> attach.getServiceName().equalsIgnoreCase(p.getArtifactId()))
                    )
                    .forEach(p -> p.setClassifier("desktop"));

            // 5. Serialize new pom
            try (OutputStream os = new FileOutputStream(runPomFile)) {
                new MavenXpp3Writer().write(os, model);
            }
        } catch (Exception e) {
            if (runPomFile.exists()) {
                runPomFile.delete();
            }
            throw new MojoExecutionException("Error reading pom", e);
        }

        if (!"host".equals(target)) {
            getLog().warn(String.format("Target '%s' will be ignored for 'gluonfx:run' goal on the host machine", target));
        }

        invocationRequest.setPomFile(runPomFile);
        String goal = "javafx:run";
        if (execution != null) {
            goal += "@" + execution.getExecutionId();
        }
        invocationRequest.setGoals(Collections.singletonList(goal));

        final Invoker invoker = new DefaultInvoker();
        try {
            final InvocationResult invocationResult = invoker.execute(invocationRequest);
            if (invocationResult.getExitCode() != 0) {
                throw new MojoExecutionException("Error, " + goal + " failed", invocationResult.getExecutionException());
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Error", e);
        } finally {
            if (runPomFile.exists()) {
                runPomFile.delete();
            }
        }
    }
}

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

import com.gluonhq.gradle.tasks.ClientNativeBuild;
import com.gluonhq.gradle.tasks.ClientNativeCompile;
import com.gluonhq.gradle.tasks.ClientNativeLink;
import com.gluonhq.gradle.tasks.ClientNativeRun;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ClientPlugin implements Plugin<Project> {

    private static final String CONFIGURATION_CLIENT = "client";

    @Override
    public void apply(Project project) {
        project.getConfigurations().create(CONFIGURATION_CLIENT);

        ClientExtension client = project.getExtensions().create("gluonClient", ClientExtension.class, project);

        project.afterEvaluate(a -> {
            ClientNativeCompile nativeCompile = project.getTasks().create("nativeCompile", ClientNativeCompile.class);
            nativeCompile.setTarget(client.getTarget());
            nativeCompile.dependsOn(project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            ClientNativeLink nativeLink = project.getTasks().create("nativeLink", ClientNativeLink.class);
            nativeLink.setTarget(client.getTarget());
            nativeLink.dependsOn(project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            ClientNativeBuild nativeBuild = project.getTasks().create("nativeBuild", ClientNativeBuild.class);
            nativeBuild.dependsOn(nativeCompile, nativeLink);

            ClientNativeRun nativeRun = project.getTasks().create("nativeRun", ClientNativeRun.class);
            nativeRun.setTarget(client.getTarget());
            nativeRun.dependsOn(project.getTasks().findByName("classes"), project.getTasks().findByName("processResources"));

            project.getLogger().debug("Applied client plugin, tasks = " + project.getAllTasks(true));
        });
    }
}

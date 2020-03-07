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

package com.gluonhq.attach;

import com.gluonhq.utils.MavenArtifactResolver;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Repository;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttachArtifactResolver {

    public static Map<String, Artifact> findArtifactsForTarget(List<AttachService> attachServices,
                                                               List<Dependency> dependencies,
                                                               List<Repository> repositories, String target) {

        final MavenArtifactResolver resolver = new MavenArtifactResolver(repositories);

        Map<Pair<String, String>, Dependency> dependencyArtifacts =
                mapDependencyArtifacts(dependencies);

        return attachServices.stream()
                .filter(attachService -> dependencyArtifacts.containsKey(
                        Pair.of(attachService.getGroupId(), attachService.getArtifactId())))
                .map(attachService -> {
                    String groupId = attachService.getGroupId();
                    String artifactId = attachService.getArtifactId();

                    Dependency dependency = dependencyArtifacts.get(Pair.of(groupId, artifactId));
                    AttachServiceDefinition attachServiceDefinition = new AttachServiceDefinition(attachService);

                    return new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(),
                            attachServiceDefinition.getSupportedPlatform(target),
                            dependency.getType(), dependency.getVersion());

                })
                .flatMap(a -> {
                    Set<Artifact> resolve = resolver.resolve(a);
                    if (resolve == null) {
                        return Stream.empty();
                    }
                    return resolve.stream();
                })
                .distinct()
                .collect(Collectors.toMap(Artifact::getArtifactId, a -> a));
    }

    private static Map<Pair<String, String>, Dependency> mapDependencyArtifacts(List<Dependency> dependencies) {
        return dependencies.stream().collect(
                Collectors.toMap(
                        dependency -> Pair.of(dependency.getGroupId(), dependency.getArtifactId()),
                        dependency -> dependency));
    }
}

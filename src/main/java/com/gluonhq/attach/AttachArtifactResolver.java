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

import com.gluonhq.substrate.Constants;
import com.gluonhq.utils.MavenArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttachArtifactResolver {

    public static final String DEPENDENCY_GROUP = "com.gluonhq.attach";
    public static final String UTIL_ARTIFACT = "util";

    public static Map<String, Artifact> findArtifactsForTarget(List<Dependency> dependencies, String target) {
        return dependencies.stream()
                .filter(d -> DEPENDENCY_GROUP.equals(d.getGroupId()))
                .map(d -> {
                    if (UTIL_ARTIFACT.equals(d.getArtifactId())) {
                        if (Constants.PROFILE_ANDROID.equals(target) ||
                            Constants.PROFILE_IOS.equals(target)||
                            Constants.PROFILE_IOS_SIM.equals(target)) {
                            return new DefaultArtifact(d.getGroupId(), d.getArtifactId(),
                                    target, d.getType(), d.getVersion());
                        }
                        return  null;
                    }
                    AttachServiceDefinition asd = new AttachServiceDefinition(d.getArtifactId());
                    return new DefaultArtifact(d.getGroupId(), d.getArtifactId(),
                            asd.getSupportedPlatform(target), d.getType(), d.getVersion());
                })
                .filter(Objects::nonNull)
                .flatMap(a -> {
                    Set<Artifact> resolve = MavenArtifactResolver.getInstance().resolve(a);
                    if (resolve == null) {
                        return Stream.empty();
                    }
                    return resolve.stream();
                })
                .distinct()
                .collect(Collectors.toMap(Artifact::getArtifactId, a -> a));
    }
}

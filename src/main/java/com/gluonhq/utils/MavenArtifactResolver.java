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

package com.gluonhq.utils;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Repository;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenArtifactResolver {

    private static String DEFAULT_LOCAL_REPO = org.apache.maven.repository.RepositorySystem.
            defaultUserLocalRepository.getAbsolutePath();

    private final RepositorySystem repositorySystem;
    private final List<RemoteRepository> remoteRepositories;

    public MavenArtifactResolver(List<Repository> repositories) {
        this.repositorySystem = createRepositorySystem();
        this.remoteRepositories = new LinkedList<>();
        repositories.forEach(r -> {
            RemoteRepository repository = new RemoteRepository
                .Builder(r.getId(), "default", r.getUrl())
                .build();
            remoteRepositories.add(repository);
        });
    }

    private RepositorySystem createRepositorySystem() {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        serviceLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new RuntimeException(exception);
            }
        });
        return serviceLocator.getService(RepositorySystem.class);
    }

    private DefaultRepositorySystemSession createRepositorySystemSession(RepositorySystem system, String localRepoPath) {
        DefaultRepositorySystemSession systemSession = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(localRepoPath);
        systemSession.setLocalRepositoryManager(system.newLocalRepositoryManager(systemSession, localRepo));
        return systemSession;
    }

    public Set<org.apache.maven.artifact.Artifact> resolve(Artifact artifact) {
        RepositorySystemSession systemSession = createRepositorySystemSession(this.repositorySystem, DEFAULT_LOCAL_REPO);

        ArtifactResult resolvedArtifact;
        try {
            List<ArtifactRequest> artifactRequests = Arrays.asList(
                    new ArtifactRequest(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                            artifact.getClassifier() != null ? artifact.getClassifier() : "",
                            artifact.getExtension(), artifact.getVersion()),
                    remoteRepositories, JavaScopes.RUNTIME));
            List<ArtifactResult> results = repositorySystem.resolveArtifacts(systemSession, artifactRequests);
            resolvedArtifact = results.get(results.size() - 1);
        } catch (ArtifactResolutionException e) {
            e.printStackTrace();
            return null;
        }

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(resolvedArtifact.getArtifact(), JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepositories);

        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);

        List<ArtifactResult> artifactResults;
        try {
            artifactResults = repositorySystem.resolveDependencies(systemSession, dependencyRequest)
                    .getArtifactResults();
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
            return null;
        }

        return artifactResults.stream()
                .map(ArtifactResult::getArtifact)
                .map(a -> {
                    org.apache.maven.artifact.Artifact ar = new org.apache.maven.artifact.DefaultArtifact(
                            a.getGroupId(), a.getArtifactId(), a.getVersion(),
                            "compile", "jar", a.getClassifier(), new DefaultArtifactHandler("jar"));
                    ar.setFile(a.getFile());
                    return ar;
                })
                .collect(Collectors.toSet());
    }

}
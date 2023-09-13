/*
 * Copyright (c) 2019, 2023, Gluon
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
import org.eclipse.aether.repository.RepositoryPolicy;
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
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_WARN;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_DAILY;
import static org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER;

public class MavenArtifactResolver {

    private static final String DEFAULT_LOCAL_REPO = org.apache.maven.repository.RepositorySystem.
            defaultUserLocalRepository.getAbsolutePath();

    private final RepositorySystem repositorySystem;
    private final List<RemoteRepository> remoteRepositories;
    private final RepositorySystemSession systemSession;

    private static MavenArtifactResolver instance;

    /**
     * Returns an existing instance of MavenArtifactResolver.
     *
     * If the instance hasn't been created yet, it will throw an
     * {@code IllegalStateException}. To prevent this,
     * {@link #initRepositories(List)} has to be called first.
     *
     * @return an instance of MavenArtifactResolver
     */
    public static MavenArtifactResolver getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MavenArtifactResolver not initialized");
        }
        return instance;
    }

    private MavenArtifactResolver(List<Repository> repositories) {
        repositorySystem = createRepositorySystem();
        systemSession = createRepositorySystemSession(repositorySystem, DEFAULT_LOCAL_REPO);
        remoteRepositories = new LinkedList<>();
        repositories.forEach(r -> {
            org.apache.maven.model.RepositoryPolicy releases = r.getReleases();
            RepositoryPolicy releasesRepositoryPolicy = null;
            if (releases != null) {
                releasesRepositoryPolicy = new RepositoryPolicy(releases.isEnabled(),
                        releases.getUpdatePolicy() == null ? UPDATE_POLICY_NEVER : releases.getUpdatePolicy(),
                        releases.getChecksumPolicy());
            }
            RepositoryPolicy snapshotsRepositoryPolicy = null;
            org.apache.maven.model.RepositoryPolicy snapshots = r.getSnapshots();
            if (snapshots != null) {
                snapshotsRepositoryPolicy = new RepositoryPolicy(snapshots.isEnabled(),
                        snapshots.getUpdatePolicy() == null ? UPDATE_POLICY_NEVER : snapshots.getUpdatePolicy(),
                        snapshots.getChecksumPolicy());
            }
            if (releasesRepositoryPolicy == null && snapshotsRepositoryPolicy == null) {
                if (r.getUrl().toLowerCase(Locale.ROOT).contains("/release")) {
                    releasesRepositoryPolicy = new RepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN);
                    snapshotsRepositoryPolicy = new RepositoryPolicy(false, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN);
                } else if (r.getUrl().toLowerCase(Locale.ROOT).contains("/snapshot")) {
                    releasesRepositoryPolicy = new RepositoryPolicy(false, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN);
                    snapshotsRepositoryPolicy = new RepositoryPolicy(true, UPDATE_POLICY_DAILY, CHECKSUM_POLICY_WARN);
                }
            }

            RemoteRepository repository = new RemoteRepository
                .Builder(r.getId(), "default", r.getUrl())
                    .setReleasePolicy(releasesRepositoryPolicy)
                    .setSnapshotPolicy(snapshotsRepositoryPolicy)
                    .build();
            remoteRepositories.add(repository);
        });
    }

    /**
     * Creates and initializes a new instance with a list of remote
     * repositories, only if such instance doesn't already exist
     *
     * @param repositories a list of remote repositories
     */
    public static void initRepositories(List<Repository> repositories) {
        if (instance != null) {
            return;
        }
        instance = new MavenArtifactResolver(repositories);
    }

    // TODO: Find an alternative, as this only works up until Maven 3.8.7.
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

    /**
     * Finds a set of existing artifacts for a created artifact out of on some coordinates and
     * classifier
     *
     * @param artifact the created artifact
     * @return a set of existing artifacts
     */
    public Set<org.apache.maven.artifact.Artifact> resolve(Artifact artifact) {
        return resolve(artifact, null);
    }

    /**
     * Finds a set of existing artifacts for a created artifact out of on some coordinates and
     * classifier
     *
     * @param artifact the created artifact
     * @param exclusionsFilter a filter that identifies artifacts that will be excluded, it can be null
     * @return a set of existing artifacts
     */
    public Set<org.apache.maven.artifact.Artifact> resolve(Artifact artifact, DependencyFilter exclusionsFilter) {
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
            Logger.getLogger(MavenArtifactResolver.class.getName()).log(Level.SEVERE, "Error resolving artifact: " + e.getMessage());
            return null;
        }

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(resolvedArtifact.getArtifact(), JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepositories);

        DependencyFilter classpathFilter;
        if (exclusionsFilter != null) {
            classpathFilter = DependencyFilterUtils.andFilter(
                    DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE),
                    DependencyFilterUtils.notFilter(exclusionsFilter));
        } else {
            classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        }
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
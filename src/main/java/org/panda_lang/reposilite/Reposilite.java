/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.reposilite;

import io.vavr.control.Try;
import org.fusesource.jansi.Ansi.Color;
import org.panda_lang.reposilite.auth.Authenticator;
import org.panda_lang.reposilite.auth.TokenService;
import org.panda_lang.reposilite.config.Configuration;
import org.panda_lang.reposilite.config.ConfigurationLoader;
import org.panda_lang.reposilite.console.Console;
import org.panda_lang.reposilite.frontend.Frontend;
import org.panda_lang.reposilite.frontend.FrontendLoader;
import org.panda_lang.reposilite.metadata.MetadataService;
import org.panda_lang.reposilite.repository.RepositoryService;
import org.panda_lang.reposilite.stats.StatsService;
import org.panda_lang.reposilite.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fusesource.jansi.Ansi.ansi;

public final class Reposilite {

    private static final Logger LOGGER = LoggerFactory.getLogger("Reposilite");

    private Console console;
    private Frontend frontend;
    private Authenticator authenticator;
    private TokenService tokenService;
    private StatsService statsService;
    private MetadataService metadataService;
    private RepositoryService repositoryService;
    private Configuration configuration;
    private ReposiliteHttpServer reactiveHttpServer;
    private boolean stopped;
    private long uptime;

    public static void main(String[] args) throws Exception {
        Reposilite reposilite = new Reposilite();
        reposilite.launch(args);
    }

    public void launch(String[] args) throws Exception {
        this.console = new Console(this);

        if (console.executeArguments(args)) {
            return;
        }

        getLogger().info("");
        getLogger().info(ansi().bold().fg(Color.GREEN).a("Reposilite ").reset().a(ReposiliteConstants.VERSION).reset().toString());
        getLogger().info("");

        Reposilite.getLogger().info("--- Preparing workspace");
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        this.configuration = configurationLoader.load();

        Thread shutdownHook = new Thread(() -> Try.run(this::shutdown).orElseRun(Throwable::printStackTrace));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        console.hook();

        FrontendLoader frontendLoader = new FrontendLoader();
        this.frontend = frontendLoader.loadFrontend(ReposiliteConstants.FRONTEND_FILE_NAME);

        getLogger().info("--- Loading data");
        this.tokenService = new TokenService();
        tokenService.load();

        this.statsService = new StatsService();
        statsService.load();
        getLogger().info("");

        this.authenticator = new Authenticator(tokenService);
        this.metadataService = new MetadataService();

        this.repositoryService = new RepositoryService();
        repositoryService.load(configuration);
        repositoryService.scan(configuration);
        getLogger().info("");

        getLogger().info("Binding server at *::" + configuration.getPort());
        this.uptime = System.currentTimeMillis();

        this.reactiveHttpServer = new ReposiliteHttpServer(this);
        reactiveHttpServer.start(configuration, () -> {
            getLogger().info("Done (" + TimeUtils.format(TimeUtils.getUptime(uptime)) + "s)!");
            console.displayHelp();

            getLogger().info("Collecting status metrics...");
            console.displayStatus();
        });
    }

    public void shutdown() throws Exception {
        if (stopped) {
            return;
        }

        this.stopped = true;
        getLogger().info("Shutting down...");

        statsService.save();
        reactiveHttpServer.stop();
        console.stop();

        getLogger().info("Bye! Uptime: " + TimeUtils.format(TimeUtils.getUptime(uptime) / 60) + "min");
    }

    public long getUptime() {
        return System.currentTimeMillis() - uptime;
    }

    public ReposiliteHttpServer getHttpServer() {
        return reactiveHttpServer;
    }

    public Frontend getFrontend() {
        return frontend;
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    public StatsService getStatsService() {
        return statsService;
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

}
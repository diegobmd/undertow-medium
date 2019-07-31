package br.com.diegobmd;

import br.com.diegobmd.config.ServerProperties;
import br.com.diegobmd.handler.HandlerProvider;
import br.com.diegobmd.module.ApplicationModule;
import com.google.inject.*;
import com.google.inject.util.Modules;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MediumServer {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Undertow server;
    private final Injector injector;
    private final long startTime;

    public MediumServer(Module... override) {

        startTime = System.currentTimeMillis();

        this.injector = Guice.createInjector(Stage.PRODUCTION, Modules.override(new ApplicationModule()).with(override));

        ServerProperties config = getInstance(ServerProperties.class);

        HandlerProvider handler = getInstance(HandlerProvider.class);

        this.server = Undertow.builder()
                .addHttpListener(config.getPort(), config.getAddress())
                .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
                .setServerOption(UndertowOptions.ALWAYS_SET_DATE, false)
                .setHandler(handler.getHandler())
                .build();
    }

    public void start() {
        server.start();

        server.getListenerInfo().forEach(i -> {
            LOGGER.info("Server start {} in {}ms", i.getAddress(), System.currentTimeMillis() - startTime);
        });
    }

    public <T> T getInstance(Class<T> aClass) {
        return injector.getInstance(aClass);
    }

    public <T> T getInstance(Key<T> key) {
        return injector.getInstance(key);
    }

    public void stop() {
        LOGGER.info("Server stop");

        server.stop();
    }

    public static void main(String[] args) {
        new MediumServer()
                .start();
    }
}
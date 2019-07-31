package br.com.diegobmd.module;

import br.com.diegobmd.config.ServerProperties;
import br.com.diegobmd.controller.MediumController;
import br.com.diegobmd.handler.HandlerProvider;
import br.com.diegobmd.service.MediumService;
import br.com.diegobmd.service.MediumServiceImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.ServiceLoader;

public class ApplicationModule extends AbstractModule {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected void configure() {
        configureProperties();

        ServiceLoader<Module> modules = ServiceLoader.load(Module.class);

        for (Module module : modules) {
            install(module);
        }

        bind(ObjectMapper.class).toInstance(objectMapper());
        bind(ServerProperties.class).asEagerSingleton();
        bind(HandlerProvider.class).asEagerSingleton();
        bind(MediumService.class).to(MediumServiceImpl.class).asEagerSingleton();
        bind(MediumController.class).asEagerSingleton();

        LOGGER.info("Application configured");
    }

    private void configureProperties() {

        String profile = getProfile();

        if (profile != null) {
            LOGGER.info("Profile active " + profile);
        } else {
            LOGGER.info("Profile active default");
        }

        try {
            bindPropertiesFile(getResource("application.properties"));
            bindPropertiesFile(getFile("application.properties"));

            if (profile != null) {
                bindPropertiesFile(getResource("application-" + profile + ".properties"));
                bindPropertiesFile(getFile("application-" + profile + ".properties"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProfile() {
        if (System.getenv("PROFILE_ACTIVE") != null)
            return System.getenv("PROFILE_ACTIVE");

        if (System.getenv("profile.active") != null)
            return System.getenv("profile.active");

        if (System.getProperty("PROFILE_ACTIVE") != null)
            return System.getProperty("PROFILE_ACTIVE");

        if (System.getProperty("profile.active") != null)
            return System.getProperty("profile.active");

        return null;
    }

    private void bindPropertiesFile(InputStream in) throws IOException {
        if (in != null) {
            Properties p = new Properties();
            p.load(in);
            Names.bindProperties(binder(), p);
        }
    }

    private InputStream getResource(String file) {
        return ApplicationModule.class.getResourceAsStream("/" + file);
    }

    private InputStream getFile(String file) {
        File inf = new File(file);

        if (inf.exists()) {
            try {
                return new FileInputStream(inf);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}

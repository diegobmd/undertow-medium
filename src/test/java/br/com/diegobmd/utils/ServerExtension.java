package br.com.diegobmd.utils;

import br.com.diegobmd.MediumServer;
import br.com.diegobmd.config.ServerProperties;
import com.google.inject.Key;
import com.google.inject.Module;
import io.restassured.RestAssured;
import org.junit.jupiter.api.extension.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ServerExtension implements TestInstancePostProcessor, BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private static MediumServer application;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Named.class)) {
                    field.set(testInstance, application.getInstance(Key.get(field.getType(), field.getAnnotation(Named.class))));
                } else {
                    field.set(testInstance, application.getInstance(field.getType()));
                }
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        System.setProperty("profile.active", "test");

        application = new MediumServer(getModules(context.getTestClass().orElse(null)));
        application.start();

        RestAssured.port = application.getInstance(ServerProperties.class).getPort();
        RestAssured.basePath = application.getInstance(ServerProperties.class).getContextPath();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (application != null)
            application.stop();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == MediumServer.class) || parameterContext.getParameter().isAnnotationPresent(Inject.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType() == MediumServer.class)
            return application;

        Parameter field = parameterContext.getParameter();

        if (field.isAnnotationPresent(Named.class)) {
            return application.getInstance(Key.get(field.getType(), field.getAnnotation(Named.class)));
        } else {
            return application.getInstance(field.getType());
        }
    }

    private Module[] getModules(Class<?> testClass) throws IllegalAccessException, InstantiationException {
        List<Module> modules = new ArrayList<>();

        if (testClass != null) {
            for (Class<?> innerClass : testClass.getDeclaredClasses()) {
                if (Module.class.isAssignableFrom(innerClass)) {
                    modules.add((Module) innerClass.newInstance());
                }
            }
        }
        return modules.toArray(new Module[0]);
    }
}

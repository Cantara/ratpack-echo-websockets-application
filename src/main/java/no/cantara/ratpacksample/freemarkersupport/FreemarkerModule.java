package no.cantara.ratpacksample.freemarkersupport;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import ratpack.guice.ConfigurableModule;

import java.io.IOException;

public class FreemarkerModule extends ConfigurableModule<FreemarkerModule.Config> {

    public static class Config {
        private String templateLoadingPath;

        public String getTemplateLoadingPath() {
            return templateLoadingPath;
        }

        public Config templateLoadingPath(String templateLoadingPath) {
            this.templateLoadingPath = templateLoadingPath;
            return this;
        }
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public FreemarkerRenderer freemarkerRenderer(Config config) {
        try {
            return new FreemarkerRenderer(config.getTemplateLoadingPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

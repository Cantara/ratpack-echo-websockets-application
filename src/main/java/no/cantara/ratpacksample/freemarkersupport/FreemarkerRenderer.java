package no.cantara.ratpacksample.freemarkersupport;

import freemarker.cache.TemplateLookupStrategy;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import ratpack.handling.Context;
import ratpack.http.MediaType;
import ratpack.render.RendererSupport;

import java.io.IOException;
import java.io.StringWriter;

public class FreemarkerRenderer extends RendererSupport<FreemarkerModel> {

    private Configuration freemarkerConfig;

    public FreemarkerRenderer(String templateLoadingPath) throws IOException {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_22);
        freemarkerConfig.setTemplateLookupStrategy(TemplateLookupStrategy.DEFAULT_2_3_0);
        freemarkerConfig.setClassForTemplateLoading(FreemarkerRenderer.class, templateLoadingPath);
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER); // in prod: TemplateExceptionHandler.RETHROW_HANDLER
    }

    @Override
    public void render(Context context, FreemarkerModel model) throws Exception {
        StringWriter stringWriter = new StringWriter();
        Template template = freemarkerConfig.getTemplate(model.getTemplate());
        template.process(model, stringWriter);
        context.getResponse().contentType(MediaType.TEXT_HTML);
        context.getResponse().send(stringWriter.toString());
    }
}

package no.cantara.ratpacksample.freemarkersupport;

import java.util.HashMap;

public class FreemarkerModel extends HashMap<String, Object> {
    private final String template;

    public FreemarkerModel(String template) {
        this.template = template;
    }

    public FreemarkerModel(String template, HashMap<String, Object> map) {
        this(template);
        putAll(map);
    }

    public String getTemplate() {
        return template;
    }
}

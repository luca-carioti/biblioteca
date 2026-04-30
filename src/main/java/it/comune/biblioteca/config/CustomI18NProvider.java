package it.comune.biblioteca.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class CustomI18NProvider implements I18NProvider {

    private final ResourceBundleMessageSource messageSource;

    public CustomI18NProvider() {
	messageSource = new ResourceBundleMessageSource();
	messageSource.setBasenames("messages"); // Nome base dei file .properties
	messageSource.setDefaultEncoding("UTF-8");
    }

    @Override
    public List<Locale> getProvidedLocales() {
	return List.of(Locale.ITALIAN, Locale.ENGLISH);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
	return messageSource.getMessage(key, params, locale);
    }
}
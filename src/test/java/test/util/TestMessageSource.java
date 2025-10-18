package test.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

public final class TestMessageSource {

    private static final MessageSource messageSource = init();

    public TestMessageSource() {}

    private static MessageSource init() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasenames("ValidationMessages");
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }
    
    public static String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}

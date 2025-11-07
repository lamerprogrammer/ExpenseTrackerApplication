package com.example.expensetracker.controller.base;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public interface ControllerSupport {

    /**
     * Каждый контроллер должен предоставить доступ к MessageSource.
     */
    MessageSource getMessageSource();


    /**
     * Получает локализованное сообщение по коду.
     *
     * @param code ключ сообщения из message.properties
     * @return локализованный текст
     */
    default String msg(String code) {
        return getMessageSource().getMessage(code, null, LocaleContextHolder.getLocale());
    }
}


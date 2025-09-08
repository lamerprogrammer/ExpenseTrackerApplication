package test.util;

import org.springframework.mock.web.DelegatingServletOutputStream;

import java.io.ByteArrayOutputStream;

public final class UtilForTests {

    private UtilForTests() {}

    public static DelegatingServletOutputStream writeByteToStream(ByteArrayOutputStream outputStream) {
        return new DelegatingServletOutputStream(outputStream);
    }
}
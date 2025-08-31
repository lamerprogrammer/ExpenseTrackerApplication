package test.util;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UtilForTests {

    public static ServletOutputStream writeByteToStream(ByteArrayOutputStream byteArrayOutputStream) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }
        };
    }
}
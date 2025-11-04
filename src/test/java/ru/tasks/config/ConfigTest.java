package ru.tasks.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigTest {

    private ClassLoader prev;

    @AfterEach
    void cleanup() {
        if (prev != null) {
            Thread.currentThread().setContextClassLoader(prev);
        }

        System.clearProperty("ttlMinutes");
        System.clearProperty("cleanupSeconds");
    }

    @Test
    void readsFromClasspathWhenNoCliProps() {
        setContextLoader("ttlMinutes=5\ncleanupSeconds=7\n");
        assertEquals(Duration.ofMinutes(5), Config.linkTtl());
        assertEquals(7L, Config.cleanupSeconds());
    }

    @Test
    void cliPropsOverrideEnv() {
        setContextLoader("ttlMinutes=5\ncleanupSeconds=7\n");
        System.setProperty("ttlMinutes", "9");
        System.setProperty("cleanupSeconds", "11");
        assertEquals(Duration.ofMinutes(9), Config.linkTtl());
        assertEquals(11L, Config.cleanupSeconds());
    }

    @Test
    void defaultsWhenNoPropsAndNoEnv() {
        assertEquals(Duration.ofMinutes(60L), Config.linkTtl());
        assertEquals(Config.DEFAULT_CLEANUP_SECONDS, Config.cleanupSeconds());
    }

    @SuppressWarnings("all")
    private void setContextLoader(final String content) {
        prev = Thread.currentThread().getContextClassLoader();
        final ClassLoader cl = new ClassLoader(prev) {
            @Override
            public InputStream getResourceAsStream(String name) {
                if ("config.env".equals(name)) {
                    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                }
                return super.getResourceAsStream(name);
            }
        };
        Thread.currentThread().setContextClassLoader(cl);
    }
}

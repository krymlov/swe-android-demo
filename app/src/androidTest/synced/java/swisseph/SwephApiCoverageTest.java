/*
 * Copyright (C) By the Author
 * Author    Yura Krymlov
 * Created   2026-08
 */
package swisseph;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code SwephExp} is a pure JNI declaration layer with no logic of its own to unit test, so
 * "covered" here means "exercised by at least one test in this module" - a call site is the
 * only thing that can catch a wrong name, wrong signature or wrong array size. This audits
 * that claim instead of trusting it: every {@code native} method reflection finds on
 * {@code SwephExp} must appear as a call, i.e. {@code methodName(}, somewhere under
 * {@code src/test/java} outside this file itself. A future native method added to
 * {@code SwephExp.java} without an accompanying test fails this rather than silently
 * shipping untested.
 *
 * @author Yura Krymlov
 * @version 1.0, 2026-08
 */
class SwephApiCoverageTest {

    private static final Path TEST_SOURCE_ROOT = org.swisseph.android.TestFiles.sources("swe-java-api");

    private static Set<String> declaredNativeMethodNames() {
        Set<String> names = new TreeSet<>();
        for (Method m : SwephExp.class.getDeclaredMethods()) {
            if (Modifier.isNative(m.getModifiers())) {
                names.add(m.getName());
            }
        }
        return names;
    }

    private static String allTestSourceExceptThisFile() throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(TEST_SOURCE_ROOT)) {
            files = walk.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("SwephApiCoverageTest.java"))
                    .collect(Collectors.toList());
        }
        StringBuilder all = new StringBuilder();
        for (Path f : files) {
            all.append(new String(Files.readAllBytes(f))).append('\n');
        }
        return all.toString();
    }

    @Test
    void everyNativeMethodDeclaredOnSwephExpIsCalledBySomeTest() throws IOException {
        Set<String> declared = declaredNativeMethodNames();
        assertTrue(declared.size() >= 100, "sanity check: reflection found only "
                + declared.size() + " native methods - is SwephExp on the classpath?");

        String source = allTestSourceExceptThisFile();
        List<String> uncovered = new ArrayList<>();
        for (String name : declared) {
            if (!source.contains(name + "(")) {
                uncovered.add(name);
            }
        }

        assertTrue(uncovered.isEmpty(), "native methods with no test call site: " + uncovered);
    }

    @Test
    void declaredNativeMethodCountMatchesSwephexpH() {
        // swephexp.h declares 107 ext_def entries; swe_set_timeout is commented out there
        // and has no symbol to bind to, so the bridge wraps the remaining 106 - see
        // CLAUDE.md's "API coverage" section
        assertTrue(declaredNativeMethodNames().size() == 106,
                "expected 106 native methods, found " + declaredNativeMethodNames().size());
    }
}

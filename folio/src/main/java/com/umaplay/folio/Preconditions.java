package com.umaplay.folio;

final class Preconditions {

    static void checkNotNull(Object o, String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
    }

    static void checkStringNotEmpty(String s, String message) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    static void checkTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalStateException(message);
        }
    }

    private Preconditions() {
        throw new AssertionError("No instances");
    }
}

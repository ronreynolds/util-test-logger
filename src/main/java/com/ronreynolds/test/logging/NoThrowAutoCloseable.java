package com.ronreynolds.test.logging;

import java.util.Objects;

public class NoThrowAutoCloseable implements AutoCloseable {
    private final Runnable onClose;

    public static NoThrowAutoCloseable of(Runnable onClose) {
        return new NoThrowAutoCloseable(onClose);
    }

    public NoThrowAutoCloseable(Runnable onClose) {
        this.onClose = Objects.requireNonNull(onClose, "onClose can not be null");
    }

    @Override
    public void close() {
        onClose.run();
    }
}

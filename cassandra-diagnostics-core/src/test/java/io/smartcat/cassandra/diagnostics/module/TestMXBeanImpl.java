package io.smartcat.cassandra.diagnostics.module;

/**
 * TextMXBean interface implementation.
 */
public class TestMXBeanImpl implements TestMXBean {

    private final ModuleConfiguration config;

    public TestMXBeanImpl(ModuleConfiguration config) {
        this.config = config;
    }

    public boolean called = false;

    @Override
    public int getValue() {
        this.called = true;
        return 1;
    }
}

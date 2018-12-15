package com.sci.bfc.util.jit;

public final class JITClassLoader extends ClassLoader {
    public static final JITClassLoader INSTANCE = new JITClassLoader();

    private JITClassLoader() {
    }

    public Class<?> loadClass(final String name, final byte[] data) throws ClassFormatError {
        return this.defineClass(name, data, 0, data.length);
    }
}
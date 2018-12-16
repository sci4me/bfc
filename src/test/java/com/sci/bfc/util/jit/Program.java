package com.sci.bfc.util.jit;

import java.util.ArrayList;
import java.util.List;

public abstract class Program implements Runnable {
    protected static int wrap(final int n) {
        if(n < 0) return n + 256;
        if(n > 255) return n - 256;
        return n;
    }

    private final List<Integer> stdin;
    private final List<Integer> stdout;

    protected final int[] tape;
    protected int dp;

    public Program(final List<Integer> stdin, final int tapeSize) {
        this.stdin = new ArrayList<>(stdin);
        this.stdout = new ArrayList<>();
        this.tape = new int[tapeSize];
        this.dp = 0;
    }

    protected final void read(final int base_offset) {
        this.tape[this.dp + base_offset] = Program.wrap(this.stdin.remove(0));
    }

    protected final void write(final int base_offset) {
        this.stdout.add(this.tape[this.dp + base_offset]);
    }

    protected final void scanLeft() {
        while(this.tape[this.dp] != 0) {
            this.dp--;
        }
    }

    protected final void scanRight() {
        while(this.tape[this.dp] != 0) {
            this.dp++;
        }
    }

    public final int[] getTape() {
        return this.tape;
    }

    public final int getDP() {
        return this.dp;
    }

    public final List<Integer> getOutput() {
        return this.stdout;
    }

    public abstract void run();
}
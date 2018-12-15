package com.sci.bfc.util;

import com.sci.bfc.ir.*;
import com.sci.bfc.ir.Set;

import java.util.*;

public final class IRRunner implements IVisitor {
    private final List<Instruction> ir;
    private final List<Integer> stdin;
    private final List<Integer> stdout;

    private final Map<Integer, Integer> loopLocations;

    private int[] tape;
    private int ip;
    private int nextIP;
    private int dp;

    public IRRunner(final List<Instruction> ir, final int tapeSize, final List<Integer> stdin) {
        this.ir = ir;
        this.stdin = new ArrayList<>(stdin);
        this.stdout = new ArrayList<>();

        this.loopLocations = new HashMap<>();
        this.calculateLoops();

        this.tape = new int[tapeSize];
        this.ip = 0;
        this.dp = 0;
    }

    private void calculateLoops() {
        final Stack<Integer> starts = new Stack<>();

        for(int i = 0; i < this.ir.size(); i++) {
            final Instruction insn = this.ir.get(i);
            if(insn instanceof Open) {
                starts.push(i);
            } else if(insn instanceof Close) {
                final int start = starts.pop();

                this.loopLocations.put(start, i + 1);
                this.loopLocations.put(i, start + 1);
            }
        }

        if(!starts.isEmpty()) throw new RuntimeException("Unclosed loop");
    }

    private int wrap(final int n) {
        if(n < 0) return n + 0x100;
        if(n > 0xFF) return n - 0x100;
        return n;
    }

    public void run() {
        while(this.ip < this.ir.size()) {
            this.nextIP = this.ip + 1;
            this.ir.get(this.ip).accept(this);
            this.ip = this.nextIP;
        }
    }

    public int[] getTape() {
        return this.tape;
    }

    public List<Integer> getOutput() {
        return Collections.unmodifiableList(this.stdout);
    }

    @Override
    public void visitAdjust(final Adjust insn) {
        this.tape[this.dp] = this.wrap(this.tape[this.dp] + insn.delta);
    }

    @Override
    public void visitSelect(final Select insn) {
        this.dp += insn.delta;
    }

    @Override
    public void visitRead(final Read insn) {
        this.tape[this.dp] = this.wrap(this.stdin.remove(0));
    }

    @Override
    public void visitWrite(final Write insn) {
        this.stdout.add(this.tape[this.dp]);
    }

    @Override
    public void visitOpen(final Open insn) {
        if(this.tape[this.dp] == 0) {
            this.nextIP = this.loopLocations.get(this.ip);
        }
    }

    @Override
    public void visitClose(final Close insn) {
        if(this.tape[this.dp] != 0) {
            this.nextIP = this.loopLocations.get(this.ip);
        }
    }

    @Override
    public void visitSet(final Set insn) {
        this.tape[this.dp] = this.wrap(insn.value);
    }

    @Override
    public void visitMul(final Mul insn) {
        final int i = this.dp + insn.offset;
        final int n = this.wrap(this.tape[this.dp] * insn.factor);
        this.tape[i] = this.wrap(this.tape[i] + n);
    }
}
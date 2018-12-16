package com.sci.bfc;

import com.sci.bfc.ir.*;
import com.sci.bfc.util.Stack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class CCodeGenerator implements IVisitor {
    private final List<Instruction> ir;
    private final int tapeSize;
    private final StringBuilder sb;
    private int level;

    private final Stack<Integer> loops;
    private int loop;

    public CCodeGenerator(final List<Instruction> ir, final int tapeSize) {
        this.ir = ir;
        this.tapeSize = tapeSize;
        this.sb = new StringBuilder();
        this.loops = new Stack<>();
    }

    private void increaseIndent() {
        this.level++;
    }

    private void decreaseIndent() {
        this.level--;
    }

    private void indent() {
        for (int i = 0; i < this.level; i++) this.emit("    ");
    }

    private void emit(final String fmt, final Object... args) {
        this.sb.append(String.format(fmt, args));
    }

    private void emitLine(final String fmt, final Object... args) {
        this.emit(fmt, args);
        this.sb.append('\n');
    }

    private String readTemplate() {
        try {
            final StringBuilder sb = new StringBuilder();

            final InputStream tin = CCodeGenerator.class.getResourceAsStream("/template.bf.c");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(tin));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                sb.append(line);
                sb.append('\n');
            }

            return sb.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String compile() {
        this.increaseIndent();
        this.ir.forEach(n -> n.accept(this));
        this.decreaseIndent();

        final String template = this.readTemplate();
        return template
                .replace("__TAPE_SIZE__", String.valueOf(this.tapeSize))
                .replace("__CODE__", this.sb.toString());
    }

    @Override
    public void visitAdjust(final Adjust insn) {
        this.indent();
        this.emitLine("ADJUST(%d, %d);", insn.base_offset, insn.delta);
    }

    @Override
    public void visitSelect(final Select insn) {
        this.indent();
        this.emitLine("SELECT(%d);", insn.delta);
    }

    @Override
    public void visitRead(final Read insn) {
        this.indent();
        this.emitLine("READ(%d);", insn.base_offset);
    }

    @Override
    public void visitWrite(final Write insn) {
        this.indent();
        this.emitLine("WRITE(%d);", insn.base_offset);
    }

    @Override
    public void visitOpen(final Open insn) {
        this.indent();
        this.emitLine("OPEN(%d);", this.loop);
        this.loops.push(this.loop);
        this.loop++;
        this.increaseIndent();
    }

    @Override
    public void visitClose(final Close insn) {
        this.decreaseIndent();
        this.indent();
        this.emitLine("CLOSE(%d);", this.loops.pop());
    }

    @Override
    public void visitSet(final Set insn) {
        this.indent();
        this.emitLine("SET(%d, %d);", insn.base_offset, insn.value);
    }

    @Override
    public void visitMul(final Mul insn) {
        this.indent();
        this.emitLine("MUL(%d, %d);", insn.offset, insn.factor);
    }

    @Override
    public void visitScanLeft(final ScanLeft insn) {
        this.indent();
        this.emitLine("SCAN_LEFT();");
    }

    @Override
    public void visitScanRight(final ScanRight insn) {
        this.indent();
        this.emitLine("SCAN_RIGHT();");
    }
}
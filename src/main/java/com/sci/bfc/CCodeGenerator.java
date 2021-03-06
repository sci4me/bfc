package com.sci.bfc;

import com.sci.bfc.ir.*;

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

    public CCodeGenerator(final List<Instruction> ir, final int tapeSize) {
        this.ir = ir;
        this.tapeSize = tapeSize;
        this.sb = new StringBuilder();
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
                .replace("__CODE__", this.sb.toString().trim());
    }

    @Override
    public void visitAdjust(final Adjust insn) {
        this.indent();
        this.emitLine("ADJUST(%d, %d)", insn.baseOffset(), insn.delta);
    }

    @Override
    public void visitSelect(final Select insn) {
        this.indent();
        this.emitLine("SELECT(%d)", insn.delta);
    }

    @Override
    public void visitRead(final Read insn) {
        this.indent();
        this.emitLine("READ(%d)", insn.baseOffset());
    }

    @Override
    public void visitWrite(final Write insn) {
        this.indent();
        this.emitLine("WRITE(%d)", insn.baseOffset());
    }

    @Override
    public void visitOpen(final Open insn) {
        this.indent();
        this.emitLine("OPEN()");
        this.increaseIndent();
    }

    @Override
    public void visitClose(final Close insn) {
        this.decreaseIndent();
        this.indent();
        this.emitLine("CLOSE()");
    }

    @Override
    public void visitSet(final Set insn) {
        this.indent();
        this.emitLine("SET(%d, %d)", insn.baseOffset(), insn.value);
    }

    @Override
    public void visitMAdd(final MAdd insn) {
        this.indent();
        this.emitLine("MADD(%d, %d)", insn.offset, insn.factor);
    }

    @Override
    public void visitScanLeft(final ScanLeft insn) {
        this.indent();
        this.emitLine("SCAN_LEFT()");
    }

    @Override
    public void visitScanRight(final ScanRight insn) {
        this.indent();
        this.emitLine("SCAN_RIGHT()");
    }
}
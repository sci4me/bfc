package com.sci.bfc;

import com.sci.bfc.ir.*;
import com.sci.bfc.util.Stack;

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
        for(int i = 0; i < this.level; i++) this.emit("    ");
    }

    private void emit(final String fmt, final Object... args) {
        this.sb.append(String.format(fmt, args));
    }

    private void emitLine(final String fmt, final Object... args) {
        this.emit(fmt, args);
        this.sb.append('\n');
    }

    public String compile() {
        this.emitLine("#define _GNU_SOURCE");
        this.emitLine("#include <stdio.h>");
        this.emitLine("#include <string.h>");
        this.emitLine("#include <sys/mman.h>");

        this.emitLine("typedef unsigned char u8;");
        this.emitLine("typedef unsigned long long u64;");

        this.emitLine("static const u64 tape_size = " + this.tapeSize + ";");

        this.emitLine("#define ADJUST(delta) *dp += delta");
        this.emitLine("#define SELECT(delta) dp += delta");
        this.emitLine("#define READ() *dp = getchar()");
        this.emitLine("#define WRITE() putchar(*dp)");
        this.emitLine("#define OPEN(loop) loop_##loop##_start: if(!*dp) goto loop_##loop##_end;");
        this.emitLine("#define CLOSE(loop) if(*dp) goto loop_##loop##_start; loop_##loop##_end:");
        this.emitLine("#define SET(value) *dp = value");
        this.emitLine("#define MUL(offset, factor) *(dp + offset) += *dp * factor");
        this.emitLine("#define SCAN_LEFT() dp -= (u64)((void*) dp - memrchr(tape, 0, (dp - tape + 1)))");
        this.emitLine("#define SCAN_RIGHT() dp += (u64)(memchr(dp, 0, tape_size - (dp - tape)) - (void*) dp)");

        this.emitLine("int main() {");
        this.increaseIndent();
        this.indent();
        this.emitLine("u8 *tape = (u8*) mmap(0, tape_size, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);");
        this.indent();
        this.emitLine("u8 *dp = tape;");

        this.ir.forEach(n -> n.accept(this));

        this.indent();
        this.emitLine("munmap(tape, tape_size);");
        this.indent();
        this.emitLine("return 0;");
        this.emit("}");

        return this.sb.toString();
    }

    @Override
    public void visitAdjust(final Adjust insn) {
        this.indent();
        this.emitLine("ADJUST(%d);", insn.delta);
    }

    @Override
    public void visitSelect(final Select insn) {
        this.indent();
        this.emitLine("SELECT(%d);", insn.delta);
    }

    @Override
    public void visitRead(final Read insn) {
        this.indent();
        this.emitLine("READ();");
    }

    @Override
    public void visitWrite(final Write insn) {
        this.indent();
        this.emitLine("WRITE();");
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
        this.emitLine("SET(%d);", insn.value);
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
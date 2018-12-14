package com.sci.bfc.ir;

public interface IVisitor {
    void visitAdjust(final Adjust insn);

    void visitSelect(final Select insn);

    void visitRead(final Read insn);

    void visitWrite(final Write insn);

    void visitOpen(final Open insn);

    void visitClose(final Close insn);

    void visitSet(final Set insn);
}
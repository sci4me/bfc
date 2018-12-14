package com.sci.bfc.ir;

public final class Read extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitRead(this);
    }
}
package com.sci.bfc.ir;

public final class Open extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitOpen(this);
    }
}
package com.sci.bfc.ir;

public final class Write extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitWrite(this);
    }
}
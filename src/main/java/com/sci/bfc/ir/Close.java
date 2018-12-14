package com.sci.bfc.ir;

public final class Close extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitClose(this);
    }
}
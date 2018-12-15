package com.sci.bfc.ir;

public final class Mul extends Instruction {
    public final int offset;
    public final int factor;

    public Mul(final int offset, final int factor) {
        this.offset = offset;
        this.factor = factor;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitMul(this);
    }
}
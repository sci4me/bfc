package com.sci.bfc.ir;

public final class Adjust extends Instruction {
    public final int delta;

    public Adjust(final int delta) {
        this.delta = delta;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitAdjust(this);
    }
}
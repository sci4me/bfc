package com.sci.bfc.ir;

public final class Adjust extends Instruction {
    public final int base_offset;
    public final int delta;

    public Adjust(final int base_offset, final int delta) {
        this.base_offset = base_offset;
        this.delta = delta;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitAdjust(this);
    }
}
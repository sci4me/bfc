package com.sci.bfc.ir;

public final class Read extends Instruction {
    public final int base_offset;

    public Read(final int base_offset) {
        this.base_offset = base_offset;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitRead(this);
    }
}
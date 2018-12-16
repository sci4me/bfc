package com.sci.bfc.ir;

public final class Write extends Instruction {
    public final int base_offset;

    public Write(final int base_offset) {
        this.base_offset = base_offset;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitWrite(this);
    }
}
package com.sci.bfc.ir;

public final class Set extends Instruction {
    public final int base_offset;
    public final int value;

    public Set(final int base_offset, final int value) {
        this.base_offset = base_offset;
        this.value = value;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitSet(this);
    }
}
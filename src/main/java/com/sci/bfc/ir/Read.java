package com.sci.bfc.ir;

public final class Read extends Instruction {
    private final int base_offset;

    public Read(final int base_offset) {
        this.base_offset = base_offset;
    }

    @Override
    public int baseOffset() {
        return this.base_offset;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitRead(this);
    }
}
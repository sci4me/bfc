package com.sci.bfc.ir;

public final class Set extends Instruction {
    public final int value;

    public Set(final int value) {
        this.value = value;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitSet(this);
    }
}
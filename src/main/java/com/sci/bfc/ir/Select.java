package com.sci.bfc.ir;

public final class Select extends Instruction {
    public final int delta;

    public Select(final int delta) {
        this.delta = delta;
    }

    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitSelect(this);
    }
}
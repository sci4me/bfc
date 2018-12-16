package com.sci.bfc.ir;

public final class ScanLeft extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitScanLeft(this);
    }
}
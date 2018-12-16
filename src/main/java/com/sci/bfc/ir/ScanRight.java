package com.sci.bfc.ir;

public final class ScanRight extends Instruction {
    @Override
    public void accept(final IVisitor visitor) {
        visitor.visitScanRight(this);
    }
}
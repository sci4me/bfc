package com.sci.bfc.opts;

import com.sci.bfc.ir.Instruction;

import java.util.List;

public interface Optimization {
    List<Instruction> optimize(final List<Instruction> ir);
}
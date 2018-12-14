package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Select;

import java.util.ArrayList;
import java.util.List;

public final class NullInstructionRemoval implements Optimization {
    private static boolean isNull(final Instruction insn) {
        if(insn instanceof Adjust && ((Adjust) insn).delta == 0) return true;
        if(insn instanceof Select && ((Select) insn).delta == 0) return true;
        return false;
    }

    public static final NullInstructionRemoval INSTANCE = new NullInstructionRemoval();

    private NullInstructionRemoval() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            final Instruction insn = ir.get(index);
            if(!NullInstructionRemoval.isNull(insn)) result.add(insn);
            index++;
        }

        return result;
    }
}
package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Set;

import java.util.ArrayList;
import java.util.List;

public final class AdjustSetOptimization implements Optimization {
    private static boolean matchAdjustSet(final List<Instruction> ir, final int index) {
        if(index + 1 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Adjust)) return false;
        if(!(ir.get(index + 1) instanceof Set)) return false;
        return true;
    }

    public static final AdjustSetOptimization INSTANCE = new AdjustSetOptimization();

    private AdjustSetOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            final Instruction insn = ir.get(index);
            if(AdjustSetOptimization.matchAdjustSet(ir, index)) {
                result.add(ir.get(index + 1));
                index += 2;
            } else {
                result.add(insn);
                index++;
            }
        }

        return result;
    }
}
package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Set;

import java.util.ArrayList;
import java.util.List;

public final class SetAdjustOptimization implements Optimization {
    private static boolean matchSetAdjust(final List<Instruction> ir, final int index) {
        if(index + 1 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Set)) return false;
        if(!(ir.get(index + 1) instanceof Adjust)) return false;
        if(ir.get(index).baseOffset() != ir.get(index + 1).baseOffset()) return false;
        return true;
    }

    public static final SetAdjustOptimization INSTANCE = new SetAdjustOptimization();

    private SetAdjustOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(SetAdjustOptimization.matchSetAdjust(ir, index)) {
                final int value = ((Set) ir.get(index)).value;
                final Adjust adjust = (Adjust) ir.get(index + 1);
                result.add(new Set(adjust.baseOffset(), value + adjust.delta));
                index += 2;
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
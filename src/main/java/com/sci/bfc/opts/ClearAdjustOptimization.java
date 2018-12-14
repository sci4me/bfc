package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Set;

import java.util.ArrayList;
import java.util.List;

public final class ClearAdjustOptimization implements Optimization {
    private static boolean matchClearAdjust(final List<Instruction> ir, final int index) {
        if(index + 1 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Set)) return false;
        if(((Set) ir.get(index)).value != 0) return false;
        return ir.get(index + 1) instanceof Adjust;
    }

    public static final ClearAdjustOptimization INSTANCE = new ClearAdjustOptimization();

    private ClearAdjustOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(ClearAdjustOptimization.matchClearAdjust(ir, index)) {
                final int value = ((Adjust) ir.get(index + 1)).delta;
                result.add(new Set(value < 0 ? value + 256 : value));
                index += 2;
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
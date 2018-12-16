package com.sci.bfc.opts;

import com.sci.bfc.ir.*;

import java.util.ArrayList;
import java.util.List;

public final class ClearLoopRemoval implements Optimization {
    private static boolean matchClear(final List<Instruction> ir, final int index) {
        if(index + 2 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Open)) return false;
        if(!(ir.get(index + 1) instanceof Adjust)) return false;
        if(!(ir.get(index + 2) instanceof Close)) return false;

        final int delta = ((Adjust) ir.get(index + 1)).delta;
        return delta == -1 || delta == 1;
    }

    public static final ClearLoopRemoval INSTANCE = new ClearLoopRemoval();

    private ClearLoopRemoval() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(ClearLoopRemoval.matchClear(ir, index)) {
                result.add(new Set(0, 0));
                index += 3;
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
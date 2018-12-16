package com.sci.bfc.opts;

import com.sci.bfc.ir.*;

import java.util.ArrayList;
import java.util.List;

public final class ScanLoopOptimization implements Optimization {
    private static boolean matchScanLoop(final List<Instruction> ir, final int index) {
        if(index + 2 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Open)) return false;
        if(!(ir.get(index + 1) instanceof Select)) return false;
        if(!(ir.get(index + 2) instanceof Close)) return false;
        final int delta = ((Select) ir.get(index + 1)).delta;
        return delta == 1 || delta == -1;
    }

    public static final ScanLoopOptimization INSTANCE = new ScanLoopOptimization();

    private ScanLoopOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(ScanLoopOptimization.matchScanLoop(ir, index)) {
                if(((Select) ir.get(index + 1)).delta > 0) {
                    result.add(new ScanRight());
                } else {
                    result.add(new ScanLeft());
                }
                index += 3;
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
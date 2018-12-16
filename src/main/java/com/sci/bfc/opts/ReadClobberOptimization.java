package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Read;
import com.sci.bfc.ir.Set;

import java.util.ArrayList;
import java.util.List;

public final class ReadClobberOptimization implements Optimization {
    private static boolean matchReadClobber(final List<Instruction> ir, final int index) {
        if(index + 1 >= ir.size()) return false;
        if(!(ir.get(index) instanceof Set || ir.get(index) instanceof Adjust)) return false;
        if(!(ir.get(index + 1) instanceof Read)) return false;
        return true;
    }

    public static final ReadClobberOptimization INSTANCE = new ReadClobberOptimization();

    private ReadClobberOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(ReadClobberOptimization.matchReadClobber(ir, index)) {
                result.add(ir.get(index + 1));
                index += 2;
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
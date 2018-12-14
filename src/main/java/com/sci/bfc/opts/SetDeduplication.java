package com.sci.bfc.opts;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Set;

import java.util.ArrayList;
import java.util.List;

public final class SetDeduplication implements Optimization {
    public static final SetDeduplication INSTANCE = new SetDeduplication();

    private SetDeduplication() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            final Instruction insn = ir.get(index);
            if(insn instanceof Set) {
                int j = index + 1;

                while(j < ir.size() && ir.get(j) instanceof Set) {
                    j++;
                }

                result.add(ir.get(j - 1));
                index = j;
            } else {
                result.add(insn);
                index++;
            }
        }

        return result;
    }
}
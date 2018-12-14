package com.sci.bfc.opts;

import com.sci.bfc.ir.Adjust;
import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Select;

import java.util.ArrayList;
import java.util.List;

public final class Contraction implements Optimization {
    private static boolean isContractable(final Instruction insn) {
        return insn instanceof Adjust || insn instanceof Select;
    }

    private static int getDelta(final Instruction insn) {
        if(!Contraction.isContractable(insn))
            throw new IllegalArgumentException(insn.getClass().toString() + " is not Contractible");
        if(insn instanceof Adjust) {
            return ((Adjust) insn).delta;
        } else {
            return ((Select) insn).delta;
        }
    }

    public static final Contraction INSTANCE = new Contraction();

    private Contraction() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            final Instruction insn = ir.get(index);
            if(Contraction.isContractable(insn)) {
                final Class<?> clazz = insn.getClass();
                int delta = Contraction.getDelta(insn);

                int j = index + 1;
                while(j < ir.size() && clazz.isInstance(ir.get(j))) {
                    delta += Contraction.getDelta(ir.get(j));
                    j++;
                }

                try {
                    result.add((Instruction) clazz.getConstructor(int.class).newInstance(delta));
                } catch(final Throwable t) {
                    throw new RuntimeException(t);
                }

                index = j;
            } else {
                result.add(insn);
                index++;
            }
        }

        return result;
    }
}
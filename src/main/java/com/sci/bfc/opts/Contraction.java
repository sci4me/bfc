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

    private static boolean match(final Instruction a, final Instruction b) {
        if(!a.getClass().equals(b.getClass())) return false;
        return a.baseOffset() == b.baseOffset();
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
                int delta = Contraction.getDelta(insn);

                int j = index + 1;
                while(j < ir.size() && Contraction.match(insn, ir.get(j))) {
                    delta += Contraction.getDelta(ir.get(j));
                    j++;
                }

                if(insn instanceof Adjust) {
                    result.add(new Adjust(insn.baseOffset(), delta));
                } else if(insn instanceof Select) {
                    result.add(new Select(delta));
                } else {
                    throw new RuntimeException();
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
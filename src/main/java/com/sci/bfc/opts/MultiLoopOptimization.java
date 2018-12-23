package com.sci.bfc.opts;

import com.sci.bfc.ir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class MultiLoopOptimization implements Optimization {
    private static boolean matchMultiLoop(final List<Instruction> ir, final int index) {
        if(!(ir.get(index) instanceof Open)) return false;

        int i = index + 1;
        while(i < ir.size()) {
            final Instruction insn = ir.get(i);
            if(insn instanceof Close) break;
            if(!(insn instanceof Adjust || insn instanceof Select)) return false;
            i++;
        }

        return true;
    }

    public static final MultiLoopOptimization INSTANCE = new MultiLoopOptimization();

    private MultiLoopOptimization() {
    }

    @Override
    public List<Instruction> optimize(final List<Instruction> ir) {
        final List<Instruction> result = new ArrayList<>();

        int index = 0;
        while(index < ir.size()) {
            if(MultiLoopOptimization.matchMultiLoop(ir, index)) {
                final int start = index;
                final Map<Integer, Integer> deltas = new HashMap<>();

                final BiConsumer<Integer, Integer> adjust = (offset, delta) -> {
                    if(deltas.containsKey(offset)) {
                        deltas.put(offset, deltas.get(offset) + delta);
                    } else {
                        deltas.put(offset, delta);
                    }
                };

                int dp = 0;
                while(true) {
                    index++;

                    final Instruction insn = ir.get(index);
                    if(insn instanceof Adjust) {
                        adjust.accept(dp, ((Adjust) insn).delta);
                    } else if(insn instanceof Select) {
                        dp += ((Select) insn).delta;
                    } else {
                        index++;
                        break;
                    }
                }

                if(dp == 0 && deltas.size() >= 2 && deltas.containsKey(0) && deltas.get(0) == -1) {
                    deltas.remove(0);

                    result.add(new Open());

                    for(final Map.Entry<Integer, Integer> entry : deltas.entrySet()) {
                        if(entry.getValue() == 0) continue;
                        result.add(new MAdd(entry.getKey(), entry.getValue()));
                    }

                    result.add(new Set(0, 0));
                    result.add(new Close());
                } else {
                    for(int i = start; i < index; i++) result.add(ir.get(i));
                }
            } else {
                result.add(ir.get(index));
                index++;
            }
        }

        return result;
    }
}
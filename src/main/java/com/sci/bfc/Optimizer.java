package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.opts.Optimization;

import java.util.ArrayList;
import java.util.List;

public final class Optimizer {
    private final boolean debug;
    private final List<Optimization> opts;

    public Optimizer(final boolean debug) {
        this.debug = debug;
        this.opts = new ArrayList<>();
    }

    public void addPass(final Optimization opt) {
        this.opts.add(opt);
    }

    public List<Instruction> optimize(final List<Instruction> ir) {
        List<Instruction> current = ir;
        int lastSize;

        if(this.debug) System.out.println("Optimizing:");

        do {
            lastSize = current.size();

            for(final Optimization opt : this.opts) {
                int prevSize = current.size();

                current = opt.optimize(current);

                if(this.debug) {
                    final int diff = prevSize - current.size();
                    if(diff > 0)
                        System.out.printf("    - %s removed %d instructions\n", opt.getClass().getSimpleName(), diff);
                }
            }
        } while(current.size() != lastSize);

        if(this.debug)
            System.out.printf("Optimizations removed a total of %d instructions\n\n", ir.size() - current.size());

        return current;
    }
}
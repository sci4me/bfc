package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Parser;
import com.sci.bfc.util.jit.JIT;
import com.sci.bfc.util.jit.Program;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class OptimizationTests {
    private String readFile(final String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(OptimizationTests.class.getResource(file).toURI())));
        } catch(final Throwable t) {
            return null;
        }
    }

    private Program run(final List<Instruction> ir, final int tapeSize, final List<Integer> stdin) {
        final JIT jit = new JIT(ir);
        final Program program = jit.compile(stdin, tapeSize);
        program.run();
        return program;
    }

    @Test
    public void optimizationsPreserveFunctionality() {
        final int tapeSize = 30000;

        final String[] files = new String[]{
//                "/opts/contraction.bf",
//                "/opts/clear_loop_removal.bf",
//                "/opts/clear_adjust_optimization.bf",
//                "/opts/set_deduplication.bf",
//                "/opts/null_instruction_removal.bf",
//                "/fib2.bf",
//                "/hw.bf",
//                "/dbfi_hw.bf",
//                "/dbfi_squares.bf",
//                "/hanoi.bf",
//                "/sierpinski.bf",
                "/mandelbrot.bf"
        };

        for(final String file : files) {
            try {
                System.out.println("Running regression test for '" + file + "' ...");

                final List<Instruction> ir = Parser.parse(this.readFile(file));

                final String in = this.readFile(file + ".in");
                final List<Integer> stdin;
                if(in != null) {
                    stdin = in.chars().boxed().collect(Collectors.toList());
                } else {
                    stdin = new ArrayList<>();
                }

                final Optimizer optimizer = new Optimizer(false);
                optimizer.addStandardPasses();

                final List<Instruction> optimized = optimizer.optimize(ir);

                final Program expected = this.run(ir, tapeSize, stdin);
                final Program actual = this.run(optimized, tapeSize, stdin);

                if(!Arrays.equals(expected.getTape(), actual.getTape()))
                    throw new RuntimeException("Tape mismatch");

                if(!expected.getOutput().equals(actual.getOutput()))
                    throw new RuntimeException("Output mismatch");

                if(expected.getDP() != actual.getDP())
                    throw new RuntimeException("Data Pointer mismatch");

                System.out.println("    - SUCCSES\n");
            } catch(final Throwable t) {
                System.err.println("Regression in '" + file + "'");
                throw t;
            }
        }
    }
}
package com.sci.bfc;

import com.sci.bfc.ir.Instruction;
import com.sci.bfc.ir.Parser;
import com.sci.bfc.util.IRRunner;
import com.sci.bfc.util.jit.JIT;
import com.sci.bfc.util.jit.Program;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class OptimizationTests {
    private String readFile(final String file) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(OptimizationTests.class.getResource(file).toURI())));
    }

    private Program run(final List<Instruction> ir, final int tapeSize, final List<Integer> stdin) {
        final JIT jit = new JIT(ir);
        final Program program = jit.compile(stdin, tapeSize);
        program.run();
        return program;
    }

    @Test
    public void optimizationsPreserveFunctionality() throws IOException, URISyntaxException {
        final int tapeSize = 30000;

        final String[] files = new String[]{
                "/opts/contraction.bf",
                "/opts/clear_loop_removal.bf",
                "/opts/clear_adjust_optimization.bf",
                "/opts/set_deduplication.bf",
                "/opts/null_instruction_removal.bf",

                "/fib2.bf",
                "/hw.bf",
                "/dbfi_hw.bf",
                "/dbfi_squares.bf",
                "/hanoi.bf"
        };

        for(final String file : files) {
            try {
                System.out.println("Running regression test for '" + file + "' ...");

                final String input = this.readFile(file);
                final String[] parts = input.split(";");
                final List<Instruction> ir = Parser.parse(parts[0]);

                final List<Integer> stdin;
                if(parts.length == 2) {
                    stdin = parts[1].chars().boxed().collect(Collectors.toList());
                } else {
                    stdin = new ArrayList<>();
                }

                final Optimizer optimizer = new Optimizer(false);
                optimizer.addStandardPasses();

                final List<Instruction> optimized = optimizer.optimize(ir);

                final IRRunner runner = new IRRunner(optimized, tapeSize, stdin);
                runner.run();
                System.out.print("Runner output: ");
                System.out.println(runner.getOutput().stream().map(n -> String.valueOf((char) n.intValue())).collect(Collectors.joining()));

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
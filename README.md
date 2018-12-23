# bfc
_bfc_ is an optimizing Brainfuck to C compiler.

## Intermediate Representation
The compiler uses the a linear IR consisting of instructions of the following types:
```
Adjust        :: + or -
Select        :: > or <
Read          :: ,
Write         :: .
Open          :: [
Close         :: ]
Set           :: generated by optimizations
MAdd          :: generated by optimizations
ScanLeft      :: [<]
ScanRight     :: [>]
```

The compiler parses Brainfuck code into the following instructions:
```
Adjust
Select
Read
Write
Open
Close
```

The rest are generated from the input IR by various optimizations.

## Optimizations
### Contraction
This optimization essentially performs "run-length encoding" on 'Adjust' instructions (`+` and `-`). For example: `+++` becomes `adjust 3`.

### Clear Loop Removal
This optimization turns loops of the following form: `[-]` into a single instruction: `set 0`.

### Scan Loop Optimization
This optimization acts on two specific instances of loops: `[>]` and `[<]`. The optimization generates code that uses `memchr` and `memrchr` respectively. On Windows, `memrchr` is not supported so `[<]` just turns into a regular scan loop.

### MultiLoop Optimization
This optimization acts on balanced loops (loops in which the data pointer ends where it began) that only contain adjust and select (`>` and `<`) instructions. It extracts 'multiply and add' (_MADD_) operations from the loop and generates individual instructions for them.

For example:
```
[
    -
    >
    +++
    >
    ++
    <<
]
```
Turns into:
```
madd [1] 3
madd [2] 2
set 0
```

Notice that this optimization only occurs if the loop decrements its condition cell by one.

### Adjust Set Optimization
This optimization simply removes adjust operations which occur directly before set operations as they are entirely redundant. For example:
```
adjust 5
set 0
```
Turns into:
```
set 0
```

### Set Deduplication
This optimization turns multiple adjacent set instructions into a single set instruction, namely, the last one; the preceeding ones are redundant. For example:
```
set 0
set 5
```
Turns into:
```
set 5
```

### Set Adjust Optimization
This optimization detects adjust instructions that occur directly after set instructions and combine them into a single set instruction. For example:
```
set 0
adjust 5
```
Turns into:
```
set 5
```

### Null Instruction Removal
This optimization removes adjust and select instructions whose value is zero. For example:
```
adjust 0
select 0
```
Would turn into nothing.

### Read Clobber Optimization
This optimization removes set or adjust instructions that occur directly before a read instruction as the read will overwrite them. For example:
```
adjust 5
read
```
Turns into:
```
read
```

### Offset Optimization
This optimization calculates data pointer offsets for certain instructions by counting the preceeding select instructions and adds those offsets to the instructions, allowing the select instructions to be eliminated. This is best explained visually:
```
adjust 3
select 5
adjust -2
select 2
set 0
select -1
read
open
...
close
```
Turns into:
```
adjust [0] 3
adjust [5] -2
set [7] 0
read [6]
select 6
open
...
close
```
#define _GNU_SOURCE
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
typedef unsigned char u8;
typedef unsigned long long u64;
static const u64 tape_size = %d;
#define ADJUST(base_offset, delta) *(dp + base_offset) += delta
#define SELECT(delta) dp += delta
#define READ(base_offset) *(dp + base_offset) = getchar()
#define WRITE(base_offset) putchar(*(dp + base_offset))
#define OPEN(loop) loop_##loop##_start: if(!*dp) goto loop_##loop##_end;
#define CLOSE(loop) if(*dp) goto loop_##loop##_start; loop_##loop##_end:
#define SET(base_offset, value) *(dp + base_offset) = value
#define MUL(offset, factor) *(dp + offset) += *dp * factor
#define SCAN_LEFT() dp -= (u64)((void*) dp - memrchr(tape, 0, (dp - tape + 1)))
#define SCAN_RIGHT() dp += (u64)(memchr(dp, 0, tape_size - (dp - tape)) - (void*) dp)
int main() {
    u8 *tape = (u8*) mmap(0, tape_size, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    u8 *dp = tape;
    %s
    munmap(tape, tape_size);
    return 0;
}
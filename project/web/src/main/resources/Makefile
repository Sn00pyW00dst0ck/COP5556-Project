CLANG := clang
LLC := llc
LD := wasm-ld

# Flags to compile to web assembly from either CLANG (going .c to .ll) or LLVM (from .ll to .wasm)
CLANGFLAGS := -S -emit-llvm --target=wasm32 -nostdinc -nostdlib -isystem ./node_modules/twr-wasm/include
LLCFLAGS := -mtriple=wasm32-unknown-unknown -filetype=obj
TWRCFLAGS := --no-entry --initial-memory=131072 --max-memory=131072 --export=main
LDFLAGS := --no-entry --export=main

TWRLIBC := ./node_modules/twr-wasm/lib-c/twr.a
DIST := static/wasm

.PHONY: default clean

# Usage: make <filename>.ll (from .c)
%.ll: %.c
	$(CLANG) $(CLANGFLAGS) $< -o $@

# Usage: make <filename>.wasm (from .ll)
%.wasm: %.ll | $(DIST)
	$(LLC) $(LLCFLAGS) $< -o $(DIST)/$*.o
	$(LD) $(DIST)/$*.o $(TWRLIBC) -o $(DIST)/$@ $(LDFLAGS)

# Create dist/ dir if it doesn't exist
$(DIST):
	mkdir -p $(DIST)

# Clean rule
.PHONY: clean
clean:
	rm -rf $(DIST)

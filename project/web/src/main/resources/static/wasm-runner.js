import { twrWasmModule } from "twr-wasm";

export async function runWasmBinary(wasmBinary) {
  const mod = new twrWasmModule();
  await mod.loadWasm(wasmBinary.buffer);
  await mod.callC(["main"]);
}

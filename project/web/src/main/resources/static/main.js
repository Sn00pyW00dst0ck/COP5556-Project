import { runWasmBinary } from "./wasm-runner.js";
import { encodeBase64 } from "./utils.js";

window.loadExample = function () {
  document.getElementById("code-editor").value = `
program HelloWorld;
begin
  writeln('Hello, WebAssembly!');
end.
  `;
};

window.compileAndRun = async function () {
  const code = document.getElementById("code-editor").value;
  const base64Source = encodeBase64(code);
  const response = await fetch("/compile", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      filename: "main.pas",
      sourceCode: base64Source,
    }),
  });

  const result = await response.json();
  if (!result.success) {
    alert("Compilation failed:\n" + result.message);
    return;
  }

  const wasmBinary = Uint8Array.from(atob(result.wasm), c => c.charCodeAt(0));
  await runWasmBinary(wasmBinary);
};

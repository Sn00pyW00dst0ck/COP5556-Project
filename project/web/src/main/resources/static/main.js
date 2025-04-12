import { twrWasmModule } from "twr-wasm";
import './editor.js';

const EDITOR = await window.MonacoEditorModule.initEditor('monaco-container');

const DEFAULT_TEXT = 
`program HelloWorld;
begin
    writeln('Hello, WebAssembly!');
end.`;

window.loadExample = () => EDITOR.setValue(DEFAULT_TEXT);

window.compileAndRun = async () => {
    await window.compile("main");
    window.clearConsole();
    await window.runWasm("main");
}

window.compile = async (filename) => {
    const response = await fetch("/compile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            filename: `${filename}.ll`, // TODO: SWAP .ll for .pas when compiler to .ll works in java
            sourceCode: btoa(EDITOR.getValue()),
        })
    });  

    const result = await response.json();
    if (!result.success) {
        alert("Compilation failed:\n" + result.message);
        return;
    }
}

window.clearConsole = () => document.getElementById("twr_iodiv").innerHTML = "";

window.runWasm = async (filename) => {
    const mod = new twrWasmModule();
    await mod.loadWasm(`/wasm/${filename}.wasm`);
    await mod.callC(["main"]);
}

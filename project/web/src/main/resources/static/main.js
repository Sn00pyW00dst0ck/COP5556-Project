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
    let filename = "main_" + (Math.random() + 1).toString(36).substring(7); // Append some randomness so we can avoid cache issues...
    await window.compile(filename);
    window.clearConsole();
    await new Promise(r => setTimeout(r, 1000)); // Give server time to update....
    await window.runWasm(filename);
}

window.compile = async (filename) => {
    const response = await fetch("/compile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        cache: "no-store",
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

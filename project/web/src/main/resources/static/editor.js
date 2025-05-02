/**
 * Use a CDN to initialize a Monaco Editor within a div of given ID. 
 * 
 * @param {string} containerId the id of the container to initialize the editor into
 * @returns the monaco editor instance once it has been setup.
 */
const initEditor = async (containerId) => {
    return new Promise((resolve) => {
        require.config({ paths: { 'vs': 'https://cdn.jsdelivr.net/npm/monaco-editor@latest/min/vs' } });

        require(['vs/editor/editor.main'], function () {
            const editor = monaco.editor.create(document.getElementById(containerId), {
                value: '',
                language: '',
                theme: 'vs-dark'
            });
            resolve(editor);
        });
    });
};
  
// Simulate Export
window.MonacoEditorModule = { initEditor };
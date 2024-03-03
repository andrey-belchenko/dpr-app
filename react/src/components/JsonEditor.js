import Editor from "@monaco-editor/react";

export default function Component({ value, onChange, readOnly }) {
  return (
    <Editor
      //   height="90vh"
      className="df-monaco-editor"
      defaultLanguage="json"
      // defaultValue={value}
      value={value}
      loading=""
      onChange={onChange}
      options={{
        readOnly: readOnly,
        minimap: { enabled: false }, // disable miniature map
        // lineNumbersMinChars: 3, // adjust line number width
        glyphMargin: false, // disable glyph margin
        // folding: false, // disable folding
        // Underscore (_) & dollar ($) are the only valid characters
        // wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\@\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,
        unicodeHighlight: {
          ambiguousCharacters: false,
        },
        // wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\@\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s\u0400-\u04FF]+)/g,
        // occurrencesHighlight: false, // disable occurrences highlight
        renderLineHighlight: "none", // disable line highlight
        // selectionHighlight: false, // disable selection highlight
        // matchBrackets: "never", // disable match brackets
        // overviewRulerBorder: false, // disable overview ruler border
        // hideCursorInOverviewRuler: true, // hide cursor in overview ruler
        scrollBeyondLastLine: false, // disable scroll beyond last line
        renderFinalNewline: false, // disable render final newline
        // rulers: [], // disable rulers
        // overviewRulerLanes: 0, // disable overview ruler lanes
        automaticLayout: true, // enable automatic layout
      }}
    />
  );
}

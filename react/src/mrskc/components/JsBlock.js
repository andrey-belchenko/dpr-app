import { CopyBlock, a11yLight } from "react-code-blocks";

export default function Component({ code }) {
  return (
    <CopyBlock
      text={code}
      language="javascript"
      showLineNumbers={false}
      theme={a11yLight}
      codeBlock
    />
  );
}

import { Button } from "devextreme-react/button";
// import Hint from "../../common/components/hint/Hint";
import { useState } from "react";
import { Hint } from "src/common/components";
import { v4 as uuidv4 } from "uuid";
export default function Component({ icon, text, onClick, children }: any) {
  
  const idVal = "a"+uuidv4().replaceAll("-","");
  const [id, setId] = useState(idVal);
  return (
    <div>
      <Button
        onClick={onClick}
        icon={icon}
        text={text}
        className="df-button-with-info"
      ></Button>
      <i id={id} className="dx-icon-info df-button-info"></i>
      <Hint target={"#" + id}>{children}</Hint>
    </div>
  );
}

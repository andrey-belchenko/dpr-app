import React from "react";
import { Split } from "@geoffcox/react-splitter";
import "./Splitter.scss";
import Box, { Item as BoxItem } from "devextreme-react/box";
import Resizable from "devextreme-react/resizable";

export default function Component({ horizontal, children , onlyFirst }: any) {

  if (onlyFirst) {
    return (
      <Split horizontal={horizontal} initialPrimarySize="100%" minPrimarySize="100%" splitterSize="0px">
        {children}
      </Split>
    );
  }
  
  return (
    <Split horizontal={horizontal} splitterSize="10px">
      {children}
    </Split>
  );
}

// export default function Component({ horizontal, children }: any) {
//   return (
//     <Box direction="row" className="df-form-box">
//       {/* TODO: хорошо бы избавиться от явного определения размера 39 */}
//       <BoxItem ratio={1}>{children[0]}</BoxItem>
//       <BoxItem ratio={0} baseSize={10}><div></div></BoxItem>
//       <BoxItem ratio={1}>{children[1]}</BoxItem>
//     </Box>
//   );
// }

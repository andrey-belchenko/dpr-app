import { Popup, Position, ToolbarItem } from "devextreme-react/popup";
import React, { useCallback, useState } from "react";
const Component = ({
  visible,
  width,
  height,
  title,
  onHiding,
  children,
}: any) => {
  // const [isVisible, setIsVisible] = useState(visible);

  const [currentWidth,setCurrentWidth] =  useState(width)
  const [currentHeight,setCurrentHeight] =  useState(height)

  const onResizeEnd =  useCallback((e)=>{
      setCurrentWidth(e.width)
      setCurrentHeight(e.height)
  },[])
  return (
    <Popup
      visible={visible}
      dragEnabled={true}
      hideOnOutsideClick={true}
      showCloseButton={true}
      showTitle={true}
      title={title}
      container=".dx-viewport"
      width={currentWidth}
      height={currentHeight}
      onHiding={onHiding}
      resizeEnabled={true}
      onResizeEnd={onResizeEnd}
    >
      <Position at="center" my="center" collision="fit" />
      <div className="df-popup-window-content">{children}</div>
    </Popup>
  );
};

export default Component;

// export default React.memo(component)

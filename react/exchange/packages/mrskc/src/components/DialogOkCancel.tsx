import { Popup, Position, ToolbarItem } from "devextreme-react/popup";
import { useEffect, useState } from "react";
export default function Component({
  visible,
  width,
  height,
  title,
  onOk,
  onCancel,
  children,
}: any) {
  // const [isVisible, setIsVisible] = useState(visible);

  const okButtonOptions = {
    text: "ОК",
    onClick: onOk,
  };
  const closeButtonOptions = {
    text: "Отменить",
    onClick: onCancel,
  };
  return (
    <Popup
      visible={visible}
      dragEnabled={false}
      hideOnOutsideClick={false}
      showCloseButton={false}
      showTitle={true}
      title={title}
      container=".dx-viewport"
      width={width}
      height={height}
    >
      <Position
        at="center"
        my="center"
        collision="fit"
      />
      <ToolbarItem
        widget="dxButton"
        toolbar="bottom"
        location="before"
        options={okButtonOptions}
      />
      <ToolbarItem
        widget="dxButton"
        toolbar="bottom"
        location="after"
        options={closeButtonOptions}
      />
      <div style={{textAlign:"center",fontSize:"15px"}} className="df-text">{children}</div>
    </Popup>
  );
}

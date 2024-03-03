import { Popup, Position, ToolbarItem } from "devextreme-react/popup";
export default function Component({
  visible,
  width,
  height,
  title,
  onOk,
  children,
}: any) {
  // const [isVisible, setIsVisible] = useState(visible);

  const okButtonOptions = {
    text: "ОК",
    onClick: onOk,
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
      resizeEnabled={false}
    >
      <Position
        at="center"
        my="center"
        collision="fit"
      />
      <ToolbarItem
        widget="dxButton"
        toolbar="bottom"
        location="center"
        options={okButtonOptions}
      />
      <div style={{textAlign:"center",fontSize:"15px"}} className="df-text">{children}</div>
    </Popup>
  );
}

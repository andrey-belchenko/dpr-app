import Popover from "devextreme-react/popover";
export default function Component({target, children, width , position}: any) {
  // if (!width){
  //    width = 300
  // }
  if (!position){
    position = "top"
 }
  return (
    <Popover
      target={target}
      showEvent="mouseenter"
      hideEvent="mouseleave"
      position={position}
      width={width}
    >
      {children}
    </Popover>
  );
}

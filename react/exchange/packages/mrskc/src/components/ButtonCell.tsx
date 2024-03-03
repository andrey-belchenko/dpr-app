import Button from "devextreme-react/button";

export default function Component({ children, text, textAlign }: any) {
  if (text) {
    var prev = []
    // todo, костыль сделать нормально если будет еще использоваться
    if (textAlign == "center") {
        prev.push(<div style={{width:"20px"}}></div>)
    }
    return (
      <div className="df-grid-button-cell">
        {prev}
        <div>{text}</div>
        <div className="df-grid-button-container">{children}</div>
      </div>
    );
  } else {
    return <div className="df-grid-button-container">{children}</div>;
  }
}

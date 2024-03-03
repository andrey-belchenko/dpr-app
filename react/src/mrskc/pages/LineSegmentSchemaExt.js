import React from "react";
import Diagram, {
  Nodes,
  Edges,
  AutoLayout,
  Toolbox,
  Group,
  CustomShape,
} from "devextreme-react/diagram";

import { createDataSource, createArrayStore } from "src/mrskc/data/apiClient";

import { renderEnd, renderNode, renderSwitch } from "./LineSegmentSchema";
import { renderTower } from "./LineSpanSchema";
export const baseColor = "#03A9F4";
const renderSegment = (data, index) => {
  return (
    <svg width="100%" viewBox="0 0 200 200">
      <circle
        cx="100"
        cy="100"
        r="60"
        stroke={baseColor}
        stroke-width="40"
        fill="white"
      />
    </svg>
  );
};
// todo кешировать
const renderSwitch1 = (data, index) => {
  let boxSize = 100;
  let height = 80;
  let width = 70;
  let lineThickness = 15;
  let xPadding = (boxSize - width) / 2;
  let yPadding = (boxSize - height) / 2;

  let rect1X = xPadding;
  let rect1Y = yPadding;
  let rect1Width = width;
  let rect1Height = lineThickness;

  let rect2X = xPadding;
  let rect2Y = boxSize - yPadding - lineThickness;
  let rect2Width = width;
  let rect2Height = lineThickness;

  let rect3X = xPadding + width / 2 - lineThickness / 2;
  let rect3Y = yPadding;
  let rect3Width = lineThickness;
  let rect3Height = height;

  return (
    <svg width="100%" viewBox="0 0 100 100">
      <rect
        x={rect1X}
        y={rect1Y}
        width={rect1Width}
        height={rect1Height}
        style={{ fill: baseColor, strokeWidth: 0 }}
      />
      <rect
        x={rect2X}
        y={rect2Y}
        width={rect2Width}
        height={rect2Height}
        style={{ fill: baseColor, strokeWidth: 0 }}
      />
      <rect
        x={rect3X}
        y={rect3Y}
        width={rect3Width}
        height={rect3Height}
        style={{ fill: baseColor, strokeWidth: 0 }}
      />
    </svg>
  );
};

const customShapeTemplate = (item) => {
  const dataItem = item.dataItem;
  return (
    <svg>
      <text style={{fill:"red"}} x="50%" y="20%">
        text1 text1 text1 text1 text1 text1 text1
      </text>
      <text x="50%" y="45%">
        text2 text2 text2 text2 text2 text2 text2 
      </text>
    </svg>
  );
};

class Component extends React.Component {
  constructor(props) {
    super(props);

    this.state = {};
  }

  componentWillMount() {
    this.getData();
  }

  async getData() {
    let state = {
      flowNodesDataSource: await createArrayStore(
        "view_topology_LineSegmentSchemaNodes",
        "id"
      ),
      flowEdgesDataSource: await createArrayStore(
        "view_topology_LineSegmentSchemaEdges",
        "id"
      ),
    };
    this.setState(state);
  }

  render() {
    const nodeSize = 0.2;
    const switchSize = 0.25;
    return (
      <Diagram
        id="diagram"
        className="diagram"
        showGrid={false}
        simpleView={true}
        autoZoomMode="fitWidth"
        useNativeScrolling={true}
        toolbox={{ visibility: "disabled" }}
        snapToGrid={false}
      >
        <CustomShape
          type="df.node"
          minHeight={nodeSize}
          maxHeight={nodeSize}
          minWidth={nodeSize}
          maxWidth={nodeSize}
          defaultHeight={nodeSize}
          defaultWidth={nodeSize}
          render={renderNode}
        ></CustomShape>
        <CustomShape
          type="df.switch"
          minHeight={switchSize}
          maxHeight={switchSize}
          minWidth={switchSize}
          maxWidth={switchSize}
          defaultHeight={switchSize}
          defaultWidth={switchSize}
          render={renderSwitch}
        ></CustomShape>
        <CustomShape
          type="df.endElement"
          baseType="text"
          render={renderEnd}
        ></CustomShape>
        <CustomShape
          type="df.segment"
          // baseType="text"
          baseType="rectangle"
          // textExpr = {() => null}
          textTop = {0.5}
          render={customShapeTemplate}
        ></CustomShape>
        <Nodes
          dataSource={this.state.flowNodesDataSource}
          idExpr="id"
          typeExpr="type"
          textExpr={(obj, value) => {
            if (obj.type === "df.segment") {
              return "fsdf sdf sd f sd f dsfdsfsdssf sdfsd f ds fd sf ds fsdfsfdsfds sdaf sd fa sd fa s fa f safahgfjkgkjgh g kjgkjhg kjhgkhgkjhg kjhg kjh kjhgkjhg kjg kghkhgkjg kgh!!!";
            } else {
              return obj.text;
            }
          }}
          autoLayout={{ orientation: "vertical", type: "layered" }}
          // leftExpr = {()=>0}
        ></Nodes>
        <Edges
          dataSource={this.state.flowEdgesDataSource}
          idExpr="id"
          textExpr="text"
          fromExpr="fromId"
          toExpr="toId"
          toLineEndExpr="toLineEnd"
          // fromPointIndexExpr="fromPointIndex"
          // toPointIndexExpr="toPointIndex"
          // lineTypeExpr ={()=>"straight"}
          styleExpr={() => {
            return { stroke: "#999999" };
          }}
        />
        <Toolbox>
          <Group category="general" title="General" />
        </Toolbox>
      </Diagram>
    );
  }
}

export default Component;

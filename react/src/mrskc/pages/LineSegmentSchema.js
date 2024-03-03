import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link,
  useParams,
} from "react-router-dom";

import React, { useState, useEffect } from "react";

import Diagram, {
  Nodes,
  Edges,
  AutoLayout,
  Toolbox,
  Group,
  CustomShape,
} from "devextreme-react/diagram";

import { createArrayStore } from "src/common/data/apiClient";

export const baseColor = "#03A9F4";
export const redColor = "#FF8282";
export const grayColor = "#AAAAAA";

export const renderNode = (data, index) => {
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

export const renderEnd = (data, index) => {
  var color = baseColor;
  if (data.dataItem.isConsumerEquipment) {
    color = grayColor;
  } else if (!data.text) {
    color = "red";
  }
  return (
    <svg width="100%" height="100%" viewBox="0 0 100 100">
      <rect
        x="10"
        y="10"
        width="80"
        height="80"
        style={{ fill: color, strokeWidth: 0, fillOpacity: 0.3 }}
      />
    </svg>
  );
};

export const renderSwitch = (data, index) => {
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
  let switchColor = baseColor;

  if (data.dataItem.isConsumerEquipment) {
    switchColor = grayColor;
  } else if (!data.dataItem.switchCode) {
    switchColor = redColor;
  }
  return (
    <svg width="100%" viewBox="0 0 100 100">
      <rect
        x={rect1X}
        y={rect1Y}
        width={rect1Width}
        height={rect1Height}
        style={{ fill: switchColor, strokeWidth: 0 }}
      />
      <rect
        x={rect2X}
        y={rect2Y}
        width={rect2Width}
        height={rect2Height}
        style={{ fill: switchColor, strokeWidth: 0 }}
      />
      <rect
        x={rect3X}
        y={rect3Y}
        width={rect3Width}
        height={rect3Height}
        style={{ fill: switchColor, strokeWidth: 0 }}
      />
    </svg>
  );
};

export default function Component() {
  // We can use the `useParams` hook here to access
  // the dynamic pieces of the URL.

  const [data, setData] = useState(null);
  let { lineCode } = useParams();
  useEffect(() => {
    // define async function to fetch data

    async function getData() {
      let data = await loadData(lineCode);
      setData(data);
    }
    getData(); // call the function to fetch user data
  }, [lineCode]); // only run effect when id changes

  if (!data) {
    return <div>Loading...</div>; // display loading message while data is being fetched
  }
  document.title = `${data.lineCode} сегменты`;
  return diagram(data);
}

const loadData = async (lineCode) => {
  let filter = { lineCode: lineCode };
  let data = {
    lineCode: lineCode,
    nodes: await createArrayStore(
      "view_topology_LineSegmentSchemaNodes",
      "id",
      filter
    ),
    edges: await createArrayStore(
      "view_topology_LineSegmentSchemaEdges",
      "id",
      filter
    ),
  };
  return data;
};

function diagram(data) {
  const nodeSize = 0.2;
  const switchSize = 0.25;
  return (
    <Diagram
      id="diagram"
      className="diagram"
      showGrid={false}
      simpleView={true}
      // autoZoomMode="fitWidth" // ломает компонент в прод. сборке
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
        baseType="text"
        minWidth={1.5}
      ></CustomShape>
      <Nodes
        dataSource={data.nodes}
        idExpr="id"
        typeExpr="type"
        textExpr="text"
        autoLayout={{ orientation: "vertical", type: "layered" }}

        // leftExpr = {()=>0}
      ></Nodes>
      <Edges
        dataSource={data.edges}
        idExpr="id"
        textExpr={(obj) => {
          if (!obj.text) {
            return "";
          }
          const list = obj.text.split("-");
          if (list.length > 1) {
            return list[list.length - 2] + "-" + list[list.length - 1];
          }
          return "";
        }}
        fromExpr="fromId"
        toExpr="toId"
        toLineEndExpr="toLineEnd"
        // fromPointIndexExpr="fromPointIndex"
        // toPointIndexExpr="toPointIndex"
        // lineTypeExpr ={()=>"straight"}
        styleExpr={(obj) => {
          if (obj.needMatch) {
            return { stroke: redColor };
          }
          if (obj.matched) {
            return { stroke: baseColor };
          }

          return { stroke: "#AAAAAA" };
        }}
      />
      <Toolbox>
        <Group category="general" title="General" />
      </Toolbox>
    </Diagram>
  );
}

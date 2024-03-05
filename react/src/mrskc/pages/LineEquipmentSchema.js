import React, { useState, useEffect } from "react";
import Diagram, {
  Nodes,
  Edges,
  AutoLayout,
  Toolbox,
  Group,
  CustomShape,
} from "devextreme-react/diagram";

import { createDataSource, createArrayStore } from "src/mrskc/data/exchange-processor-api";
import { renderEnd, renderNode, renderSwitch } from "./LineSegmentSchema";

import { useParams } from "react-router-dom";

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
      "view_topology_LineEquipmentSchemaNodes",
      "id",
      filter
    ),
    edges: await createArrayStore(
      "view_topology_LineEquipmentSchemaEdges",
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
        textExpr="label"
        fromExpr="fromId"
        toExpr="toId"
        // toLineEndExpr="toLineEnd"
        // fromPointIndexExpr="fromPointIndex"
        // toPointIndexExpr="toPointIndex"
        lineTypeExpr={() => "straight"}
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

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

import { createArrayStore } from "src/data/apiClient";
import { renderSwitch, renderEnd } from "./LineSegmentSchema";
const colors = [
  // "#03A9F4",
  // "#FFC300",
  // "#FF5733",
  // "#C70039",
  // "#900C3F",
  // "#581845",
  // "#4661EE",

  //  "#EC5657",
  //  "#1BCDD1",
  //  "#8FAABB",
  //  "#B08BEB",
  //  "#3EA0DD",
  //  "#F5A52A",
  //  "#23BFAA",
  //  "#FAA586",
  //  "#EB8CC6",
  "yellow",
  "red",
  "green",
  "blue",
  "purple",
  "brown",
  "orange",
];

// todo кешировать
export const renderTower = (data, index) => {
  return (
    <svg width="100%" viewBox="0 0 200 200">
      <circle
        cx="100"
        cy="100"
        r="60"
        stroke={colors[data.dataItem.aplIndex % colors.length]}
        stroke-width="40"
        fill="white"
      />
    </svg>
  );
};

const loadData = async (lineCode) => {
  let filter = { lineCode: lineCode };
  let data = {
    lineCode: lineCode,
    nodes: await createArrayStore(
      "view_topology_LineSpanSchemaNodes",
      "id",
      filter
    ),
    edges: await createArrayStore(
      "view_topology_LineSpanSchemaEdges",
      "id",
      filter
    ),
  };
  return data;
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
  document.title = `${data.lineCode} пролеты`;
  return diagram(data);
}

function diagram(data) {
  const switchSize = 0.25;
  const towerHeight = 0.15;
  const towerWidth = 3.5;
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
          type="df.tower"
          minHeight={towerHeight}
          maxHeight={towerHeight}
          minWidth={towerWidth}
          maxWidth={towerWidth}
          defaultHeight={towerHeight}
          defaultWidth={towerWidth}
          render={renderTower}
          styleExpr={(obj, value) => {
            return { stroke: "#999999" };
          }}
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
          // textExpr="label"
          textExpr={(obj) => {
            let text = obj.label;
            if (!text) {
              return "";
            }
            if (obj.type == "df.tower" || obj.type == "df.endElement") {
              const list = text.split("-");
              if (list.length > 1) {
                return list[list.length - 2] + "-" + list[list.length - 1];
              }
            }
            return text;
          }}
          autoLayout={{ orientation: "vertical", type: "layered" }}
        ></Nodes>
        <Edges
          dataSource={data.edges}
          idExpr="id"
          // textExpr="text"
          textExpr={(obj) => {
            let text = obj.text;
            if (text) {
              const list = text.split("-");
              if (list.length > 1) {
                text = list[list.length - 2] + "-" + list[list.length - 1];
              }
            }
            if (obj.segmentIndex >= 0) {
              text = "[" + obj.segmentIndex + "] " + text;
            }
            if (obj.wireInfo) {
              text += " " + obj.wireInfo;
            }
            return text;
          }}
          fromExpr="fromId"
          toExpr="toId"
          toLineEndExpr="toLineEnd"
          lineTypeExpr={() => "straight"}
          styleExpr={(obj) => {
            if (obj.isInUse) {
              return { stroke: "#999999" };
            }
            return { stroke: "red" };
          }}
        />

        <Toolbox>
          <Group category="general" title="General" />
        </Toolbox>
      </Diagram>
  );
}

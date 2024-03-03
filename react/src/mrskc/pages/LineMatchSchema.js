import React from "react";
import Diagram, {
  Nodes,
  Edges,
  AutoLayout,
  Toolbox,
  Group,
  CustomShape,
} from "devextreme-react/diagram";

import { createDataSource, createArrayStore } from "src/common/data/apiClient";

import {renderEnd,renderNode,renderSwitch} from "./LineSegmentSchema"
import {renderTower} from "./LineSpanSchema"

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
        "view_topology_LineMatchSchemaNodes",
        "id"
      ),
      flowEdgesDataSource: await createArrayStore(
        "view_topology_LineMatchSchemaEdges",
        "id"
      ),
    };
    this.setState(state);
  }
  

  render() {
    const towerHeight = 0.15;
    const towerWidth = 3.5;
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
          type="df.tower"
          minHeight={towerHeight}
          maxHeight={towerHeight}
          minWidth={towerWidth}
          maxWidth={towerWidth}
          defaultHeight={towerHeight}
          defaultWidth={towerWidth}
          render={renderTower}
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
          dataSource={this.state.flowNodesDataSource}
          idExpr="id"
          typeExpr="type"
          textExpr="text"
          autoLayout={{ orientation: "vertical", type: "layered" }}
        ></Nodes>
        <Edges
          dataSource={this.state.flowEdgesDataSource}
          idExpr="id"
          textExpr="text"
          fromExpr="fromId"
          toExpr="toId"
          toLineEndExpr="toLineEnd"
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
}

export default Component;

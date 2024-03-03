import React from 'react';
import Diagram, {
  Nodes, AutoLayout, Toolbox, Group,
} from 'devextreme-react/diagram';
import ArrayStore from 'devextreme/data/array_store';
import service from './data.js';


class TestDiagram extends React.Component {
  constructor(props) {
    super(props);

    this.dataSource = new ArrayStore({
      key: 'ID',
      data: service.getEmployees(),
    });
  }

  render() {
    return (
      <Diagram id="diagram">
        <Nodes  keyExpr="ID" textExpr="Title" parentKeyExpr="Head_ID">
          <AutoLayout type="tree" />
        </Nodes>
        <Toolbox>
          <Group category="general" title="General" />
        </Toolbox>
      </Diagram>
    );
  }
}

export default TestDiagram;
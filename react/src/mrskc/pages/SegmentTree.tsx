import React from "react";
// import DataGrid, {
//   Paging,
//   FilterRow,
//   Scrolling,
//   SearchPanel,
// } from "devextreme-react/data-grid";
import { createDataSource } from "src/common/data/exchange-processor-api";
// import { TreeList } from "devextreme-react";
import TreeList, {
  Column,
  FilterRow,
  HeaderFilter,
} from "devextreme-react/tree-list";

// const dataSource = createDataSource("view_topology_LineSegmentTree", {
//   idField: "id",
// });
export default function Component() {
  return (
    <React.Fragment>
      <TreeList
        id="treelist"
        // dataSource={dataSource}
        keyExpr="id"
        parentIdExpr="parentId"
        autoExpandAll={true}
        rootValue={null}
        className={"diagram"}
        showBorders={false}
        focusedRowEnabled={true}
        defaultFocusedRowIndex={0}
        columnAutoWidth={true}
        columnHidingEnabled={false}
        showColumnLines={true}
        columns={[
          "наименованиеСегмента",
          "наименованиеЛинии",
          "наименованиеКонечногоЭлемента",
          "типКонечногоЭлемента",
          "кодКонечногоЭлемента",
        ]}
      >
        <FilterRow visible={true} />
        <HeaderFilter visible={true} />
      </TreeList>
    </React.Fragment>
  );
}

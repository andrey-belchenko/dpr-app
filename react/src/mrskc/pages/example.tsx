import React from "react";
import DataGrid, {
  Paging,
  FilterRow,
  Scrolling,
  SearchPanel,
  HeaderFilter,
} from "devextreme-react/data-grid";
import { createDataSource } from "src/common/data/apiClient";

export default function Component() {
  return (
    <React.Fragment>
      <DataGrid
        remoteOperations={true}
        className={"grid"}
        dataSource={createDataSource("model_Links")}
        showBorders={false}
        focusedRowEnabled={true}
        defaultFocusedRowIndex={0}
        columnAutoWidth={true}
        columnHidingEnabled={false}
        showColumnLines={true}
      >
        <Paging enabled={false} />
        <FilterRow visible={true} />
        <Scrolling
          useNative={true}
          // scrollByContent={true}
          // scrollByThumb={true}
          // showScrollbar="always"
          mode="virtual"
        />
        <SearchPanel visible={true} highlightSearchText={true} />
        <HeaderFilter visible={true} />
      </DataGrid>
    </React.Fragment>
  );
}
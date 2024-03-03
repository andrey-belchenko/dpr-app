import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import DataGrid, {
  Paging,
  FilterRow,
  Scrolling,
  SearchPanel,
  HeaderFilter,
  Column,
  FilterPanel,
  Summary,
  TotalItem,
  Editing,
  GroupPanel,
  Grouping,
  GroupItem,
  Toolbar,
  Item,
  StateStoring,
  Sorting,
  RemoteOperations,
} from "devextreme-react/data-grid";
import { createDataSource } from "src/mrskc/data/apiClient";
import Button from "devextreme-react/button";
import { exportGridToExcel } from "src/mrskc/utils/export-grid";
import { clearState } from "src/mrskc/utils/grid-state";
import { useQueryParam, StringParam } from "use-query-params";
import { useNavigate, useLocation } from "react-router-dom";
import dxDataGrid from "devextreme/ui/data_grid";
import { customProcessColumns, processColumns } from "src/mrskc/utils/grid-columns";
import { element } from "prop-types";

// Child component
interface GridProps {
  collectionName?: string;
  dataSourceOptions?: any;
  title: string;
  countByColumn: string; // Хотелось бы избавиться от этого параметра и показывать к-во всегда в первой колонке но не нашел простого способа как это сделать
  columns: any[];
  toolbarItemsRightBefore?: any[];
  toolbarItemsRightAfter?: any[];
  onEditorPreparing?: any;
  gridId?: string;
  children?: any;
  stateSavingEnabled?: boolean;
  filterPanel?: any;
  groupPanel?: any;
  grouping?: any;
  filterValue?: any;
  dataSource?: any;
  remoteOperations?: boolean;
  refreshButtonVisible?: boolean;
  // keyExpr?: any;
}

export interface GridRef {
  getInstance: () => dxDataGrid<any, any> | undefined;
  getCurrentDataItem: () => any | undefined;
}

// Можно передавать настройки колонок напрямую в свойство TreeList, но так почему то не работает опция renderCell на колонке
// TODO Возможно требуется какая-то оптимизация , чтобы исключить лишние вызовы
const createColumns = (columns?: any[]) => {
  const colElements: any[] = [];
  if (columns) {
    processColumns(columns);
    customProcessColumns(
      columns,
      (col, isRoot, childrenResults) => {
        let props = { ...col };
        if (col.columns) {
          delete props.columns;
        }
        let element = <Column {...props}>{childrenResults}</Column>;
        if (isRoot) {
          colElements.push(element);
        }
        return element;
      },
      true
    );
  }
  return colElements;
};

const component = React.forwardRef<GridRef, GridProps>((props, ref) => {
  const gridRef = useRef<DataGrid>(null);
  const getInstance = (): dxDataGrid<any, any> | undefined => {
    return gridRef.current?.instance;
  };
  const [currentDataItem, setCurrentDataItem] = useState<any>(undefined);
  const getCurrentDataItem = (): any | undefined => {
    return currentDataItem;
  };
  React.useImperativeHandle(ref, () => ({
    getInstance,
    getCurrentDataItem,
  }));

  const refreshGrid = useCallback(() => {
    if (gridRef.current) {
      gridRef.current.instance.refresh();
    }
  }, []);

  const [gridStateName] = useState((props.gridId || "grid") + "State");
  const [gridState, setGridState] = useQueryParam(gridStateName, StringParam);
  // const [data] = useState(
  //   createDataSource(props.collectionName, props.dataSourceOptions)
  // );
  const data = React.useMemo(() => {
    return (
      props.dataSource ??
      createDataSource(props.collectionName!, props.dataSourceOptions)
    );
  }, [props.dataSource]);
  const [lastState, setLastState] = useState("");

  useEffect(() => {
    if (props.stateSavingEnabled == false) return;
    let value = gridState;
    if (!value) {
      value = "";
    }
    if (gridState == lastState) {
      return;
    }
    let options: any = null;
    if (value) {
      options = JSON.parse(value);
    }
    gridRef.current?.instance.state(options);
  }, [gridState]);

  const saveState = useCallback(
    (state) => {
      if (!props.stateSavingEnabled == false) return;
      let value = "";
      let options = clearState(state);
      // let options = state;
      if (options) {
        value = JSON.stringify(options);
      }
      if (lastState != value) {
        setGridState(value);
      }
      setLastState(value);
    },
    [lastState]
  );

  let stateStoring: any[] = [];

  if (props.stateSavingEnabled != false) {
    stateStoring.push(
      <StateStoring
        enabled={true}
        type="custom"
        // customLoad={loadState}
        customSave={saveState}
        savingTimeout={300}
      />
    );
  }

  return (
    <React.Fragment>
      <DataGrid
        ref={gridRef}
        className={"grid"}
        dataSource={data}
        showBorders={false}
        focusedRowEnabled={true}
        defaultFocusedRowIndex={0}
        columnAutoWidth={true}
        columnHidingEnabled={false}
        showColumnLines={true}
        hoverStateEnabled={true}
        onEditorPreparing={props.onEditorPreparing}
        autoNavigateToFocusedRow={true} // не работает
        onFocusedRowChanged={(e) => {
          setCurrentDataItem(e.row?.data);
        }}
        filterValue={props.filterValue}
      >
        <RemoteOperations
          filtering={props.remoteOperations ?? true}
          sorting={props.remoteOperations ?? true}
          groupPaging={props.remoteOperations ?? true}
          summary={props.remoteOperations ?? true}
          grouping={props.remoteOperations ?? true}
        ></RemoteOperations>
        <Paging enabled={false} />
        <FilterRow visible={true} />
        <Scrolling useNative={true} mode="virtual" />
        <HeaderFilter visible={true} />
        <FilterPanel visible={props?.filterPanel?.visible ?? true} />
        <Summary>
          <TotalItem column={props.countByColumn} summaryType="count" />
          <GroupItem column={props.countByColumn} summaryType="count" />
        </Summary>

        <Toolbar>
          <Item location="before" visible={props.refreshButtonVisible ?? true}>
            <Button icon="refresh" text="Обновить" onClick={refreshGrid} />
          </Item>
          <Item name="groupPanel" location="before" />
          {props.toolbarItemsRightBefore}
          <Item location="after">
            <Button
              icon="export"
              text="Экспорт"
              onClick={() => exportGridToExcel(gridRef, props.title)}
            />
          </Item>
          {props.toolbarItemsRightAfter}
        </Toolbar>
        <Grouping
          contextMenuEnabled={props?.grouping?.contextMenuEnabled ?? true}
          autoExpandAll={false}
        />
        <GroupPanel visible={props?.groupPanel?.visible ?? true} />

        <Sorting mode="multiple" />
        {stateStoring}
        {props.children}
        {createColumns(props.columns)}
      </DataGrid>
    </React.Fragment>
  );
});

export default component;
// export default React.memo(component)

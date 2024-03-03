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
import { createDataSource, getArray } from "src/common/data/exchange-processor-api";
import Button from "devextreme-react/button";
import { exportGridToExcel } from "src/common/utils/export-grid";
import { clearState } from "src/common/utils/grid-state";
import { useQueryParam, StringParam } from "use-query-params";
import dxDataGrid from "devextreme/ui/data_grid";
import { customProcessColumns, processColumns } from "src/common/utils/grid-columns";
import { useCookies } from "react-cookie";
import { Switch } from "devextreme-react/switch";

// Child component
interface GridProps {
  collectionName?: string;
  dataSourceOptions?: any;
  title: string;
  countByColumn?: string; 
  columns?: any[];
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
  onRowPrepared?: any;
  allowColumnResizing?: boolean;
  // keyExpr?: any;
}

export interface GridRef {
  getInstance: () => dxDataGrid<any, any> | undefined;
  getCurrentDataItem: () => any | undefined;
}

// Можно передавать настройки колонок напрямую в свойство Grid, но так почему то не работает опция renderCell на колонке
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

const getColumnForCount = (
  columns?: any[],
  value?: string
): string | undefined => {
  if (value) {
    return value;
  }
  if (!columns || !columns[0]) {
    return "_id";
  }
  let getColumnDataField = (column: any): string | undefined => {
    if (column.dataField) {
      return column.dataField;
    } else {
      if (column.columns) {
        for (let child of column.columns) {
          let childVal = getColumnDataField(child);
          if (childVal) {
            return childVal;
          }
        }
      }
      return undefined;
    }
  };
  return getColumnDataField(columns[0]);
};

const component = React.forwardRef<GridRef, GridProps>((props, ref) => {
  const gridRef = useRef<DataGrid>(null);
  const getInstance = (): dxDataGrid<any, any> | undefined => {
    return gridRef.current?.instance;
  };

  const [lastChanged, setLastChanged] = useState<any>(undefined);
  const [prevLastChanged, setPrevLastChanged] = useState<any>(undefined);
  const [cookies, setCookie] = useCookies(["autoRefresh"]);
  // const [autoRefresh, setAutoRefresh] = useState(false);

  // useEffect(() => {
  //   setAutoRefresh(cookies.autoRefresh);
  // }, [cookies.autoRefresh]);

  // useEffect(() => {
  //   setCookie("autoRefresh", autoRefresh);
  // }, [autoRefresh]);

  useEffect(() => {
    if (!props.collectionName) {
      return;
    }
    const checkData = async () => {
      let result = await getArray(props.collectionName!, undefined, [
        {
          $group: {
            _id: null,
            value: {
              $max: "$changedAt",
            },
          },
        },
      ]);
      if (result.data.length > 0) {
        let value = result.data[0].value;
        setLastChanged(value);
      }
    };
    if (cookies.autoRefresh) {
      const interval = setInterval(checkData, 10000);
      return () => clearInterval(interval);
    }
  }, [cookies.autoRefresh, lastChanged]);

  useEffect(() => {
    if (lastChanged && prevLastChanged && lastChanged != prevLastChanged) {
      gridRef.current?.instance?.refresh();
    }
    setPrevLastChanged(lastChanged);
  }, [lastChanged, prevLastChanged]);

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

  let columnForCount = getColumnForCount(props.columns,props.countByColumn)
  return (
    <React.Fragment>
      <DataGrid
        ref={gridRef}
        // remoteOperations={true}
        className={"df-grid"}
        dataSource={data}
        showBorders={false}
        focusedRowEnabled={true}
        defaultFocusedRowIndex={0}
        columnAutoWidth={true}
        columnHidingEnabled={false}
        showColumnLines={true}
        hoverStateEnabled={true}
        onRowPrepared={props.onRowPrepared}
        // keyExpr={props.keyExpr}
        // columns={processColumns(props.columns)}
        onEditorPreparing={props.onEditorPreparing}
        autoNavigateToFocusedRow={true} // не работает
        onFocusedRowChanged={(e) => {
          setCurrentDataItem(e.row?.data);
        }}
        allowColumnResizing={props.allowColumnResizing}
        columnResizingMode="widget"
        // filterValue={props.filterValue}
        // filterPanel={props.filterPanel ?? { visible: true }}
        // groupPanel={props.groupPanel ?? { visible: true }}
        // grouping={props.grouping ?? { contextMenuEnabled:true,autoExpandAll:false}}

        // filterPanel={{ visible: true }}
        // groupPanel={ { visible: true }}
        // grouping={{ contextMenuEnabled:true,autoExpandAll:false}}
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
        <Scrolling
          useNative={true}
          // scrollByContent={true}
          // scrollByThumb={true}
          // showScrollbar="always"
          mode="virtual"
        />
        {/* <SearchPanel visible={true} highlightSearchText={true} /> */}
        <HeaderFilter visible={true} />
        <FilterPanel visible={props?.filterPanel?.visible ?? true} />
        <Summary>
          <TotalItem column={columnForCount} summaryType="count" />
          <GroupItem column={columnForCount} summaryType="count" />
        </Summary>

        <Toolbar>
          <Item location="before" visible={props.refreshButtonVisible ?? true}>
            <Button icon="refresh" text="Обновить" onClick={refreshGrid} />
          </Item>
          <Item location="before"></Item>
          <Item location="before" visible={props.refreshButtonVisible ?? true}>
            <Switch
              className="df-toolbar-switch"
              // text="Обновлять автоматически"
              hint="Автоматическое обновление"
              value={cookies.autoRefresh}
              defaultValue={false}
              onValueChanged={(e) => setCookie("autoRefresh", e.value)}
            />
          </Item>
          {/* TODO так подвинул панель группировки сделать в css */}
          <Item location="before"></Item>
          <Item location="before"></Item>
          <Item location="before"></Item>
          <Item location="before"></Item>

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

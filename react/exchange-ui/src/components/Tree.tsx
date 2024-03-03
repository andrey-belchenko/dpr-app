import React, { useCallback, useEffect, useRef, useState } from "react";
import { clearState } from "src/utils/grid-state";
import { useQueryParam, StringParam } from "use-query-params";
import dxTreeList from "devextreme/ui/tree_list";
import TreeCore, { TreeCoreRef } from "./TreeCore";
// import notify from "devextreme/ui/notify";

// Child component
interface TreeProps {
  collectionName: string;
  dataSourceOptions?: any;
  title: string;
  columns?: any[];
  toolbarItemsRightBefore?: any[];
  toolbarItemsRightAfter?: any[];
  onEditorPreparing?: any;
  onFocusedRowChanged?: any;
  onRowPrepared?: any;
  stateSavingEnabled?: boolean;
  toolbarVisible?: boolean;
  filterPanelVisible?: boolean;
  gridId?: string;
  children?: any;
}

export interface TreeRef {
  getInstance: () => dxTreeList<any, any> | undefined;
  setFocusedRow: (key: any) => void;
  update: (key: any, values: any) => Promise<any>;
  // getCurrentDataItem: () => any | undefined;
}

function consoleLog(message: any) {
  // console.log(message)
}

const component = React.forwardRef<TreeRef, TreeProps>((props, ref) => {
  const gridRef = useRef<TreeCoreRef>(null);

  const getInstance = (): dxTreeList<any, any> | undefined => {
    return gridRef.current?.getInstance();
  };

  const update = (key: any, values: any) => {
    return gridRef.current!!.update(key, values);
  };

  // const [initialized, setInitialized] = useState(false);

  const setFocusedRow = (key: any) => {
    setTimeout(async () => {
      // в TreeList плавающий баг с отрисовкой и фокусом, если передернуть по всякому вроде работает более менее стабильно.
      await gridRef.current?.getInstance()?.navigateToRow(key);
      let state = gridRef.current?.getInstance()?.state();
      state.focusedRowKey = key;
      gridRef.current?.getInstance()?.state(state);
      await gridRef.current?.getInstance()?.navigateToRow(key);
      gridRef.current?.getInstance()?.endUpdate();
      gridRef.current?.getInstance()?.refresh();
    }, 300);
  };

  // const getCurrentDataItem = (): any | undefined => {
  //   return gridRef.current?.getCurrentDataItem();
  // };
  React.useImperativeHandle(ref, () => ({
    getInstance,
    setFocusedRow,
    update,
    // getCurrentDataItem,
  }));

  //TODO сохранение состояние похоже у tree и  grid объединить по возможности
  // для дерева пришлось накрутить костылей

  const gridStateName = (props.gridId || "tree") + "State";

  // if (!window.URL.toString().includes(gridStateName + "=")) {
  //   if (!initialized) {
  //     setInitialized(true);
  //   }
  // }
  const [gridSavedState, setGridSavedState] = useQueryParam(
    gridStateName,
    StringParam
  );

  // const [gridSavedState,setGridSavedState] = useState("");
  // const [val, setVal] = useState<any>(0);
  // const [lastState, setLastState] = useState(0);

  const [vars] = useState<any>({
    // version: 0,
    currentState: "",
  });
  // const [isFistLoad, setIsFirstLoad] = useState(true);
  const [needNavigate, setNeedNavigate] = useState("");

  const applySavedOptions = (value: string) => {
    if (value) {
      // if (value == vars.version) {
      //   // consolelog("same:" + options.version);
      //   return;
      // }

      if (value == vars.currentState) {
        consoleLog("same:" + value);
        // if (!initialized) {
        //   setInitialized(true);
        // }
        return;
      }
      consoleLog("diff:" + value + "," + vars.currentState);
      // let isNew = vars.currentState == "";
      vars.currentState = value;
      let options = JSON.parse(value);
      // consolelog("got:" + options.version);

      let state = gridRef.current!.getInstance()!.state();

      gridRef.current!.getInstance()!.state(options);
      consoleLog("set:" + value);
      // if (!initialized) {
      //   setInitialized(true);
      // }

      if (
        // isNew &&
        options.focusedRowKey &&
        state.focusedRowKey != options.focusedRowKey
      ) {
        consoleLog("navigate:" + options.focusedRowKey);
        setNeedNavigate(options.focusedRowKey);
      }
    }
  };

  useEffect(() => {
    let value = gridSavedState ?? "";
    setTimeout(() => applySavedOptions(value), 300);
  }, [gridSavedState]);

  const saveState = (state: any) => {
    let options = clearState(state);
    // options.version = vars.version + 1;
    // vars.version = options.version;

    var stateString = JSON.stringify(options);
    consoleLog("changed to:" + stateString);
    if (stateString != vars.currentState) {
      vars.currentState = stateString;
      consoleLog("save:" + stateString);
      setGridSavedState(stateString);
    }
  };

  const saveStateCallback = useCallback((state) => {
    if (props.stateSavingEnabled != false) {
      saveState(state);
    }
  }, []);

  const onContentReadyCallback = useCallback(
    // в TreeList плавающий баг с отрисовкой и фокусом, если передернуть по всякому вроде работает более менее стабильно.
    (e: any) => {
      if (needNavigate != "" && gridRef.current) {
        let state = gridRef.current!.getInstance()!.state();
        if (needNavigate == state.focusedRowKey) {
          setNeedNavigate("");
          setTimeout(async () => {
            // notify(state.focusedRowKey, "error", 1000);
            await e.component.navigateToRow(state.focusedRowKey);
            // await e.component.navigateToRow(state.focusedRowKey);
            gridRef.current!.getInstance()?.refresh();
            await e.component.navigateToRow(state.focusedRowKey);
            // await e.component.navigateToRow(state.focusedRowKey);
          }, 300);
        }
      }
    },
    [needNavigate]
  );

  return (
    <TreeCore
      ref={gridRef}
      collectionName={props.collectionName}
      dataSourceOptions={props.dataSourceOptions}
      title={props.title}
      columns={props.columns}
      toolbarItemsRightBefore={props.toolbarItemsRightBefore}
      toolbarItemsRightAfter={props.toolbarItemsRightAfter}
      onEditorPreparing={props.onEditorPreparing}
      onFocusedRowChanged={props.onFocusedRowChanged}
      onRowPrepared={props.onRowPrepared}
      saveState={saveStateCallback}
      toolbarVisible={props.toolbarVisible}
      filterPanelVisible={props.filterPanelVisible}
      onContentReady={onContentReadyCallback}
      // initialized={initialized}
    >
      {props.children}
    </TreeCore>
  );
});

export default component; // React.memo(component);

export function clearState(state: any): any | null {
  let columns: any[] = [];
 
  if (state.columns) {
    for (let col of state.columns) {
      let colOpt: any = {
        name: col.name,
        dataField: col.dataField,
      };

      let colHasOptions = false;
      if (col.sortIndex !== undefined) {
        colHasOptions = true;
        colOpt.sortIndex = col.sortIndex;
        colOpt.sortOrder = col.sortOrder;
      }

      if (col.groupIndex !== undefined) {
        colHasOptions = true;
        colOpt.groupIndex = col.groupIndex;
      }

      if (colHasOptions) {
        columns.push(colOpt);
      }
    }
  }
  // columns = state.columns;
  let hasOptions = false;
  let options: any = {};
  if (columns.length > 0) {
    options.columns = columns;
    hasOptions = true;
  }

  if (state.filterValue) {
    options.filterValue = state.filterValue;
    hasOptions = true;
  }

  // приводит при смене фокуса строки (и соотв url ) tree обновляется, не смог разобраться почему, в гриде такого нет

  if (state.focusedRowKey){
    options.focusedRowKey = state.focusedRowKey;
    hasOptions = true;
  }

  // if (state.expandedRowKeys){
  //   options.expandedRowKeys = state.expandedRowKeys;
  //   hasOptions = true;
  // }


  if (!hasOptions) {
    return null;
  }
  return options;
  // return {focusedRowKey:options.focusedRowKey};
}

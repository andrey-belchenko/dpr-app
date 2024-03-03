// TODO Возможно требуется какая-то оптимизация , чтобы исключить лишние вызовы

export const processColumns = (columns?: any[]) => {
  if (columns) {
    if (columns[0]?.processed) {
      return columns;
    }
    if (columns[0]) {
      columns[0].processed = true;
    }
    customProcessColumns(columns, (column) => {
      if (!column.headerFilter?.search) {
        if (!column.headerFilter) {
          column.headerFilter = {};
        }
        column.headerFilter.search = { enabled: true };
      }
    });
  }
  return columns;
};

export const customProcessColumns = (
  columns: any[] | undefined,
  callback: (col: any, isRoot: boolean, childrenResults?: any[]) => any,
  withGroups: boolean = false,
  isRoot: boolean = true
) => {
  let results: any[] = [];
  if (columns) {
    for (let col of columns) {
      var result = customProcessColumn(col, callback, withGroups, isRoot);
      if (result) {
        results.push(result);
      }
    }
  }
  return results;
};

const customProcessColumn = (
  column: any,
  callback: (col: any, isRoot: boolean, childrenResults?: any[]) => any,
  withGroups: boolean,
  isRoot: boolean
): any => {
  let childrenResults: any[] | undefined = undefined;
  if (column.columns) {
    childrenResults = customProcessColumns(
      column.columns,
      callback,
      withGroups,
      false
    );
    
    if (!withGroups) {
      return;
    }
  }
  return callback(column, isRoot, childrenResults);
};

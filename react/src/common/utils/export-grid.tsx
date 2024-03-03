import React from "react";
import DataGrid from "devextreme-react/data-grid";

import { Workbook } from "exceljs";
import { saveAs } from "file-saver-es";
import { exportDataGrid } from "devextreme/excel_exporter";


export const exportGridToExcel = (ref:React.RefObject<DataGrid<any,any>>, fileName:string) => {
    const workbook = new Workbook();
    const worksheet = workbook.addWorksheet("Сопоставление ключей");
    exportDataGrid({
      component: ref.current?.instance,
      worksheet,
      autoFilterEnabled: true,
    }).then(() => {
      workbook.xlsx.writeBuffer().then((buffer) => {
        saveAs(
          new Blob([buffer], { type: "application/octet-stream" }),
          `${fileName}.xlsx`
        );
      });
    });
  };

 
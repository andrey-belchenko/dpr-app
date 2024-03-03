import React, { useEffect, useState } from "react";
import { TabPanel, Item } from "devextreme-react/tab-panel";
// import Form from 'devextreme-react/form';
import ReactJson from "react-json-view";
import DataGrid, { Column } from "devextreme-react/data-grid";
import { createArrayStore, getObject } from "src/mrskc/data/apiClient";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

export default function Component({ data }: Props) {
  const [obj, setObj] = useState<any>({entities:[{}],msg:{payload:""}});

  useEffect(() => {
    const fetchData = async () => {
      const response = await getObject("view_blockedDtoDetails", {
        _id: data.data.blockedMessageId,
      });
      setObj(response);
    };
    fetchData();
  }, []);

  if (obj) {
    return (
      // <div>111</div>
      <TabPanel>
        <Item title="Заблокированные объекты">
          <DataGrid
            dataSource={obj.entities}
            showBorders={false}
            // focusedRowEnabled={true}
            defaultFocusedRowIndex={0}
            columnAutoWidth={true}
            columnHidingEnabled={false}
            showColumnLines={true}
            hoverStateEnabled={true}
          >
            <Column dataField="id" caption="Код объекта" />
            <Column dataField="type" caption="Тип" />
            <Column dataField="name" caption="Наименование" />
            <Column dataField="status" caption="Статус" />
          </DataGrid>
        </Item>
        <Item title="Сообщение">
          <ReactJson
            src={obj.message}
            displayDataTypes={false}
            name={false}
          />
        </Item>
      </TabPanel>
    );
  } else {
    return <div />;
  }
}

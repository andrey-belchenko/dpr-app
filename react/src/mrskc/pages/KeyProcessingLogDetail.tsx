import React, { useEffect, useState } from "react";
import { TabPanel, Item } from "devextreme-react/tab-panel";
import DataGrid, { Column, MasterDetail } from "devextreme-react/data-grid";
import { getArray } from "src/common/data/mongo-api";
import KeyProcessingLogMessageDetail from "./KeyProcessingLogMessageDetail";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

export default function Component({ data }: Props) {
  const [entities, setEntities] = useState([]);
  const [messages, setMessages] = useState([]);
  useEffect(() => {
    const fetchData = async () => {
      const ent = await getArray("sys_model_ExtraIdMatching", {
        applyOperationId: data.data.id,
      });
      setEntities(ent.data);
      const msg = await getArray("view_blockedDto", {
        applyOperationId: data.data.id,
      });
      setMessages(msg.data);
    };
    fetchData();
  }, []);

  return (
    // <div>111</div>
    <TabPanel>
      <Item title="Объекты">
        <DataGrid
          dataSource={entities}
          showBorders={false}
          // focusedRowEnabled={true}
          defaultFocusedRowIndex={0}
          columnAutoWidth={true}
          columnHidingEnabled={false}
          showColumnLines={true}
          hoverStateEnabled={true}
        >
          <Column dataField="type" caption="Тип" dataType="string" />
          <Column dataField="name" caption="Наименование" dataType="string" />
          <Column dataField="id" caption="Код СКК" dataType="string" />
          <Column
            dataField="allowCreate"
            caption="Разрешено создание"
            dataType="boolean"
          ></Column>
          <Column
            dataField="platformId"
            caption="Идентификатор"
            dataType="string"
          />
        </DataGrid>
      </Item>
      <Item title="Сообщения">
        <DataGrid
          dataSource={messages}
          showBorders={false}
          // focusedRowEnabled={true}
          defaultFocusedRowIndex={0}
          columnAutoWidth={true}
          columnHidingEnabled={false}
          showColumnLines={true}
          hoverStateEnabled={true}
        >
          <MasterDetail enabled={true} component={KeyProcessingLogMessageDetail} />
          <Column
            dataField="eventId"
            caption="Код события"
            dataType="string"
          ></Column>
          <Column
            dataField="objectId"
            caption="Код объекта"
            dataType="string"
          ></Column>
          <Column
            dataField="messageChangedAt"
            caption="Дата"
            dataType="datetime"
          ></Column>
          <Column
            dataField="messageExtId"
            caption="Ид сообщения"
            dataType="string"
          ></Column>
        </DataGrid>
      </Item>
    </TabPanel>
  );
}

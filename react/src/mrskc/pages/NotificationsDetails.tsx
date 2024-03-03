import React from "react";
import TabPanel, { Item } from "devextreme-react/tab-panel";
import TextBlock from "src/mrskc/components/TextBlock";
import ReactJson from "react-json-view";
import DataGrid, { Column } from "devextreme-react/data-grid";

// import { CopyBlock, a11yLight } from "react-code-blocks";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

const MasterDetailView: React.FC<Props> = ({ data }) => {
  return (
    <TabPanel>
      {/* <Item title="Текст предупреждения">
        <TextBlock code={data.data.message} />
      </Item>
      <Item title="Данные" >
        <ReactJson src={data.data.data} displayDataTypes={false} name={false}/>
      </Item> */}
      <Item title="Получатели">
        <DataGrid
          dataSource={data.data.recipients.map((a: any) => {
            return { Адрес: a };
          })}
          showBorders={false}
          // focusedRowEnabled={true}
          defaultFocusedRowIndex={0}
          columnAutoWidth={true}
          columnHidingEnabled={false}
          showColumnLines={true}
          hoverStateEnabled={true}
        >
          {/* <Column
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
          ></Column> */}
        </DataGrid>
      </Item>
    </TabPanel>
  );
};

export default MasterDetailView;

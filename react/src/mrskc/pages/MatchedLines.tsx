import Button from "devextreme-react/button";
import React from "react";
import { useCallback, useState } from "react";
import ButtonCell from "src/mrskc/components/ButtonCell";
import Grid from "src/common/components/grid/Grid";
import PopupWindow from "src/mrskc/components/PopupWindow";
import { downloadFile } from "src/common/data/apiClient";
import { getHostUrl } from "src/common/utils/host-url";

const openSchema = (pageName: string, code: string) => {
  let url = `${getHostUrl()}/#/${pageName}/${code}`;
  window.open(url, "_blank");
};

export default function Component() {
  const [currentItem, setCurrentItem] = useState<any>({});
  const [listVisible, setListVisible] = useState(false);
  const showList = useCallback((item) => {
    setCurrentItem(item);
    setTimeout(() => {
      setListVisible(true);
    }, 300);
  }, []);

  const grid = React.useMemo(() => {
    const columns = [
      {
        dataField: "region",
        caption: "РЭС",
        dataType: "string",
      },
      {
        dataField: "rootName",
        caption: "Наименование ПС",
        dataType: "string",
      },
      {
        dataField: "code",
        caption: "Код линии",
        dataType: "string",
      },
      {
        dataField: "name",
        caption: "Наименование линии",
        dataType: "string",
      },
      {
        caption: "Схемы",
        // fixed: true,
        // fixedPosition: "left",
        width: 105,
        cellRender(item: any) {
          return (
            <ButtonCell>
              <Button
                icon="smalliconslayout"
                hint="Схема пролетов"
                onClick={async () => {
                  openSchema("LineSpanSchema", item.data.code);
                }}
              ></Button>
              <Button
                icon="contentlayout"
                hint="Схема сегментов"
                onClick={async () => {
                  openSchema("LineSegmentSchema", item.data.code);
                }}
              ></Button>
              <Button
                icon="bulletlist"
                hint="Детальная схема с узлами и терминалами"
                onClick={async () => {
                  openSchema("LineEquipmentSchema", item.data.code);
                }}
              ></Button>
            </ButtonCell>
          );
        },
      },
      {
        dataField: "withMatching",
        caption: "Выполнено сопоставление",
        dataType: "string",
        alignment: "center",
      },
      {
        dataField: "matchedCount",
        caption: "Сопоставлено сегментов",
        dataType: "number",
        alignment: "center",
      },
      {
        
      
          caption: "Не сопоставлено сегментов",
          columns:[
            {
              dataField: "notMatchedCount",
              caption: "Всего",
              dataType: "number",
              alignment: "center",
              cellRender(item: any) {
                let text = "";
                if (item.data.notMatchedCount > 0) {
                  let data = item.data;
                  text = `${data.notMatchedCount}`;
                  return (
                    <ButtonCell text={text} textAlign="center">
                      <Button
                        icon="more"
                        onClick={() => {
                          showList(data);
                        }}
                      ></Button>
                    </ButtonCell>
                  );
                } else {
                  return <div />;
                }
              },
            },
            {
              dataField: "needMatchCount",
              caption: "Без потр.",
              dataType: "number",
              alignment: "center",
              
            },
          ]
      },
      

      {
        dataField: "id",
        caption: "Идентификатор",
        dataType: "string",
      },
    ];

    return (
      <Grid
        collectionName="view_topology_MatchedLineInfo"
        title="ЛЭП"
        countByColumn="region"
        columns={columns}
      ></Grid>
    );
  }, []);

  return (
    <React.Fragment>
      {grid}
      <PopupWindow
        width={1000}
        height={600}
        title={"Не сопоставленные сегменты " + currentItem?.name}
        visible={listVisible}
        onHiding={() => setListVisible(false)}
      >
        <Grid
          title={"Не сопоставленные сегменты "}
          countByColumn="name"
          columns={[
            {
              dataField: "name",
              caption: "Наименование",
              dataType: "string",
            },
            {
              dataField: "id",
              caption: "Идентификатор",
              dataType: "string",
            },
            {
              dataField: "isCustomerEquipment",
              caption: "Потребительский",
              dataType: "boolean",
              showEditorAlways: false,
              trueText: "Да",
              falseTest: "Нет",
            },
          ]}
          stateSavingEnabled={false}
          filterPanel={{ visible: false }}
          groupPanel={{ visible: false }}
          dataSource={currentItem?.notMatched}
          remoteOperations={false}
          refreshButtonVisible={false}
        ></Grid>
      </PopupWindow>
    </React.Fragment>
  );
}

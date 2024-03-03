// TODO: частично скопировано из RunLineMatching вынести общую часть при дальнейшем развитии

import Toolbar, { Item } from "devextreme-react/toolbar";
import Box, { Item as BoxItem } from "devextreme-react/box";

import { TabPanel, Item as TabItem } from "devextreme-react/tab-panel";
import TextBox from "devextreme-react/text-box";
import React, { useEffect, useState } from "react";
import { MasterDetail } from "devextreme-react/data-grid";
import {
  getArray,
  getObject,
  getScriptMetadata,
  getScriptParams,
} from "src/mrskc/data/apiClient";
import { useCookies } from "react-cookie";
import Grid from "src/common/components/grid/Grid";
import LineMatchingResultsDetail from "./LineMatchingResultsDetail";

const scriptExecutionColName = "sys_scriptExecution";
const scriptName = "lineMatching";

const formatDate = (date: any) => {
  if (!date) return undefined;
  var locale = navigator.language;
  let localDate = new Date(date);
  return localDate.toLocaleString(locale, {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
};

export default function Component() {
  const [database, setDatabase] = React.useState("");
  const [databaseNames, setDatabaseNames] = React.useState<any>(null);
  const [isValidDatabase, setIsValidDatabase] = React.useState(false);
  const [dbNameIssue, setDbNameIssue] = useState<string>("");
  const [cookies, setCookie] = useCookies(["prepDatabase"]);
  const [isRunning, setIsRunning] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [isFinished, setIsFinished] = useState(false);
  const [isError, setIsError] = useState(false);
  const [statusText, setStatusText] = useState("");
  const [hasStatus, setHasStatus] = useState(false);
  const [startedAt, setStartedAt] = useState<string | undefined>(undefined);
  const [finishedAt, setFinishedAt] = useState<string | undefined>(undefined);
  const [executionId, setExecutionId] = useState("");
  const [scriptMetadata, setScriptMetadata] = useState<any>(null);

  useEffect(() => {
    init();
  }, []);

  const init = async (currentDb?: string) => {
    let scriptParamsRequest = getScriptParams(
      "topologyMatching",
      "scripts/topologyMatching/main.py"
    );
    let dbList = (await getArray("databases")).data;
    let dbsObj: any = {};
    for (let db of dbList) {
      dbsObj[db.name] = true;
    }
    setDatabaseNames(dbsObj);

    let scriptParams = await scriptParamsRequest;

    if (!currentDb) {
      currentDb = scriptParams.database;
      if (cookies.prepDatabase) {
        currentDb = cookies.prepDatabase;
      }
    }

    setDatabase(currentDb!);
    let storedParams = await getObject(
      scriptExecutionColName,
      {
        database: currentDb,
        scriptName: scriptName,
      }, // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      currentDb
    );
    let execId = storedParams?.executionId || "";
    setExecutionId(execId);
    await refreshExecInfo(execId, currentDb);

    let scriptMetadata = await getScriptMetadata(
      "topologyMatching",
      "scripts/topologyMatching/main.py"
    );
    setScriptMetadata(scriptMetadata);
    setIsInitialized(true);
  };

  const refreshExecInfo = async (
    execId: string | null = null,
    currentDb: string | null = null
  ) => {
    if (execId === null) {
      execId = executionId;
    }

    if (!currentDb) {
      currentDb = database;
    }

    let operationInfo = await getObject(
      "sys_asyncWebOperations",
      {
        id: execId,
      },
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      currentDb
    );
    setIsRunning(operationInfo?.status == "started");
    setIsFinished(["completed", "error"].includes(operationInfo?.status));
    setIsError(operationInfo?.status == "error");
    setStartedAt(formatDate(operationInfo?.startedAt));
    setFinishedAt(formatDate(operationInfo?.finishedAt));
    setHasStatus(operationInfo?.status != null);
    let map: any = {
      started: "Выполнение",
      error: "Ошибка",
      completed: "Завершен",
    };
    setStatusText(map[operationInfo?.status] || "");
  };

  useEffect(() => {
    const action = async () => {
      debugger;
      await refreshExecInfo(executionId);
    };
    if (isRunning) {
      const interval = setInterval(action, 2000);
      return () => clearInterval(interval);
    }
  }, [executionId, isRunning, database]);

  useEffect(() => {
    if (!databaseNames) {
      setDbNameIssue("");
      setIsValidDatabase(false);
    } else if (!databaseNames[database]) {
      setDbNameIssue("Указана несуществующая БД " + database);
      setIsValidDatabase(false);
    } else if (database.startsWith("exchange") || database == "admin") {
      setDbNameIssue("Указана недопустимая БД " + database);
      setIsValidDatabase(false);
    } else {
      setDbNameIssue("");
      setIsValidDatabase(true);
    }
    setCookie("prepDatabase", database);
  }, [database, databaseNames]);

  let formContent: any = [];

  if (!isInitialized) {
    formContent = <div className="df-box-item-centred-content"></div>;
  } else {
    let message = "";
    if (!isValidDatabase) {
      message = dbNameIssue + " ";
    } else if (!isFinished) {
      message =
        "Содержимое будет отображено после выполнения процесса сопоставления";
    } else if (isError) {
      message =
        "Процесс сопоставления не был завершен из-за необработанной ошибки";
    }
    if (message) {
      formContent = (
        <div className="df-box-item-centred-content">{message}</div>
      );
    } else {
      let tabs = [];
      if (scriptMetadata?.output) {
        for (let item of scriptMetadata?.output) {
          const columns = item.columns;

          let gridId = "scriptResult-grid-" + item.collectionName;
          tabs.push(
            <TabItem
              key={"scriptResult-tab-" + item.collectionName!}
              title={item.title}
            >
              <Grid
                key={gridId}
                gridId={gridId}
                collectionName={item.collectionName}
                title={item.title}
                countByColumn="_id"
                columns={columns}
                dataSourceOptions={{ database: database }}
              >
                <MasterDetail
                  enabled={true}
                  component={LineMatchingResultsDetail}
                />
              </Grid>
            </TabItem>
          );
        }
      }

      formContent = (
        <TabPanel
          tabsPosition="top"
          showNavButtons={true}
          className="df-form-tab-panel"
        >
          {tabs}
        </TabPanel>
      );
    }
  }

  const handleDbEditorChange = (e: any) => {
    setDatabase(e.value);
    setCookie("prepDatabase", database);
    init(e.value);
  };

  return (
    <Box direction="col" className="df-form-box">
      {/* TODO: хорошо бы избавиться от явного определения размера 39 */}
      <BoxItem ratio={0} baseSize={39}>
        <Toolbar className="df-toolbar df-toolbar-no-border">
          <Item location="before">
            <span className="df-toolbar-field-label">Рабочая БД</span>
          </Item>
          <Item location="before">
            <TextBox
              stylingMode="underlined"
              width={200}
              value={database}
              onValueChanged={handleDbEditorChange}
            />
          </Item>
          <Item location="before" visible={hasStatus}>
            <span className="df-toolbar-field-label">Статус</span>
          </Item>
          <Item location="before" visible={hasStatus}>
            <TextBox
              stylingMode="underlined"
              width={100}
              value={statusText}
              readOnly={true}
            />
          </Item>
          <Item location="before" visible={hasStatus}>
            <span className="df-toolbar-field-label">Запущен в</span>
          </Item>
          <Item location="before" visible={hasStatus}>
            <TextBox
              stylingMode="underlined"
              width={140}
              value={startedAt}
              readOnly={true}
            />
          </Item>
          <Item location="before" visible={isFinished}>
            <span className="df-toolbar-field-label">Завершен в</span>
          </Item>
          <Item location="before" visible={isFinished}>
            <TextBox
              stylingMode="underlined"
              width={140}
              value={finishedAt}
              readOnly={true}
            />
          </Item>
        </Toolbar>
      </BoxItem>
      <BoxItem ratio={1}>{formContent}</BoxItem>
    </Box>
  );
}

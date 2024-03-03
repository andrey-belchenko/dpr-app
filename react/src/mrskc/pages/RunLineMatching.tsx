import Toolbar, { Item } from "devextreme-react/toolbar";
import Box, { Item as BoxItem } from "devextreme-react/box";

import Form from "devextreme-react/form";
import Button from "devextreme-react/button";
import ScrollView from "devextreme-react/scroll-view";
import { TabPanel, Item as TabItem } from "devextreme-react/tab-panel";
import ReactJson from "react-json-view";
import JsonEditor from "src/mrskc/components/JsonEditor";
import TextBox from "devextreme-react/text-box";
import React, { useEffect, useState } from "react";
import {
  getArray,
  getObject,
  getScriptParams,
  sendModifyRequest,
  sendModifyRequestGetJson,
} from "src/common/data/mongo-api";
import { useCookies } from "react-cookie";
import notify from "devextreme/ui/notify";

const scriptExecutionColName = "sys_scriptExecution";
const mergeColumns = ["database", "scriptName"];
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
  const [paramsObj, setParamsObj] = React.useState<any>({});
  const [paramsText, setParamsText] = React.useState("");
  const [isTextChanged, setIsTextChanged] = React.useState(false);
  const [database, setDatabase] = React.useState("");
  const [databaseNames, setDatabaseNames] = React.useState<any>(null);
  const [isValidDatabase, setIsValidDatabase] = React.useState(false);
  const [dbNameIssue, setDbNameIssue] = useState<string>("");
  const [cookies, setCookie] = useCookies(["prepDatabase"]);
  const [isBlocked, setIsBlocked] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [isFinished, setIsFinished] = useState(false);
  const [isError, setIsError] = useState(false);
  const [statusText, setStatusText] = useState("");
  const [hasStatus, setHasStatus] = useState(false);
  const [startedAt, setStartedAt] = useState<string | undefined>(undefined);
  const [finishedAt, setFinishedAt] = useState<string | undefined>(undefined);
  const [executionId, setExecutionId] = useState("");

  useEffect(() => {
    init();
  }, []);

  const init = async (currentDb?: string) => {
    setIsBlocked(true);
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
      },
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      currentDb
    );

    let params: any = {};
    if (storedParams?.params == null) {
      params = { ...scriptParams };
    } else {
      for (let name in scriptParams) {
        params[name] = storedParams.params[name];
      }
    }
    delete params.database;

    updateParamsText(params);
    setParamsObj(params);
    let execId = storedParams?.executionId || "";
    setExecutionId(execId);
    await refreshExecInfo(execId, currentDb);
    setIsBlocked(false);
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
      }, // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      currentDb
    );
    setIsRunning(operationInfo?.status == "started");
    setIsBlocked(operationInfo?.status == "started");
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
    if (!databaseNames || !database) {
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

  const updateParamsText = (currentParams?: any) => {
    if (!currentParams) {
      currentParams = paramsObj;
    }
    setParamsText(JSON.stringify(currentParams, null, 2));
    return paramsObj;
  };

  const updateParamsObj = () => {
    let newPars = { ...paramsObj };
    try {
      const obj = JSON.parse(paramsText);
      for (let name in newPars) {
        newPars[name] = obj[name];
      }
    } catch (error) {
      console.log("Invalid JSON:", error);
    }
    setParamsObj(newPars);
    setIsTextChanged(false);
    return newPars;
  };

  const handleFormChange = (e: any) => {
    setParamsObj({
      ...paramsObj,
      [e.dataField]: e.value,
    });
  };

  const handleEditorChange = (value: any) => {
    setParamsText(value);
    setIsTextChanged(true);
  };

  const syncParams = () => {
    if (isTextChanged) {
      return updateParamsObj();
    } else {
      return updateParamsText();
    }
  };

  const handleTabsSelectedItemChange = (value: any) => {
    syncParams();
  };

  const saveParamsToDb = async (doNotification: boolean) => {
    let currentPars = syncParams();
    let data = {
      database: database,
      scriptName: scriptName,
      params: { ...currentPars },
    };
    await sendModifyRequest({
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      database: database,
      commands: [
        {
          command: "merge",
          into: scriptExecutionColName,
          whenMatched: "merge",
          on: mergeColumns,
          data: data,
        },
      ],
    });
    if (doNotification) {
      notify("Параметры для БД " + database + " сохранены", "success");
    }
  };

  const handleSaveParamsToDb = async () => {
    saveParamsToDb(true);
  };

  const resetParams = async () => {
    syncParams();
    let data = { database: database, scriptName: scriptName, params: null };
    await sendModifyRequest({
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      database: database,
      commands: [
        {
          command: "merge",
          into: scriptExecutionColName,
          whenMatched: "merge",
          on: mergeColumns,
          data: data,
        },
      ],
    });
    init();
    notify(
      "Восстановлены параметры по умолчанию для БД " + database,
      "success"
    );
  };

  const runScript = async () => {
    setIsBlocked(true);
    let currentPars = syncParams();
    await saveParamsToDb(false);
    let result = await sendModifyRequestGetJson({
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      database: database,
      isAsync: true,
      commands: [
        {
          command: "runScript",
          projectName: "topologyMatching",
          scriptName: "scripts/topologyMatching/main.py",
          params: { database: database, ...currentPars },
        },
      ],
    });
    let execId = result.asyncExecutionId;

    await sendModifyRequest({
      // изначально планировал, что параметры и информация о запуске будут храниться в основной БД, но потом решили хранить БД где выполняется сопоставление
      database: database,
      commands: [
        {
          command: "merge",
          into: scriptExecutionColName,
          whenMatched: "merge",
          on: mergeColumns,
          data: {
            database: database,
            scriptName: scriptName,
            executionId: execId,
          },
        },
      ],
    });
    refreshExecInfo(execId, database);
    setIsRunning(true);
    setExecutionId(execId);

    notify("Процесс запущен", "success");
  };

  let formContent: any = [];
  if (!isValidDatabase) {
    formContent = (
      <div className="df-box-item-centred-content">{dbNameIssue}</div>
    );
  } else {
    formContent = (
      <TabPanel onSelectedItemChange={handleTabsSelectedItemChange}>
        <TabItem title="Параметры (форма)">
          <ScrollView useNative={true}>
            <Form
              id="form"
              labelMode="outside"
              formData={paramsObj}
              readOnly={isBlocked}
              labelLocation="left"
              colCount={1}
              onFieldDataChanged={handleFormChange}
            />
          </ScrollView>
        </TabItem>
        <TabItem title="Параметры (JSON)">
          <JsonEditor
            value={paramsText}
            onChange={handleEditorChange}
            readOnly={isBlocked}
          />
        </TabItem>
      </TabPanel>
    );
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

          <Item location="after">
            <Button
              icon="revert"
              // text="Сбросить"
              hint="Сбросить параметры"
              onClick={resetParams}
              disabled={isBlocked}
            />
          </Item>
          <Item location="after">
            <Button
              icon="check"
              stylingMode="contained"
              hint="Запомнить параметры"
              // text="Запомнить"
              onClick={handleSaveParamsToDb}
              disabled={isBlocked}
            />
          </Item>
          <Item location="after">
            <Button
              icon="video"
              text="Запуск"
              onClick={runScript}
              disabled={isBlocked}
            />
          </Item>
        </Toolbar>
      </BoxItem>
      <BoxItem ratio={1}>{formContent}</BoxItem>
    </Box>
  );
}

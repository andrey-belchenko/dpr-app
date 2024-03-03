import ArrayStore from "devextreme/data/array_store";
import CustomStore from "devextreme/data/custom_store";
import notify from "devextreme/ui/notify";
import { Interface } from "readline";
import { Cookies } from "react-cookie";
// import env from "react-dotenv";

export interface DataSourceOptions {
  idField?: string;
  updateTrigger?: string;
  database?: string;
}
const cookies = new Cookies();
export function getDatabase() {
  return cookies.get("database") || "exchange"; // временно
}

function handleErrors(response: any) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export function getQueryApiUrl() {
  return process.env.REACT_APP_adp_qry_api_url;

  // return env.REACT_APP_adp_api_url;
}

function getProcessorApiUrl() {
  return process.env.REACT_APP_adp_proc_api_url;

  // return env.REACT_APP_adp_api_url;
}

export const createArrayStore = async (
  collectionName: string,
  idField: string = "_id",
  filter: any = null
) => {
  let data = await getArray(collectionName, filter);
  let store = new ArrayStore({
    key: idField,
    data: data.data,
  });
  return store;
};

export const getObject = async (
  collectionName: string,
  filter: any = null,
  database?: string
) => {
  let data = await getArray(collectionName, filter, null, database);
  if (data.data.length > 0) {
    return data.data[0];
  }
  return null;
};

export const getArray = async (
  collectionName: string,
  filter: any = null,
  pipeline: any = null,
  database?: string
) => {
  let url = `${getQueryApiUrl()}/query`;
  if (!database) {
    database = getDatabase();
  }
  let body: any = {
    database: database,
    collection: collectionName,
  };
  if (filter) {
    body["filter"] = filter;
  }
  if (pipeline) {
    body["pipeline"] = pipeline;
  }
  const query = fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
  let response = await query;

  let data: any = await response.json();
  return data;
};

export const getScriptParams = async (
  projectName: string,
  scriptName: String,
  showErrorNotification: boolean = true
) => {
  return await getScriptVar(
    projectName,
    scriptName,
    "getScriptParams",
    showErrorNotification
  );
};

export const getScriptMetadata = async (
  projectName: string,
  scriptName: String,
  showErrorNotification: boolean = true
) => {
  return await getScriptVar(
    projectName,
    scriptName,
    "getScriptMetadata",
    showErrorNotification
  );
};

export const getScriptVar = async (
  projectName: string,
  scriptName: String,
  methodName: String,
  showErrorNotification: boolean = true
) => {
  let url = `${getProcessorApiUrl()}/${methodName}`;
  const query = fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      projectName: projectName,
      scriptName: scriptName,
    }),
  });
  let response = await query.then(handleErrors).catch(() => {
    if (showErrorNotification) {
      notify("Network error", "error", 1000 * 60);
    }
    throw "Network error";
  });
  var body = await response.json();
  return body;
};

export const sendModifyRequest = (
  options: any,
  showErrorNotification: boolean = true
) => {
  if (!options["database"]) {
    options["database"] = getDatabase();
  }

  let url = `${getProcessorApiUrl()}/exec`;
  const query = fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(options),
  });
  return query.then(handleErrors).catch(() => {
    if (showErrorNotification) {
      notify("Network error", "error", 1000 * 60);
    }
    throw "Network error";
  });
};

export const sendModifyRequestGetJson = async (options: any) => {
  var response = await sendModifyRequest(options);
  var body = await response.json();
  return body;
};

export const createDataSource = (
  collectionName: string,
  options?: DataSourceOptions
) => {
  if (!options) {
    options = {};
  }
  let idField = options.idField;
  if (!idField) {
    idField = "_id";
  }
  let updateTriggerName = options.updateTrigger;
  let database = options.database;
  if (!database) {
    database = getDatabase();
  }

  const customDataSource = new CustomStore({
    key: idField,
    load: (loadOptions: any) => {
      let url = `${getQueryApiUrl()}/query`;
      const query = fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          database: database,
          collection: collectionName,
          loadOptions: loadOptions,
        }),
      });

      return query
        .then(handleErrors)
        .then((response) => response.json())
        .catch(() => {
          throw "Network error";
        });
    },
    update: (key, values) => {
      let obj: any = {};
      let keyExpr = key;
      if (idField == "_id") {
        keyExpr = { $oid: key };
      }
      obj[idField!] = keyExpr;
      for (let name in values) {
        obj[name] = values[name];
      }
      let idFilter: any = {};
      idFilter[idField!] = keyExpr;
      let triggers: any[] = [];
      if (updateTriggerName) {
        triggers = [
          {
            command: "trigger",
            trigger: updateTriggerName,
            filter: idFilter,
          },
        ];
      }
      return sendModifyRequest(
        {
          commands: [
            {
              command: "merge",
              into: collectionName,
              on: idField,
              whenMatched: "merge",
              data: [obj],
            },
            ...triggers,
            {
              collection: collectionName,
              command: "find",
              filter: idFilter,
            },
          ],
        },
        false
      );
    },
  });

  return customDataSource;
};




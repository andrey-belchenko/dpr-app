export const dataSourceOptions = {
  updateTrigger: "trigger_IdMatching",
};

export const onEditorPreparing = (e: any) => {
  if (e.row && e.row.data) {
    if (e.row.data.status == "Обработан") {
      e.editorOptions.disabled = true;
    } else if (e.row.data.allowCreate && e.dataField == "platformId") {
      e.editorOptions.disabled = true;
    } else if (e.row.data.platformId && e.dataField == "allowCreate") {
      e.editorOptions.disabled = true;
    }
  }
};

export const isNewMatchAllowed = (data: any) => {
  if (!data) return false;

  if (data.status == "Обработан") {
    return false;
  }

  if (data.allowCreate) {
    return false;
  }

  return true;
};

export const isNeedMatch = (data: any) => {
  if (!data) return false;

  if (data.status == "Обработан" || data.status == "Готов") {
    return false;
  }
  return true;
};

export const getUpdateOptions = (updateData: any, ids: any) => {
  return {
    commands: [
      {
        command: "merge",
        into: "sys_model_ExtraIdMatching",
        whenMatched: "merge",
        on: ["fullId"],
        data: updateData,
      },
      {
        command: "trigger",
        trigger: "trigger_IdMatching",
        filter: { fullId: { $in: ids } },
      },
    ],
  };
};

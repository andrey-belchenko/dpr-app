import React, { useEffect, useState } from "react";
import { TabPanel, Item } from "devextreme-react/tab-panel";
// import Form from 'devextreme-react/form';
import ReactJson from "react-json-view";
import DataGrid, { Column } from "devextreme-react/data-grid";
import { createArrayStore, getObject } from "src/common/data/apiClient";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

export default function Component({ data }: Props) {
  const [obj, setObj] = useState<any>(null);

  useEffect(() => {
    const fetchData = async () => {
      const response = await getObject("sys_model_BlockedMessages", {
        id: data.data.blockedMessageId,
      });
      setObj(response);
    };
    fetchData();
  }, []);

  if (obj) {
    return (
      <ReactJson src={obj.msg.payload} displayDataTypes={false} name={false} />
    );
  } else {
    return <div />;
  }
}

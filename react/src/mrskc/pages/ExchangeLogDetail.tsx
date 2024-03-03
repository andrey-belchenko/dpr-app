import React, { useEffect, useState } from "react";
import ReactJson from "react-json-view";
import XmlBlock from "src/mrskc/components/XmlBlock";
import { getObject } from "src/common/data/apiClient";
// import XMLViewer from 'react-xml-viewer'
import xmlFormat from 'xml-formatter';

interface Props {
  data: {
    key: string;
    data: any;
  };
}

export default function Component({ data }: Props) {
  const [obj, setObj] = useState<any>({});

  useEffect(() => {
    const fetchData = async () => {
      const response = await getObject("sys_MessageLog", {
        _id: { $oid: data.data._id },
      });
      setObj(response);
    };
    fetchData();
  }, []);

  if (obj && obj.message) {
    let isXml = false;

    if (obj.service == "sk11-incoming") {
      isXml = true;
    }
    if (obj.service == "platform-outgoing" || obj.service == "sk11-outgoing") {
      if (obj.messageType.startsWith("out")) {
        isXml = true;
      }
    }
    if (isXml) {
      return  <XmlBlock code={xmlFormat(obj.message)}/>
      
    } else {
      let value = JSON.parse(obj.message);
      return <ReactJson src={value} displayDataTypes={false} name={false} />;
    }
  } else {
    return <div />;
  }
}

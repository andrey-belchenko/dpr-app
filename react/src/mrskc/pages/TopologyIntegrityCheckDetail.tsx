import React from "react";
import TabPanel, { Item } from "devextreme-react/tab-panel";
import TextBlock from "src/mrskc/components/TextBlock";
import ReactJson from "react-json-view";

// import { CopyBlock, a11yLight } from "react-code-blocks";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

const MasterDetailView: React.FC<Props> = ({ data }) => {
  return (
    <ReactJson src={data.data.item} displayDataTypes={false} name={false}/>
  );
};

export default MasterDetailView;

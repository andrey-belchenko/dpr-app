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
    <TabPanel>
      <Item title="Текст предупреждения">
        <TextBlock code={data.data.message} />
      </Item>
      <Item title="Данные" >
        <ReactJson src={data.data.data} displayDataTypes={false} name={false}/>
      </Item>
    </TabPanel>
  );
};

export default MasterDetailView;

import React from "react";
import TabPanel, { Item } from "devextreme-react/tab-panel";
import TextBlock from "src/components/TextBlock";
import JsBlock from "src/components/JsBlock";

// import { CopyBlock, a11yLight } from "react-code-blocks";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

const MasterDetailView: React.FC<Props> = ({ data }) => {
  if (!data.data.error) {
    return (<JsBlock code={data.data.pipeline} />);
  } else {
    return (
      <TabPanel>
        <Item title="Ошибка">
          <TextBlock code={data.data.error.stackTrace} />
        </Item>
        <Item title="Пайплайн" >
          <JsBlock code={data.data.pipeline} />
        </Item>
      </TabPanel>
    );
  }
};

export default MasterDetailView;

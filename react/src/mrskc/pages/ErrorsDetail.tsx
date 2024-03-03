import React from "react";
import TextBlock from "src/mrskc/components/TextBlock";


// import { CopyBlock, a11yLight } from "react-code-blocks";

interface Props {
  data: {
    key: string;
    data: any;
  };
}

const MasterDetailView: React.FC<Props> = ({ data }) => {
 
    return ( <TextBlock code={data.data.stackTrace} />);
 
};

export default MasterDetailView;

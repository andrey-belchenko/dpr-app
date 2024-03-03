import React from "react";
import ReactJson from "react-json-view";


interface Props {
  data: {
    key: string;
    data: any;
  };
}

const MasterDetailView: React.FC<Props> = ({ data }) => {
  return (
    <ReactJson src={data.data} displayDataTypes={false} name={false}/>
  );
};

export default MasterDetailView;

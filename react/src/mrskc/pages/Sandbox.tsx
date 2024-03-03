// Сделано на основе примера отсюда https://reactflow.dev/examples/layout/dagre
import React, { useEffect } from "react";
import { useParams } from "react-router-dom";
import ReactFlow, {
  ConnectionLineType,
  useNodesState,
  useEdgesState,
} from "reactflow";

import "reactflow/dist/style.css";
import { getArray } from "src/mrskc/data/apiClient";
import { getLayoutedElements } from "src/mrskc/pages/SandboxUtils";

const LayoutFlow = () => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  let { lineCode } = useParams();
  useEffect(() => {
    const fetchData = async () => {
      // Получение данных из БД. Данные взяты из примера в документации и записаны в коллекции в БД exchange-sukhanov
      // Можно записать свои данные в эти же коллекции, формат см. в примерах в документации.

      let nodesResult = await getArray(
        "flowNodes" // Имя коллекции
        // ,{ lineCode: lineCode } // пример, как задать фильтр "Имя поля в коллекции": Значение из переменной lineCode (в нее записана последняя часть URL - код линии)
      );
      let edgesResult = await getArray(
        "flowEdges" // Имя коллекции
        // ,{ lineCode: lineCode } 
      );
      // Эта функция выполняет расположение элементов, взята из примера в документации. Наверное ее можно доработать под свои потребности
      const processedData = getLayoutedElements(
        nodesResult.data,
        edgesResult.data
      );
      setNodes(processedData.nodes);
      setEdges(processedData.edges);
    };
    fetchData();
  }, [lineCode]);

  return (
    <div style={{ width: "100vw", height: "100vh", backgroundColor: "white" }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        connectionLineType={ConnectionLineType.SmoothStep}
        fitView
      >
          {lineCode}

      </ReactFlow>
    </div>
  );
};

export default LayoutFlow;

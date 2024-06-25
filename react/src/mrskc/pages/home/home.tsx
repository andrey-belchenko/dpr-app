import React from "react";
import "./home.scss";
import Box, { Item } from "devextreme-react/box";
import Toolbar, { Item as ToolbarItem } from "devextreme-react/toolbar";
import {
  Chart,
  Series,
  ArgumentAxis,
  CommonSeriesSettings,
  Export,
  Legend,
  Margin,
  Title,
  Subtitle,
  Tooltip,
  Grid,
  Label,
} from "devextreme-react/chart";

import { overlappingModes, population, seriesTypeLabel } from "./data.js";

function chart(title: string, dataSource: any) {
  return (
    <div className="tile">
      <Toolbar>
        <ToolbarItem
          location="after"
          widget="dxButton"
          options={{ icon:"to" }}
        />
      </Toolbar>

      <Chart className="chart" dataSource={dataSource} title={title}>
        <Series argumentField="date" type="bar" />
        <ArgumentAxis>
          <Label wordWrap="none" overlappingBehavior="rotate" />
        </ArgumentAxis>
        <Legend visible={false} />
      </Chart>
      {/* <Chart
          palette="Harmony Light"
          title="Corporations with Highest Market Value"
          dataSource={population}>
          <CommonSeriesSettings
            argumentField="date"
            type="stackedsplinearea"
          />
          <Series valueField="date" name="2005"></Series>
          <Series valueField="y2004" name="2004"></Series>
          <ArgumentAxis valueMarginsEnabled={false} />
          <Legend
            verticalAlignment="bottom"
            horizontalAlignment="center"
          />
          <Margin bottom={20} />
          <Export enabled={true} />
        </Chart> */}
    </div>
  );
}

export default function Home() {
  return (
    <React.Fragment>
      {/* <Box direction="col" className="box">
        <Item ratio={2} baseSize={0}>
          <Box direction="row" width="100%" height="100%">
            <Item ratio={1}>
              {chart("Входящие сообщения [пример]", population)}
            </Item>
            <Item ratio={1}>{chart("Отложенные сообщения [пример]", [])}</Item>
            <Item ratio={1}>{chart("Состояние сервисов [пример]", [])}</Item>
          </Box>
        </Item>
        <Item ratio={2} baseSize={0}>
          <Box direction="row" width="100%" height="100%">
            <Item ratio={1}>{chart("Исходящие СК-11 [пример]", [])}</Item>
            <Item ratio={1}>{chart("Исходящие РГИС [пример]", [])}</Item>
            <Item ratio={1}>{chart("Предупреждения [пример]", [])}</Item>
          </Box>
        </Item>
      </Box> */}
    </React.Fragment>
  );
}

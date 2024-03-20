import {
  HomePage,
  ExamplePage,
  ProfilePage,
  SegmentTree,
  LineSpanSchema,
  LineSegmentSchema,
  LineMatchSchema,
  LineSegmentSchemaExt,
  ObjectTree,
  IdMatching,
  ObjectTable,
  Sandbox,
  BlockedMessages,
  TopologyMatching,
  IncomingMessages,
  PipelineLog,
  KeyProcessingLog,
  CurrentTest,
  Errors,
  ExchangeLog,
  SkSentObjects,
  Warnings,
  Markers,
  SwitchesStat,
  Empty,
  MatchingTree,
  AplInfo,
  LineStatus,
  OutRgis,
  Notifications,
  FilesTable,
  MatchedLines,
  TopologyIntegrityCheck,
  RunLineMatching,
  LineMatchingResults,
  ProcessorStatus,
} from "./pages/_index";
import { withNavigationWatcher } from "../common/contexts/navigation";
import LineEquipmentSchema from "./pages/LineEquipmentSchema";
import { element } from "prop-types";
import UserPage from "./pages/UserPage";

const routes = [
  {
    path: "/example",
    element: ExamplePage,
  },
  {
    path: "/profile",
    element: ProfilePage,
  },
  {
    path: "/home",
    element: HomePage,
  },
  {
    path: "/SegmentTree",
    element: SegmentTree,
  },
  // {
  //   path: "/LineSegmentSchema/:lineCode",
  //   element: LineSegmentSchema,
  // },
  // {
  //   path: "/LineSpanSchema/:lineCode",
  //   element: LineSpanSchema,
  // },
  {
    path: "/LineSegmentSchemaExt",
    element: LineSegmentSchemaExt,
  },
  // {
  //     path: '/LineMatchSchema',
  //     element: LineMatchSchema
  // },
  // {
  //   path: "/LineEquipmentSchema",
  //   element: LineEquipmentSchema,
  // },
  {
    path: "/test/:id",
    element: UserPage,
  },
  {
    path: "/ObjectTree",
    element: ObjectTree,
  },
  {
    path: "/ObjectTable",
    element: ObjectTable,
  },
  {
    path: "/IdMatching",
    element: IdMatching,
  },
  {
    path: "/Sandbox",
    element: Sandbox,
  },
  {
    path: "/BlockedMessages",
    element: BlockedMessages,
  },
  {
    path: "/TopologyMatching",
    element: MatchedLines,
  },
  {
    path: "/IncomingMessages",
    element: IncomingMessages,
  },
  {
    path: "/PipelineLog",
    element: PipelineLog,
  },
  {
    path: "/KeyProcessingLog",
    element: KeyProcessingLog,
  },
  {
    path: "/CurrentTest",
    element: CurrentTest,
  },
  {
    path: "/Errors",
    element: Errors,
  },
  {
    path: "/ExchangeLog",
    element: ExchangeLog,
  },
  {
    path: "/SkSentObjects",
    element: SkSentObjects,
  },
  {
    path: "/Warnings",
    element: Warnings,
  },
  {
    path: "/Markers",
    element: Markers,
  },
  {
    path: "/SwitchesStat",
    element: SwitchesStat,
  },
  {
    path: "/MatchingTree",
    element: MatchingTree,
  },
  {
    path: "/AplInfo",
    element: AplInfo,
  },
  {
    path: "/LineStatus",
    element: LineStatus,
  },
  {
    path: "/OutRgis",
    element: OutRgis,
  },
  {
    path: "/Notifications",
    element: Notifications,
  },
  {
    path: "/FilesTable",
    element: FilesTable,
  },
  {
    path: "/TopologyIntegrityCheck",
    element: TopologyIntegrityCheck,
  },
  {
    path: "/RunLineMatching",
    element: RunLineMatching,
  },
  {
    path: "/LineMatchingResults",
    element: LineMatchingResults,
  },
  {
    path: "/ProcessorStatus",
    element: ProcessorStatus,
  }


  // {
  //     path: '',
  //     element: Empty
  // }
];

export default routes.map((route) => {
  return {
    ...route,
    element: withNavigationWatcher(route.element, route.path),
  };
});

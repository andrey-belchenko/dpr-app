import { Routes, Route, Navigate } from "react-router-dom";
import appInfo from "./app-info";
import routes from "./app-routes";
import { SideNavOuterToolbar as SideNavBarLayout } from "./layouts";

import {
  LineSpanSchema,
  LineSegmentSchema,
  Sandbox,
  LineEquipmentSchema
} from "./pages/_index";

export default function Content() {
  return (
    <Routes>
      <Route path="/Sandbox/:lineCode" element={<Sandbox/>} />
      <Route path="/LineSegmentSchema/:lineCode" element={<LineSegmentSchema/>} />
      <Route path="/LineSpanSchema/:lineCode" element={<LineSpanSchema/>} />
      <Route path="/LineEquipmentSchema/:lineCode" element={<LineEquipmentSchema/>} />
      <Route
        path="/*"
        element={
          <SideNavBarLayout title={appInfo.title}>
            <Routes>
              {routes.map(({ path, element }) => (
                <Route key={path} path={path} element={element} />
              ))}
              <Route path="*" element={<Navigate to="home" />} />
            </Routes>
          </SideNavBarLayout>
        }
      />
    </Routes>
  );
}

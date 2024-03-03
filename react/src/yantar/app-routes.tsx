import {
  Empty, Test
} from "./pages/_index";
import { withNavigationWatcher } from "./contexts/navigation";

const routes = [
  {
    path: "/home",
    element: Empty,
  },
  {
    path: "/test",
    element: Test,
  },
 
];

export default routes.map((route) => {
  return {
    ...route,
    element: withNavigationWatcher(route.element, route.path),
  };
});

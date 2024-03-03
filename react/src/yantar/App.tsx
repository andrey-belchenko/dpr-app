import "devextreme/dist/css/dx.common.css";
import "../themes/generated/theme.base.css";
import "../themes/generated/theme.additional.css";
import React, { useEffect, useState } from "react";
import { Route, HashRouter as Router, Routes } from "react-router-dom";
import "../dx-styles.scss";
import LoadPanel from "devextreme-react/load-panel";
import { NavigationProvider } from "src/common/contexts/navigation";
import { useScreenSizeClass } from "./utils/media-query";
import Content from "./Content";
import messages from "devextreme/localization/messages/ru.json";
import { locale, loadMessages } from "devextreme/localization";
import { QueryParamProvider } from "use-query-params";
import { ReactRouter6Adapter } from "use-query-params/adapters/react-router-6";
import { CookiesProvider, useCookies } from "react-cookie";
import { KeycloakAuthProvider } from "src/common/contexts/keycloak";


export default function App() {
  const [initialized, setInitialized] = useState(false);
  useEffect(() => {
    loadMessages(messages);
    locale(navigator.language);
    setInitialized(true);
  }, []);

  const screenSizeClass = useScreenSizeClass();

  if (initialized) {
    return (
      <CookiesProvider>
        <KeycloakAuthProvider>
          <Router>
            <QueryParamProvider adapter={ReactRouter6Adapter}>
              <NavigationProvider>
                <div className={`app ${screenSizeClass}`}>
                <Content />
                </div>
              </NavigationProvider>
            </QueryParamProvider>
          </Router>
        </KeycloakAuthProvider>
      </CookiesProvider>
    );
  } else {
    return <div />;
  }
}

// export default function Root() {
//   return (
//     <Router>
//       <Routes>
//         <Route path="/" Component={Sandbox} />
//       </Routes>
//     </Router>
//   );
// }

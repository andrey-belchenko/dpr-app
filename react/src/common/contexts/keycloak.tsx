import Keycloak from "keycloak-js";
import React, {
  useContext,
  createContext,
  useCallback,
  useState,
  useEffect,
} from "react";
import { useCookies } from "react-cookie";


export enum KeycloakRole {
  Аналитик = "Voronezh_rs20",
  Администратор = "Voronezh_rs20_Администратор",
  Инженер = "Voronezh_rs20_Инженер",
  Эксперт = "Voronezh_rs20_Эксперт",
}

// тут есть химия связанная с особенностями поведения keycloak-js в сочетании с HashRouter и другими провайдерами
// сделать тупо по примерам из интернета не получилось
const initOptions = {
  url: process.env.REACT_APP_adp_keycloak_url as string,
  realm: process.env.REACT_APP_adp_keycloak_realm as string,
  clientId: process.env.REACT_APP_adp_keycloak_clientId as string,
};
const keycloak = new Keycloak(initOptions);

interface KeycloakAuthProps {
  token: string;
  username: string;
  roles: KeycloakRole[];
  logout: () => void;
}

const KeycloakAuthContext = createContext<KeycloakAuthProps | undefined>(
  undefined
);
let keycloakInitialized = false;
async function keycloakInit() {
  if (!keycloakInitialized) {
    keycloakInitialized = true;
    await keycloak.init({ onLoad: "login-required" });
  }
}

let doingExit = false;
const KeycloakAuthProvider: React.FC = ({ children }) => {
  const [cookies, setCookie, removeCookie] = useCookies(["auth", "exit"]);
  const logout = useCallback(() => {
    removeCookie("auth");
    doingExit = true;
    const action = async () => {
      await keycloakInit();
      keycloak.logout();
    };
    action();
  }, []);

  if (doingExit) {
    return <div />;
  } else if (!cookies.auth) {
    const login = async () => {
      await keycloakInit();
      if (!keycloak.token) {
        return null;
      }
      return {
        token: keycloak.token,
        username: keycloak.tokenParsed?.preferred_username,
        roles: keycloak.tokenParsed?.realm_access?.roles,
      };
    };
    const action = async () => {
      const authInfo = await login();
      if (authInfo) {
        setCookie("auth", authInfo);
      }
    };
    action();
    return <div />;
  } else {
    let roles = [];
    for (let role of cookies.auth.roles) {
      // if (Object.values(KeycloakRole).includes(role)) {
        roles.push(role);
      // }
    }
    return (
      <KeycloakAuthContext.Provider
        value={{
          token: cookies.auth.token,
          username: cookies.auth.username,
          roles: roles,
          logout: logout,
        }}
      >
        {children}
      </KeycloakAuthContext.Provider>
    );
  }
};

const useKeycloakAuth = (): KeycloakAuthProps => {
  const context = useContext(KeycloakAuthContext);
  if (!context) {
    throw new Error(
      "useKeycloakAuth must be used within a KeycloakAuthProvider"
    );
  }
  return context;
};

export { KeycloakAuthProvider, useKeycloakAuth };

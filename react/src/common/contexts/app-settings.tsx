import Keycloak from "keycloak-js";
import React, {
  useContext,
  createContext,
  useCallback,
  useState,
  useEffect,
} from "react";
import { useCookies } from "react-cookie";
import { useLocation } from "react-router-dom";

interface AppSettingsProps {
  menuData: any[];
  allowDbSelection: boolean;
}

interface AppSettingsParams extends AppSettingsProps {
  children: React.ReactNode;
}

const AppSettingsContext = createContext<AppSettingsProps>({
  menuData: [],
  allowDbSelection: false,
});

const AppSettingsProvider = ({ menuData, allowDbSelection, children }: AppSettingsParams) => {

  // TODO исправление проблемы возникающей из-за того что кейклок добавляет #iis=... к url
  const location = useLocation();
  useEffect(() => {
    if (window.location.hash.includes("iss=")) {
      window.location.hash = "";
    }
  }, [location]);

  return (
    <AppSettingsContext.Provider
      value={{
        menuData: menuData,
        allowDbSelection: allowDbSelection
      }}
    >
      {children}
    </AppSettingsContext.Provider>
  );
};

const useAppSettings = (): AppSettingsProps => {
  const context = useContext(AppSettingsContext);
  if (!context) {
    throw new Error("useAppSettings must be used within a AppSettingsProvider");
  }
  return context;
};

export { AppSettingsProvider, useAppSettings };

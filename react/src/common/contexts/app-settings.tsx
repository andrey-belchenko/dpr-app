import Keycloak from "keycloak-js";
import React, {
  useContext,
  createContext,
  useCallback,
  useState,
  useEffect,
} from "react";
import { useCookies } from "react-cookie";

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

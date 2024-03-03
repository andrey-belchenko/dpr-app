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
}

interface AppSettingsParams extends AppSettingsProps {
  children: React.ReactNode;
}

const AppSettingsContext = createContext<AppSettingsProps>({ menuData: [] });

const AppSettingsProvider = ({ menuData, children }: AppSettingsParams) => {
  return (
    <AppSettingsContext.Provider
      value={{
        menuData: menuData,
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

import React, { useState } from "react";
import Toolbar, { Item } from "devextreme-react/toolbar";
import Button from "devextreme-react/button";
import "./Header.scss";
// import { Template } from "devextreme-react/core/template";
import SelectBox from "devextreme-react/select-box";
import type { HeaderProps } from "../../types";
import DropDownButton from "devextreme-react/drop-down-button";
import { useCookies } from "react-cookie";
import { useNavigate } from "react-router-dom";
import { useKeycloakAuth } from "src/contexts/keycloak";
import DialogOk from "../DialogOk";

// import { useKeycloak } from "@react-keycloak/web";

// import { useAuth } from "src/contexts/auth";

export default function Header({
  menuToggleEnabled,
  title,
  pageTitle,
  toggleMenu,
}: HeaderProps) {
  // const { keycloak, initialized } = useKeycloak();

  const auth = useKeycloakAuth();
  let defaultDatabase = "exchange"; // временно

  let dbList = ["exchange", "exchange-demo"];
  if (process.env.REACT_APP_adp_isDev == "true") {
    dbList = ["exchange", "exchange-sukhanov", "exchange-test","exchange-demo"];
    // defaultDatabase = "exchange";
  }

  const [databases] = useState(dbList);
  const [cookies, setCookie] = useCookies(["database"]);

  let pageTitleItems = (pageTitle?: string[]) => {
    let result: JSX.Element[] = [];
    if (pageTitle) {
      for (let name of pageTitle) {
        result.push(
          <Item
            location={"before"}
            cssClass={"dx-icon-chevronright header-separator"}
          />
        );
        result.push(
          <Item
            location={"before"}
            cssClass={"header-page-title"}
            text={name}
          />
        );
      }
    }
    return result;
  };

  const [rolesDialogVisible, setRolesDialogVisible] = useState(false);
  return (
    <header className={"header-component"}>
      <DialogOk
        // height={300}
        width={400}
        title={"Роли пользователя " + auth.username}
        visible={rolesDialogVisible}
        onOk={() => setRolesDialogVisible(false)}
      >
        {auth.roles.join(", ")}
      </DialogOk>
      <Toolbar className={"header-toolbar"}>
        <Item
          visible={menuToggleEnabled}
          location={"before"}
          widget={"dxButton"}
          cssClass={"menu-button"}
        >
          <Button icon="menu" stylingMode="text" onClick={toggleMenu} />
        </Item>

        <Item
          location={"before"}
          cssClass={"header-title"}
          text={title}
          visible={!!title}
        />
        {pageTitleItems(pageTitle)}
        <Item
          location={"after"}
          locateInMenu={"never"}
          // visible={process.env.REACT_APP_adp_isDev == "true"}
        >
          <DropDownButton
            text={cookies.database || defaultDatabase}
            icon="contentlayout"
            stylingMode="text"
            className="header-button"
            dropDownOptions={{ width: "auto", resizeEnabled: true }}
            items={databases}
            onItemClick={(e) => {
              setCookie("database", e.itemData);
              window.location.reload();
            }}
          />
        </Item>

        <Item location={"after"} locateInMenu={"never"}>
          <DropDownButton
            text={auth.username}
            icon="user"
            stylingMode="text"
            className="header-button"
            dropDownOptions={{ width: "auto", resizeEnabled: true }}
            items={["Выход", "Роли"]}
            onItemClick={(e) => {
              switch (e.itemData) {
                case "Выход":
                  auth.logout();
                  break;
                case "Роли":
                  setRolesDialogVisible(true);
                  break;
              }
            }}
          />
        </Item>
      </Toolbar>
    </header>
  );
}

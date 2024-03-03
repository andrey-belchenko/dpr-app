import Drawer from "devextreme-react/drawer";
import ScrollView from "devextreme-react/scroll-view";
import React, { useState, useCallback, useRef } from "react";
import { useNavigate } from "react-router";
import { Header, SideNavigationMenu, Footer } from "../../components";
import "./side-nav-outer-toolbar.scss";
import { useScreenSize } from "../../utils/media-query";
import { Template } from "devextreme-react/core/template";
import { useMenuPatch } from "../../utils/patches";
import type { SideNavToolbarProps } from "../../types";

export default function SideNavOuterToolbar({
  title,
  children,
}: React.PropsWithChildren<SideNavToolbarProps>) {
  const scrollViewRef = useRef<ScrollView>(null);
  const navigate = useNavigate();
  const { isXSmall, isLarge } = useScreenSize();
  const [patchCssClass, onMenuReady] = useMenuPatch();
  const [menuStatus, setMenuStatus] = useState(
    isLarge ? MenuStatus.Opened : MenuStatus.Closed
  );

  let value: string[] = [];
  const [pageTitle, setPageTitle] = useState(value);

  const toggleMenu = useCallback(({ event }) => {
    setMenuStatus((prevMenuStatus) =>
      prevMenuStatus === MenuStatus.Closed
        ? MenuStatus.Opened
        : MenuStatus.Closed
    );
    event.stopPropagation();
  }, []);

  const temporaryOpenMenu = useCallback(() => {
    setMenuStatus((prevMenuStatus) =>
      prevMenuStatus === MenuStatus.Closed
        ? MenuStatus.TemporaryOpened
        : prevMenuStatus
    );
  }, []);

  // const onOutsideClick = useCallback(() => {
  //   setMenuStatus(
  //     prevMenuStatus => prevMenuStatus !== MenuStatus.Closed && !isLarge
  //       ? MenuStatus.Closed
  //       : prevMenuStatus
  //   );
  //   return true;
  // }, [isLarge]);

  const onNavigationChanged = useCallback(
    (pars: any) => {

      // { itemData, event, node } = pars

      let node = pars.node;
      let itemData = pars.itemData;
      let path = itemData.path;
      let event = pars.event;
      if (!event) {
        // todo: химия чтобы обновить заголовок при старте.
        // наверное лучше переделать
  
        let titles = [];
        let item = node;
        while (item) {
          titles.push(item.text); //.replace("𑂾 ","")
          item = item.parent;
        }
        titles = titles.reverse();

        setPageTitle(titles);
        return;
      }

      if (menuStatus === MenuStatus.Closed || !path || node.selected) {
        event.preventDefault();
        return;
      }

      navigate(path);
      scrollViewRef.current?.instance.scrollTo(0);

      if (!isLarge || menuStatus === MenuStatus.TemporaryOpened) {
        setMenuStatus(MenuStatus.Closed);
        event.stopPropagation();
      }
    },
    [navigate, menuStatus, isLarge]
  );

  return (
    <div className={"side-nav-outer-toolbar"}>
      <Header
        menuToggleEnabled
        toggleMenu={toggleMenu}
        title={title}
        pageTitle={pageTitle}
      />
      <Drawer
        className={["drawer", patchCssClass].join(" ")}
        position={"before"}
        // closeOnOutsideClick={onOutsideClick}
        closeOnOutsideClick={false}
        openedStateMode={isLarge ? "shrink" : "overlap"}
        revealMode={isXSmall ? "slide" : "expand"}
        minSize={isXSmall ? 0 : 60}
        maxSize={260}
        shading={isLarge ? false : true}
        opened={menuStatus === MenuStatus.Closed ? false : true}
        template={"menu"}
      >
        <div className={"container"}>
          <ScrollView ref={scrollViewRef} className={"layout-body with-footer"}>
            <div className={"content"}>
              {React.Children.map(children, (item: any) => {
                return item.type !== Footer && item;
              })}
            </div>
            <div className={"content-block"}>
              {React.Children.map(children, (item: any) => {
                return item.type === Footer && item;
              })}
            </div>
          </ScrollView>
        </div>
        <Template name={"menu"}>
          <SideNavigationMenu
            compactMode={menuStatus === MenuStatus.Closed}
            selectedItemChanged={onNavigationChanged}
            openMenu={temporaryOpenMenu}
            onMenuReady={onMenuReady}
          ></SideNavigationMenu>
        </Template>
      </Drawer>
    </div>
  );
}

const MenuStatus = {
  Closed: 1,
  Opened: 2,
  TemporaryOpened: 3,
};

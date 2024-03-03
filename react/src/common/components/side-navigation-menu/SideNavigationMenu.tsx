import React, { useEffect, useRef, useCallback, useMemo } from "react";
import TreeView from "devextreme-react/tree-view";
import { useNavigation } from "../../contexts/navigation";
import { useScreenSize } from "../../utils/media-query";
import "./SideNavigationMenu.scss";
import type { SideNavigationMenuProps } from "../../types";
import * as events from "devextreme/events";
import { useKeycloakAuth } from "src/common/contexts/keycloak";
import { useAppSettings } from "src/common/contexts/app-settings";
import {Hint} from "src/common/components";

export default function SideNavigationMenu(
  props: React.PropsWithChildren<SideNavigationMenuProps>
) {
  let auth = useKeycloakAuth();
  const { children, selectedItemChanged, openMenu, compactMode, onMenuReady } =
    props;

  const { isLarge } = useScreenSize();

  // проверен только верхний уровень
  function processMenuItems(items: any[], level: number = 0): any[] {
    // фильтрация по ролям
    // добавление уровня
    const filtered: any[] = [];
    for (let item of items) {
      let take = true;
      item.level = level;
      if (item.roles) {
        take = false;
        for (let role of item.roles) {
          if (auth.roles.includes(role)) {
            take = true;
          }
        }
      }
      if (take) {
        let newItem = { ...item };
        filtered.push(newItem);
        if (newItem.items) {
          newItem.items = processMenuItems(newItem.items, level + 1);
        }
      }
    }
    return filtered;
  }

  const {menuData} =  useAppSettings()

  function normalizePath() {
    const filtered = processMenuItems(menuData);

    return filtered.map((item) => ({
      ...item,
      // expanded: isLarge,
      expanded: false,
      path: item.path && !/^\//.test(item.path) ? `/${item.path}` : item.path,
      // id: item.path || item.text
    }));
  }

  const items = useMemo(
    normalizePath,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  const {
    navigationData: { currentPath },
  } = useNavigation();


  const treeViewRef = useRef<TreeView>(null);
  const wrapperRef = useRef();
  const getWrapperRef = useCallback(
    (element) => {
      const prevElement = wrapperRef.current;
      if (prevElement) {
        events.off(prevElement, "dxclick");
      }

      wrapperRef.current = element;
      events.on(element, "dxclick", (e: React.PointerEvent) => {
        openMenu(e);
      });
    },
    [openMenu]
  );

  useEffect(() => {
    const treeView = treeViewRef.current && treeViewRef.current.instance;
    if (!treeView) {
      return;
    }

    if (currentPath !== undefined) {
      treeView.selectItem(currentPath);
      treeView.expandItem(currentPath);
    }

    if (compactMode) {
      treeView.collapseAll();
    }
  }, [currentPath, compactMode]);
  const renderListItem = (data: any, index: any) => {
    let icon: any = null;
    if (data.icon) {
      icon = <i className={"dx-icon df-menu-icon dx-icon-" + data.icon} />;
    }
    let hintIcon: any = null;
    if (data.hintIcon) {
      const hintId = "menu-item" + index + "-hint";
      hintIcon = (
        <React.Fragment>
          <i
            id={hintId}
            className={"df-menu-hint-icon dx-icon dx-icon-" + data.hintIcon}
          />
          <Hint target={"#" + hintId} position="right">
            <div className="df-vertical-center-container">
              <i
                className={
                  "df-vertical-center df-hint-icon dx-icon-" + data.hintIcon
                }
              />
              <span className="df-vertical-center df-text">
                {data.hintText}
              </span>
            </div>
          </Hint>
        </React.Fragment>
      );
    }
    let indent = data.level - 1;
    let indentClassName = "";
    if (indent > 0) {
      indentClassName = "df-menu-item-indent-" + indent;
    }
    return (
      <React.Fragment>
        {icon}
        <span className={indentClassName}>
          {data.text}
        </span>
        {hintIcon}
      </React.Fragment>
    );
  };
  return (
    <div
      className={"dx-swatch-additional side-navigation-menu"}
      ref={getWrapperRef}
    >
      {children}
      <div className={"menu-container"}>
        <TreeView
          ref={treeViewRef}
          items={items}
          keyExpr={"path"}
          selectionMode={"single"}
          focusStateEnabled={false}
          expandEvent={"click"}
          onItemSelectionChanged={selectedItemChanged}
          onItemClick={selectedItemChanged}
          onContentReady={onMenuReady}
          width={"100%"}
          itemRender={renderListItem}
        ></TreeView>
      </div>
    </div>
  );
}

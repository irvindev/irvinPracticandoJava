package com.pe.allpafood.api.transaction.catalog.dto.comanda;

import lombok.Getter;

@Getter
public class OrderDTO{
    private Integer menuTypeId;
    private Integer menuId;
    private String menuType;
    private String menuName;
    private Integer count;

    // Constructor
    public OrderDTO(Integer menuTypeId, Integer menuId,String menuType, String menuName, Integer count) {
        this.menuTypeId = menuTypeId;
        this.menuId = menuId;

        this.menuType = menuType;
        this.menuName = menuName;
        this.count = count;
    }
    public void setMenuTypeId(Integer menuTypeId) { this.menuTypeId = menuTypeId; }

    public void setMenuId(Integer menuId) { this.menuId = menuId; }

    public void setMenuType(String menuType) { this.menuType = menuType; }

    public void setMenuName(String menuName) { this.menuName = menuName; }

    public void setCount(Integer count) { this.count = count; }

    public void incrementCount() { this.count++; }
}

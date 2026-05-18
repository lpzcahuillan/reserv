package com.lpzcahuillan.menu.service;

import com.lpzcahuillan.menu.dto.CategoryRequest;
import com.lpzcahuillan.menu.dto.CategoryResponse;
import com.lpzcahuillan.menu.dto.MenuItemRequest;
import com.lpzcahuillan.menu.dto.MenuItemResponse;

import java.util.List;

public interface MenuService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);

    MenuItemResponse createMenuItem(MenuItemRequest request);
    List<MenuItemResponse> getAllMenuItems();
    List<MenuItemResponse> getMenuItemsByCategory(Long categoryId);
    MenuItemResponse getMenuItemById(Long id);
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest request);
    void deleteMenuItem(Long id);
}

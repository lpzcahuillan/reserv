package com.lpzcahuillan.menu.service;

import com.lpzcahuillan.menu.dto.CategoryRequest;
import com.lpzcahuillan.menu.dto.CategoryResponse;
import com.lpzcahuillan.menu.dto.MenuItemRequest;
import com.lpzcahuillan.menu.dto.MenuItemResponse;
import com.lpzcahuillan.menu.entity.Category;
import com.lpzcahuillan.menu.entity.MenuItem;
import com.lpzcahuillan.menu.exception.BadRequestException;
import com.lpzcahuillan.menu.exception.ResourceNotFoundException;
import com.lpzcahuillan.menu.repository.CategoryRepository;
import com.lpzcahuillan.menu.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creando categoría: {}", request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Intento de crear categoría con nombre existente: {}", request.getName());
            throw new BadRequestException("Category name already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Categoría creada exitosamente con id: {} y nombre: {}", saved.getId(), saved.getName());
        return mapToCategoryResponse(saved);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.debug("Obteniendo todas las categorías");
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} categorías", categories.size());
        return categories;
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Obteniendo categoría con id: {}", id);
        return categoryRepository.findById(id)
                .map(this::mapToCategoryResponse)
                .orElseThrow(() -> {
                    log.warn("Categoría no encontrada con id: {}", id);
                    return new ResourceNotFoundException("Category not found");
                });
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Eliminando categoría con id: {}", id);
        if (!categoryRepository.existsById(id)) {
            log.warn("Categoría no encontrada con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
        log.info("Categoría con id: {} eliminada exitosamente", id);
    }

    @Override
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        log.info("Creando ítem de menú: {}, id de categoría: {}", request.getName(), request.getCategoryId());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Categoría no encontrada con id: {} para crear ítem de menú", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found");
                });

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : MenuItem.ItemStatus.AVAILABLE)
                .category(category)
                .build();
        MenuItem saved = menuItemRepository.save(item);
        log.info("Ítem de menú creado exitosamente con id: {}, nombre: {}, precio: {}", saved.getId(), saved.getName(), saved.getPrice());
        return mapToMenuItemResponse(saved);
    }

    @Override
    public List<MenuItemResponse> getAllMenuItems() {
        log.debug("Obteniendo todos los ítems de menú");
        List<MenuItemResponse> items = menuItemRepository.findAll().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} ítems de menú", items.size());
        return items;
    }

    @Override
    public List<MenuItemResponse> getMenuItemsByCategory(Long categoryId) {
        log.debug("Obteniendo ítems de menú para categoría: {}", categoryId);
        List<MenuItemResponse> items = menuItemRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
        log.info("Se obtuvieron {} ítems de menú para categoría {}", items.size(), categoryId);
        return items;
    }

    @Override
    public MenuItemResponse getMenuItemById(Long id) {
        log.debug("Obteniendo ítem de menú con id: {}", id);
        return menuItemRepository.findById(id)
                .map(this::mapToMenuItemResponse)
                .orElseThrow(() -> {
                    log.warn("Ítem de menú no encontrado con id: {}", id);
                    return new ResourceNotFoundException("Menu item not found");
                });
    }

    @Override
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        log.info("Actualizando ítem de menú con id: {}", id);
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ítem de menú no encontrado con id: {} durante la actualización", id);
                    return new ResourceNotFoundException("Menu item not found");
                });

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Categoría no encontrada con id: {} durante actualizar ítem de menú", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found");
                });

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setStatus(request.getStatus());
        item.setCategory(category);

        MenuItem updated = menuItemRepository.save(item);
        log.info("Ítem de menú con id: {} actualizado exitosamente", id);
        return mapToMenuItemResponse(updated);
    }

    @Override
    public void deleteMenuItem(Long id) {
        log.info("Eliminando ítem de menú con id: {}", id);
        if (!menuItemRepository.existsById(id)) {
            log.warn("Ítem de menú no encontrado con id: {} durante la eliminación", id);
            throw new ResourceNotFoundException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
        log.info("Ítem de menú con id: {} eliminado exitosamente", id);
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .status(item.getStatus())
                .categoryId(item.getCategory().getId())
                .build();
    }
}

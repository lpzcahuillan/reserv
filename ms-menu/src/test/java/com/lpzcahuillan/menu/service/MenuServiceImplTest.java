package com.lpzcahuillan.menu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MenuServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuServiceImpl service;

    // --- CATEGORY TESTS ---

    @Test
    void createCategory_Success() {
        // Given
        CategoryRequest request = CategoryRequest.builder()
            .name("Entradas")
            .description("Platos de entrada")
            .build();

        Category savedCategory = Category.builder()
            .id(1L)
            .name("Entradas")
            .description("Platos de entrada")
            .build();

        when(categoryRepository.existsByName("Entradas")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(
            savedCategory
        );

        // When
        CategoryResponse response = service.createCategory(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Entradas", response.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_NameAlreadyExists() {
        // Given
        CategoryRequest request = CategoryRequest.builder()
            .name("Entradas")
            .build();

        when(categoryRepository.existsByName("Entradas")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> service.createCategory(request)
        );
        assertEquals("Category name already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_Success() {
        // Given
        Category cat = Category.builder().id(1L).name("Bebidas").build();
        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        // When
        List<CategoryResponse> response = service.getAllCategories();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Bebidas", response.get(0).getName());
    }

    @Test
    void getCategoryById_Success() {
        // Given
        Long id = 1L;
        Category cat = Category.builder().id(id).name("Postres").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));

        // When
        CategoryResponse response = service.getCategoryById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Postres", response.getName());
    }

    @Test
    void getCategoryById_NotFound() {
        // Given
        Long id = 99L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getCategoryById(id)
        );
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void deleteCategory_Success() {
        // Given
        Long id = 1L;
        when(categoryRepository.existsById(id)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(id);

        // When
        assertDoesNotThrow(() -> service.deleteCategory(id));

        // Then
        verify(categoryRepository).deleteById(id);
    }

    @Test
    void deleteCategory_NotFound() {
        // Given
        Long id = 99L;
        when(categoryRepository.existsById(id)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.deleteCategory(id)
        );
        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository, never()).deleteById(id);
    }

    // --- MENU ITEM TESTS ---

    @Test
    void createMenuItem_Success() {
        // Given
        MenuItemRequest request = MenuItemRequest.builder()
            .name("Pisco Sour")
            .description("Bebida tradicional chilena")
            .price(new BigDecimal("5000"))
            .categoryId(1L)
            .status(MenuItem.ItemStatus.AVAILABLE)
            .build();

        Category category = Category.builder().id(1L).name("Bebidas").build();
        MenuItem savedItem = MenuItem.builder()
            .id(10L)
            .name("Pisco Sour")
            .description("Bebida tradicional chilena")
            .price(new BigDecimal("5000"))
            .category(category)
            .status(MenuItem.ItemStatus.AVAILABLE)
            .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(
            savedItem
        );

        // When
        MenuItemResponse response = service.createMenuItem(request);

        // Then
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Pisco Sour", response.getName());
        assertEquals(1L, response.getCategoryId());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void createMenuItem_CategoryNotFound() {
        // Given
        MenuItemRequest request = MenuItemRequest.builder()
            .categoryId(99L)
            .build();

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.createMenuItem(request)
        );
        assertEquals("Category not found", exception.getMessage());
        verify(menuItemRepository, never()).save(any(MenuItem.class));
    }

    @Test
    void getAllMenuItems_Success() {
        // Given
        Category category = Category.builder().id(1L).build();
        MenuItem item = MenuItem.builder()
            .id(10L)
            .name("Item")
            .category(category)
            .build();
        when(menuItemRepository.findAll()).thenReturn(List.of(item));

        // When
        List<MenuItemResponse> response = service.getAllMenuItems();

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getMenuItemsByCategory_Success() {
        // Given
        Long categoryId = 1L;
        Category category = Category.builder().id(categoryId).build();
        MenuItem item = MenuItem.builder()
            .id(10L)
            .name("Item")
            .category(category)
            .build();
        when(menuItemRepository.findByCategoryId(categoryId)).thenReturn(
            List.of(item)
        );

        // When
        List<MenuItemResponse> response = service.getMenuItemsByCategory(
            categoryId
        );

        // Then
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(categoryId, response.get(0).getCategoryId());
    }

    @Test
    void getMenuItemById_Success() {
        // Given
        Long id = 10L;
        Category category = Category.builder().id(1L).build();
        MenuItem item = MenuItem.builder()
            .id(id)
            .name("Item")
            .category(category)
            .build();
        when(menuItemRepository.findById(id)).thenReturn(Optional.of(item));

        // When
        MenuItemResponse response = service.getMenuItemById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
    }

    @Test
    void getMenuItemById_NotFound() {
        // Given
        Long id = 99L;
        when(menuItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.getMenuItemById(id)
        );
        assertEquals("Menu item not found", exception.getMessage());
    }

    @Test
    void updateMenuItem_Success() {
        // Given
        Long id = 10L;
        MenuItemRequest request = MenuItemRequest.builder()
            .name("Pisco Sour Premium")
            .price(new BigDecimal("7000"))
            .categoryId(2L)
            .status(MenuItem.ItemStatus.AVAILABLE)
            .build();

        Category oldCategory = Category.builder()
            .id(1L)
            .name("Bebidas")
            .build();
        Category newCategory = Category.builder().id(2L).name("Tragos").build();
        MenuItem existingItem = MenuItem.builder()
            .id(id)
            .name("Pisco Sour")
            .price(new BigDecimal("5000"))
            .category(oldCategory)
            .status(MenuItem.ItemStatus.AVAILABLE)
            .build();

        MenuItem updatedItem = MenuItem.builder()
            .id(id)
            .name("Pisco Sour Premium")
            .price(new BigDecimal("7000"))
            .category(newCategory)
            .status(MenuItem.ItemStatus.AVAILABLE)
            .build();

        when(menuItemRepository.findById(id)).thenReturn(
            Optional.of(existingItem)
        );
        when(categoryRepository.findById(2L)).thenReturn(
            Optional.of(newCategory)
        );
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(
            updatedItem
        );

        // When
        MenuItemResponse response = service.updateMenuItem(id, request);

        // Then
        assertNotNull(response);
        assertEquals("Pisco Sour Premium", response.getName());
        assertEquals(new BigDecimal("7000"), response.getPrice());
        assertEquals(2L, response.getCategoryId());
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    void updateMenuItem_MenuItemNotFound() {
        // Given
        Long id = 99L;
        MenuItemRequest request = MenuItemRequest.builder()
            .categoryId(1L)
            .build();
        when(menuItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateMenuItem(id, request)
        );
        assertEquals("Menu item not found", exception.getMessage());
    }

    @Test
    void updateMenuItem_CategoryNotFound() {
        // Given
        Long id = 10L;
        MenuItemRequest request = MenuItemRequest.builder()
            .categoryId(99L)
            .build();
        Category category = Category.builder().id(1L).build();
        MenuItem existingItem = MenuItem.builder()
            .id(id)
            .category(category)
            .build();

        when(menuItemRepository.findById(id)).thenReturn(
            Optional.of(existingItem)
        );
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateMenuItem(id, request)
        );
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void deleteMenuItem_Success() {
        // Given
        Long id = 10L;
        when(menuItemRepository.existsById(id)).thenReturn(true);
        doNothing().when(menuItemRepository).deleteById(id);

        // When
        assertDoesNotThrow(() -> service.deleteMenuItem(id));

        // Then
        verify(menuItemRepository).deleteById(id);
    }

    @Test
    void deleteMenuItem_NotFound() {
        // Given
        Long id = 99L;
        when(menuItemRepository.existsById(id)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.deleteMenuItem(id)
        );
        assertEquals("Menu item not found", exception.getMessage());
        verify(menuItemRepository, never()).deleteById(id);
    }
}

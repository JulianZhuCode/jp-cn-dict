package io.github.jpcndict.config;

import io.github.springwhale.rbac.constant.RbacConstants;
import io.github.springwhale.rbac.entity.MenuEntity;
import io.github.springwhale.rbac.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes Dict management menus on startup.
 * <p>
 * Menu hierarchy:
 * <pre>
 * Dict (directory)
 *   ├── Word Management (menu)
 *   │   ├── Word Create (button)
 *   │   ├── Word Update (button)
 *   │   └── Word Delete (button)
 *   ├── Grammar Management (menu)
 *   │   ├── Grammar Create (button)
 *   │   ├── Grammar Update (button)
 *   │   └── Grammar Delete (button)
 *   └── Example Management (menu)
 *       ├── Example Create (button)
 *       ├── Example Update (button)
 *       └── Example Delete (button)
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DictInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        initDictMenus();
    }

    // ==================== Dict Menus ====================

    private void initDictMenus() {
        // Skip if menus already exist
        if (menuRepository.findByCode("dict").isPresent()) {
            log.info("Dict menus already initialized, skipping");
            return;
        }

        log.info("Initializing Dict menus...");

        // Dict directory
        MenuEntity dictDir = createMenu(null, "dict", "Dict", RbacConstants.MENU_TYPE_DIRECTORY,
                null, null, null, "book", 20);

        // Three management menus with button permissions
        createMenuWithButtons(dictDir, "dict:word", "Word Management",
                "/admin/dict/words", 1);
        createMenuWithButtons(dictDir, "dict:grammar", "Grammar Management",
                "/admin/dict/grammars", 2);
        createMenuWithButtons(dictDir, "dict:example", "Example Management",
                "/admin/dict/examples", 3);

        log.info("Dict menus initialized successfully");
    }

    private MenuEntity createMenu(Integer parentId, String code, String name, int type,
                                   String path, String component, String permission,
                                   String icon, int sort) {
        MenuEntity menu = new MenuEntity();
        menu.setParentId(parentId);
        menu.setCode(code);
        menu.setName(name);
        menu.setType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setVisible(1);
        menu.setStatus(1);
        return menuRepository.save(menu);
    }

    /**
     * Creates a menu item and its standard CRUD button permissions.
     */
    private void createMenuWithButtons(MenuEntity parent, String baseCode, String name,
                                        String path, int sort) {
        // Main menu
        MenuEntity menu = createMenu(parent.getId(), baseCode, name, RbacConstants.MENU_TYPE_MENU,
                path, null, null, "file-text", sort);

        // Standard CRUD button permissions
        createMenu(menu.getId(), baseCode + ":create", name + " Create", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":create", null, 1);
        createMenu(menu.getId(), baseCode + ":update", name + " Update", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":update", null, 2);
        createMenu(menu.getId(), baseCode + ":delete", name + " Delete", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":delete", null, 3);
    }

}

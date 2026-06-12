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
 *   ├── Dashboard (menu)
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
        // Get or create dict directory
        MenuEntity dictDir = menuRepository.findByCode("dict")
                .orElseGet(() -> {
                    log.info("Creating Dict directory menu...");
                    return createMenu(null, "dict", "Dict", RbacConstants.MENU_TYPE_DIRECTORY,
                            null, null, null, "book", 20);
                });

        // Ensure each sub-menu exists (idempotent)
        ensureMenuExists(dictDir, "dict:dashboard", "Dashboard",
                "/admin/dict", 0);

        ensureMenuWithButtonsExists(dictDir, "dict:word", "Word Management",
                "/admin/dict/words", 1);

        ensureMenuWithButtonsExists(dictDir, "dict:grammar", "Grammar Management",
                "/admin/dict/grammars", 2);

        ensureMenuWithButtonsExists(dictDir, "dict:example", "Example Management",
                "/admin/dict/examples", 3);

        log.info("Dict menus initialized successfully");
    }

    private void ensureMenuExists(MenuEntity parent, String code, String name,
                                   String path, int sort) {
        if (menuRepository.findByCode(code).isEmpty()) {
            createMenu(parent.getId(), code, name, RbacConstants.MENU_TYPE_MENU,
                    path, null, null, "file-text", sort);
        }
    }

    private void ensureMenuWithButtonsExists(MenuEntity parent, String baseCode, String name,
                                              String path, int sort) {
        MenuEntity menu = menuRepository.findByCode(baseCode)
                .orElseGet(() -> createMenu(parent.getId(), baseCode, name, RbacConstants.MENU_TYPE_MENU,
                        path, null, null, "file-text", sort));

        // Ensure button permissions
        ensureButtonExists(menu, baseCode + ":create", name + " Create", 1);
        ensureButtonExists(menu, baseCode + ":update", name + " Update", 2);
        ensureButtonExists(menu, baseCode + ":delete", name + " Delete", 3);
    }

    private void ensureButtonExists(MenuEntity parent, String code, String name, int sort) {
        if (menuRepository.findByCode(code).isEmpty()) {
            createMenu(parent.getId(), code, name, RbacConstants.MENU_TYPE_BUTTON,
                    null, null, code, null, sort);
        }
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

}

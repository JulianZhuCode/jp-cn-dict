package io.github.jpcndict.menu;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers Dict module menu items in the admin console sidebar.
 */
@Component
public class DictMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
                MenuItem.group("dict", "Dict", "\uD83D\uDCD6", 20),
                MenuItem.leaf("dict-dashboard", "dict", "Dashboard", "/admin/dict", null, null, 0),
                MenuItem.leaf("dict-words", "dict", "Words", "/admin/dict/words", null, "dict:word", 1),
                MenuItem.leaf("dict-grammars", "dict", "Grammars", "/admin/dict/grammars", null, "dict:grammar", 2),
                MenuItem.leaf("dict-examples", "dict", "Examples", "/admin/dict/examples", null, "dict:example", 3)
        );
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

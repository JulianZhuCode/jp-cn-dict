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
                MenuItem.group("dict", "词典管理", "\uD83D\uDCD6", 20),
                MenuItem.leaf("dict-dashboard", "dict", "数据概览", "/admin/dict", null, null, 0),
                MenuItem.leaf("dict-words", "dict", "单词管理", "/admin/dict/words", null, "dict:word", 1),
                MenuItem.leaf("dict-grammars", "dict", "语法管理", "/admin/dict/grammars", null, "dict:grammar", 2),
                MenuItem.leaf("dict-examples", "dict", "例句管理", "/admin/dict/examples", null, "dict:example", 3),
                MenuItem.leaf("dict-ai-config", "dict", "AI配置", "/admin/dict/ai-config", null, "dict:ai-config", 4)
        );
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

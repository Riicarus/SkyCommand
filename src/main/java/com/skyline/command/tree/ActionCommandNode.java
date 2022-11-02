package com.skyline.command.tree;

import com.skyline.command.executor.CommandExecutor;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [FEATURE INFO]<br/>
 * 指令 action 节点
 *
 * @author Skyline
 * @create 2022-10-15 16:38
 * @since 1.0.0
 */
public class ActionCommandNode extends CommandNode {

    private final boolean isSubAction;

    public ActionCommandNode(final CommandExecutor commandExecutor,
                             final String name,
                             final boolean isSubAction) {
        super(name, null, null,
                new ConcurrentHashMap<>(), new ConcurrentHashMap<>(),
                null, commandExecutor);

        this.isSubAction = isSubAction;
    }

    @Override
    public final ConcurrentHashMap<String, ActionCommandNode> getSubActions() {
        if (!isSubAction()) {
            return doGetSubActions();
        }

        return null;
    }

    public boolean isSubAction() {
        return isSubAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionCommandNode that = (ActionCommandNode) o;
        return isSubAction == that.isSubAction && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isSubAction);
    }
}

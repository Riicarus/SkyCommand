package com.skyline.command.manage;

import com.skyline.command.exception.CommandNotFoundException;
import com.skyline.command.executor.CommandExecutor;
import com.skyline.command.tree.CommandNode;
import com.skyline.command.tree.OptionCommandNode;
import com.skyline.command.tree.RootCommandNode;

import java.util.ArrayList;
import java.util.List;

/**
 * [FEATURE INFO]<br/>
 * 指令分发器
 *
 * @author Skyline
 * @create 2022-10-15 23:27
 * @since 1.0.0
 */
public class CommandDispatcher {

    private static final String COMMAND_PART_SPLIT_STRING = " ";
    private static final String SHORT_OPTION_PREFIX_STRING = "-";
    private static final String LONG_OPTION_PREFIX_STRING = "--";

    private final CommandRegister commandRegister;

    public CommandDispatcher() {
        this.commandRegister = new CommandRegister();
    }

    public CommandDispatcher(CommandRegister commandRegister) {
        this.commandRegister = commandRegister;
    }

    /**
     * 分发并执行指令
     *
     * @param commandStr 指令字符串
     */
    public void dispatch(final String commandStr) {
        RootCommandNode rootCommandNode = commandRegister.getRootCommandNode();

        String[] parsedCommandParts = parseCommand(commandStr);

        if (parsedCommandParts.length <= 1) {
            throw new CommandNotFoundException("Command: " + commandStr + " not found.", null);
        }

        List<Object> args = new ArrayList<>();
        CommandNode commandNode = findLast(rootCommandNode, parsedCommandParts, 0, false, args);

        // args 依照 指令从左到右的顺序传入
        CommandExecutor executor = commandNode.getCommandExecutor();
        if (executor == null) {
            throw new CommandNotFoundException("No executor bound with this command.", null);
        }
        executor.execute(args.toArray());
    }

    /**
     * 将指令字符串解析为和节点对应的指令部分字符串数组, 将合并的短指令分解为单个的短指令
     *
     * @param commandStr 指令字符串
     * @return 指令部分字符串数组
     */
    private String[] parseCommand(final String commandStr) {
        String[] commandRawParts = commandStr.split(COMMAND_PART_SPLIT_STRING);

        List<String> commandPartList = new ArrayList<>();

        for (String commandRawPart : commandRawParts) {
            if (!commandRawPart.startsWith(LONG_OPTION_PREFIX_STRING) && commandRawPart.startsWith(SHORT_OPTION_PREFIX_STRING)) {
                // 是 short-option 的情况
                String tmp = commandRawPart.substring(SHORT_OPTION_PREFIX_STRING.length());
                if (tmp.length() > 1) {
                    char[] chars = tmp.toCharArray();
                    for (char c : chars) {
                        commandPartList.add(SHORT_OPTION_PREFIX_STRING + c);
                    }
                } else {
                    commandPartList.add(commandRawPart);
                }
            } else {
                // 其他情况, 直接加入列表
                commandPartList.add(commandRawPart);
            }
        }

        for (int i = 0; i < commandPartList.size(); i++) {
            String commandPart = commandPartList.get(i);
            // 处理 short-option
            if (!commandPart.startsWith(LONG_OPTION_PREFIX_STRING) && commandPart.startsWith(SHORT_OPTION_PREFIX_STRING)) {
                String tmp = commandPart.substring(SHORT_OPTION_PREFIX_STRING.length());
                if (tmp.length() > 1) {
                    commandPartList.add(i + 1, SHORT_OPTION_PREFIX_STRING + tmp);
                }
            }
        }

        return commandPartList.toArray(new String[]{});
    }

    /**
     * 找到在传入指令字符串的环境中, 该指令节点的下一个指令节点
     *
     * @param commandNode 当前指令节点
     * @param commandParts 指令字符串按分隔符 " " 解析后的数组
     * @param index 当前指令字符串数组的 index, 表征下一个要解析的节点在 index 位置
     * @param isOptionOrArg 当前节点是否为 option 或 argument 节点
     * @param args 参数列表, 解析完成后, 应该包含指令中所有传入的参数
     * @return 指令字符串对应的最后一个节点
     */
    private CommandNode findLast(CommandNode commandNode, String[] commandParts, int index, boolean isOptionOrArg, List<Object> args) {
        CommandNode node = null;

        String commandPart = commandParts[index];

        if (isOptionOrArg && !commandPart.startsWith(SHORT_OPTION_PREFIX_STRING)) {
            // argument
            args.add(commandPart);

            // 这里要求: 参数节点的名称必须和 对应 option 节点的 long-option 名称相同
            node = commandNode.getChildren().get(commandNode.getName());
        } else if (isOptionOrArg) {
            // option
            if (commandPart.startsWith(LONG_OPTION_PREFIX_STRING)) {
                // long option
                commandPart = commandPart.substring(LONG_OPTION_PREFIX_STRING.length());

                node = commandNode.getChildren().get(commandPart);
            } else if (commandPart.startsWith(SHORT_OPTION_PREFIX_STRING)) {
                // short option
                commandPart = commandPart.substring(SHORT_OPTION_PREFIX_STRING.length());

                for (CommandNode child : commandNode.getChildren().values()) {
                    if (((OptionCommandNode) child).getAlias().equals(commandPart)) {
                        node = child;
                        break;
                    }
                }
            }
        } else {
            // 这里还没有到 option 或 argument 部分
            if (commandPart.startsWith(LONG_OPTION_PREFIX_STRING)) {
                commandPart = commandPart.substring(LONG_OPTION_PREFIX_STRING.length());
                isOptionOrArg = true;

                node = commandNode.getChildren().get(commandPart);
            } else if (commandPart.startsWith(SHORT_OPTION_PREFIX_STRING)) {
                commandPart = commandPart.substring(SHORT_OPTION_PREFIX_STRING.length());
                isOptionOrArg = true;

                for (CommandNode child : commandNode.getChildren().values()) {
                    if (((OptionCommandNode) child).getAlias().equals(commandPart)) {
                        node = child;
                        break;
                    }
                }
            } else {
                node = commandNode.getChildren().get(commandPart);
            }
        }

        if (node == null) {
            throw new CommandNotFoundException("No command definition found for this command.", null);
        }

        if (index == commandParts.length - 1) {
            return node;
        }

        index++;

        return findLast(node, commandParts, index, isOptionOrArg, args);
    }


    public CommandRegister getCommandRegister() {
        return commandRegister;
    }
}

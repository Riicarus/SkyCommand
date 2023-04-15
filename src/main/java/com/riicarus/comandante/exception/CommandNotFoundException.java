package com.riicarus.comandante.exception;

/**
 * [FEATURE INFO]<br/>
 * 无法找到对应指令异常, 用于抛出指令在分发过程中出现的运行时异常
 *
 * @author Riicarus
 * @create 2022-10-16 18:57
 * @since 1.0
 */
public class CommandNotFoundException extends CommandSyntaxException {

    public CommandNotFoundException(String message) {
        super(message);
    }

}

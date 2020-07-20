package io.neow3j.devpack.framework.annotations;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.framework.annotations.Instruction.Instructions;
import java.lang.annotation.Repeatable;

@Repeatable(Instructions.class)
public @interface Instruction {

    OpCode opcode();

    // TODO: Many opcodes don't require an operand. It needs to be disregarded on such opcodes.
    byte operand() default 0;

    @interface Instructions {
        Instruction[] value();
    }
}
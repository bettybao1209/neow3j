package io.neow3j.compiler;

import static java.lang.String.format;

import io.neow3j.constants.OpCode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NeoModule {

    // Holds this module's methods, mapping from method ID to {@link NeoMethod}; Used by the
    // compiler to quickly search for a method.
    private final Map<String, NeoMethod> methods = new HashMap<>();

    // Holds the same references to the methods as {@link NeoModule#methods} but in the order they
    // have been added to this module.
    private final List<NeoMethod> sortedMethods = new ArrayList<>();

    // Holds this module's events. The keys are the variable names used when defining the events
    // in the smart contract class.
    private final Map<String, NeoEvent> events = new HashMap<>();


    public List<NeoEvent> getEvents() {
        return new ArrayList<>(events.values());
    }

    /**
     * Gets this module's methods in the order they were added.
     *
     * @return the methods.
     */
    public List<NeoMethod> getSortedMethods() {
        return sortedMethods;
    }

    public void addMethod(NeoMethod method) {
        methods.put(method.getId(), method);
        sortedMethods.add(method);
    }

    public void addEvent(NeoEvent event) {
        if (events.containsKey(event.getDisplayName())) {
            throw new CompilerException(format("Two events with the name '%s' are defined. Make "
                    + "sure that every event has a different name.", event.getDisplayName()));
        }
        events.put(event.getDisplayName(), event);
    }

    void finalizeModule() {
        int startAddress = 0;
        for (NeoMethod method : this.sortedMethods) {
            method.finalizeMethod();
            method.setStartAddress(startAddress);
            // At this point, the `nextAddress` should be set to one byte after the last
            // instruction byte of a method. So we can simply add this number to the current
            // start address and get the start address of the next method.
            startAddress += method.getLastAddress();
        }
        for (NeoMethod method : sortedMethods) {
            for (Entry<Integer, NeoInstruction> entry : method.getInstructions().entrySet()) {
                NeoInstruction insn = entry.getValue();
                // Currently we're only using OpCode.CALL_L. Using CALL instead of CALL_L might
                // lead to some savings in script size but will also require shifting
                // addresses of all following instructions.
                if (insn.getOpcode().equals(OpCode.CALL_L)) {
                    if (!(insn.getExtra() instanceof NeoMethod)) {
                        throw new CompilerException(format("Instruction with %s opcode is "
                                + "missing the reference to the called method. The jump address "
                                + "cannot be resolved.", OpCode.CALL_L.name()));
                    }
                    NeoMethod calledMethod = (NeoMethod) insn.getExtra();
                    int offset = calledMethod.getStartAddress()
                            - (method.getStartAddress() + entry.getKey());
                    insn.setOperand(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(offset).array());
                }
            }
        }
    }

    int byteSize() {
        return sortedMethods.stream().map(NeoMethod::byteSize).reduce(Integer::sum).get();
    }

    /**
     * Concatenates all of this module's methods together into one script. Should only be called
     * after {@link NeoModule#finalizeModule()} becuase otherwise the {@link
     * NeoModule#sortedMethods} is not yet initialized.
     */
    byte[] toByteArray() {
        ByteBuffer b = ByteBuffer.allocate(byteSize());
        sortedMethods.forEach(m -> b.put(m.toByteArray()));
        return b.array();
    }

    public boolean hasMethod(String calledMethodId) {
        return methods.containsKey(calledMethodId);
    }

    public NeoMethod getMethod(String calledMethodId) {
        return methods.get(calledMethodId);
    }

}

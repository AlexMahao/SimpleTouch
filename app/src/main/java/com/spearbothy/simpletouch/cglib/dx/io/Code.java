/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spearbothy.simpletouch.cglib.dx.io;

public final class Code {
    private final int registersSize;
    private final int insSize;
    private final int outsSize;
    private final int debugInfoOffset;
    private final short[] instructions;
    private final Try[] tries;
    private final CatchHandler[] catchHandlers;

    public Code(int registersSize, int insSize, int outsSize, int debugInfoOffset,
            short[] instructions, Try[] tries, CatchHandler[] catchHandlers) {
        this.registersSize = registersSize;
        this.insSize = insSize;
        this.outsSize = outsSize;
        this.debugInfoOffset = debugInfoOffset;
        this.instructions = instructions;
        this.tries = tries;
        this.catchHandlers = catchHandlers;
    }

    public int getRegistersSize() {
        return registersSize;
    }

    public int getInsSize() {
        return insSize;
    }

    public int getOutsSize() {
        return outsSize;
    }

    public int getDebugInfoOffset() {
        return debugInfoOffset;
    }

    public short[] getInstructions() {
        return instructions;
    }

    public Try[] getTries() {
        return tries;
    }

    public CatchHandler[] getCatchHandlers() {
        return catchHandlers;
    }

    public static class Try {
        final int startAddress;
        final int instructionCount;
        final int handlerOffset;

        Try(int startAddress, int instructionCount, int handlerOffset) {
            this.startAddress = startAddress;
            this.instructionCount = instructionCount;
            this.handlerOffset = handlerOffset;
        }

        public int getStartAddress() {
            return startAddress;
        }

        public int getInstructionCount() {
            return instructionCount;
        }

        public int getHandlerOffset() {
            return handlerOffset;
        }
    }

    public static class CatchHandler {
        final int[] typeIndexes;
        final int[] addresses;
        final int catchAllAddress;

        public CatchHandler(int[] typeIndexes, int[] addresses, int catchAllAddress) {
            this.typeIndexes = typeIndexes;
            this.addresses = addresses;
            this.catchAllAddress = catchAllAddress;
        }

        public int[] getTypeIndexes() {
            return typeIndexes;
        }

        public int[] getAddresses() {
            return addresses;
        }

        public int getCatchAllAddress() {
            return catchAllAddress;
        }
    }
}

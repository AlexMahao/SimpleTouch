/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.spearbothy.simpletouch.cglib.dx.dex.code.form;

import com.spearbothy.simpletouch.cglib.dx.dex.code.CstInsn;
import com.spearbothy.simpletouch.cglib.dx.dex.code.DalvInsn;
import com.spearbothy.simpletouch.cglib.dx.dex.code.InsnFormat;
import com.spearbothy.simpletouch.cglib.dx.rop.code.RegisterSpecList;
import com.spearbothy.simpletouch.cglib.dx.rop.cst.Constant;
import com.spearbothy.simpletouch.cglib.dx.rop.cst.CstMethodRef;
import com.spearbothy.simpletouch.cglib.dx.rop.cst.CstType;
import com.spearbothy.simpletouch.cglib.dx.util.AnnotatedOutput;

/**
 * Instruction format {@code 5rc}. See the instruction format spec
 * for details.
 */
public final class Form5rc extends InsnFormat {
    /** {@code non-null;} unique instance of this class */
    public static final InsnFormat THE_ONE = new Form5rc();

    /**
     * Constructs an instance. This class is not publicly
     * instantiable. Use {@link #THE_ONE}.
     */
    private Form5rc() {
        // This space intentionally left blank.
    }

    /** {@inheritDoc} */
    @Override
    public String insnArgString(DalvInsn insn) {
        return regRangeString(insn.getRegisters()) + ", " +
            cstString(insn);
    }

    /** {@inheritDoc} */
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        if (noteIndices) {
            return cstComment(insn);
        } else {
            return "";
        }
    }

    /** {@inheritDoc} */
    @Override
    public int codeSize() {
        return 5;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (! ALLOW_EXTENDED_OPCODES) {
            return false;
        }

        if (!(insn instanceof CstInsn)) {
            return false;
        }

        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();

        if (!((cst instanceof CstMethodRef) ||
              (cst instanceof CstType))) {
            return false;
        }

        RegisterSpecList regs = ci.getRegisters();
        int sz = regs.size();

        return (regs.size() == 0) ||
            (isRegListSequential(regs) &&
             unsignedFitsInShort(regs.get(0).getReg()) &&
             unsignedFitsInShort(regs.getWordCount()));
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int cpi = ((CstInsn) insn).getIndex();
        int firstReg = (regs.size() == 0) ? 0 : regs.get(0).getReg();
        int count = regs.getWordCount();

        write(out, opcodeUnit(insn), cpi, (short) count, (short) firstReg);
    }
}

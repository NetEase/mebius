 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.netease.mebius.client.utils;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.TableSwitchInsnNode;

/**
 * 复杂度计算工具类
 */
public class ComplexityUtils {

    /**
     * 圈复杂度计算
     *
     * @param methodNode
     * @return
     */
    public static int calculate(MethodNode methodNode) {
        int complexity = 1;
        InsnList instructions = methodNode.instructions;
        for (int i = 0; i < instructions.size(); i++) {
            AbstractInsnNode instruction = instructions.get(i);
            if (instruction instanceof JumpInsnNode) {
                int opcode = instruction.getOpcode();
                if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
                    complexity++;
                } else if (opcode != Opcodes.RET) {
                    complexity += 2;
                }
            } else if (instruction instanceof TableSwitchInsnNode) {
                complexity += ((TableSwitchInsnNode) instruction).labels.size() + 1;
            } else if (instruction instanceof LookupSwitchInsnNode) {
                complexity += ((LookupSwitchInsnNode) instruction).labels.size() + 1;
            }
        }
        return complexity;
    }
}
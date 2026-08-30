/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockStatePosPair {
    public BlockState blockState;
    public BlockPos pos;

    public BlockStatePosPair(BlockState b, BlockPos p) {
        blockState = b;
        pos = p;
    }

}

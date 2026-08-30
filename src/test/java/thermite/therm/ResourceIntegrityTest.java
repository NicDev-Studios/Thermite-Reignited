/*
 * Copyright (c) 2023 sparkierkan7
 * Modifications Copyright (c) 2026 NicDev-Studios
 * SPDX-License-Identifier: MIT
 */

package thermite.therm;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceIntegrityTest {
    private static final List<String> REQUIRED_RESOURCES = List.of(
            "fabric.mod.json",
            "pack.mcmeta",
            "therm.mixins.json",
            "data/therm/recipes/thermometer.json",
            "data/therm/recipes/leather_armor_wool.json",
            "data/therm/recipes/ice_box_item.json",
            "assets/therm/models/item/thermometer.json",
            "assets/therm/blockstates/fireplace.json"
    );

    @Test
    void requiredModResourcesArePackaged() {
        for (String resource : REQUIRED_RESOURCES) {
            assertNotNull(getClass().getClassLoader().getResource(resource), resource + " is missing");
        }
    }

    @Test
    void generatedMetadataContainsNoUnexpandedPlaceholders() throws IOException {
        String modMetadata = readResource("fabric.mod.json");
        String packMetadata = readResource("pack.mcmeta");

        assertTrue(modMetadata.contains("\"id\": \"therm\""));
        assertTrue(modMetadata.contains("\"version\": \"1.0.0-alpha.1+mc"));
        assertFalse(modMetadata.contains("${"));
        assertFalse(packMetadata.contains("${"));
        assertTrue(packMetadata.contains("supported_formats"));
    }

    private static String readResource(String resource) throws IOException {
        try (InputStream stream = ResourceIntegrityTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream, resource + " is missing");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

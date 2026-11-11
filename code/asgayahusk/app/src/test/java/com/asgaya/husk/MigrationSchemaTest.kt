package com.asgaya.husk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * No silent schema holes (`ui-rethink/16` P0-6; TightDS `15 §3`).
 *
 * Every Room schema version must be exported to `app/schemas/` (KSP regenerates them on every
 * build), and every gap between the baseline and the current version must have a `Migration` —
 * otherwise a bump would wipe (or crash on) a device's database.
 */
class MigrationSchemaTest {

    private fun schemaDir(): File {
        val candidates = listOf(
            File("schemas/com.asgaya.husk.AppDatabase"),
            File("app/schemas/com.asgaya.husk.AppDatabase")
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("Room schema dir not found (checked: ${candidates.map { it.absolutePath }})")
    }

    private fun exportedVersions(): List<Int> =
        schemaDir().listFiles { f -> f.name.endsWith(".json") }
            ?.map { it.name.removeSuffix(".json").toInt() }
            ?.sorted()
            ?: emptyList()

    @Test
    fun everyVersionIsExportedWithoutHoles() {
        val versions = exportedVersions()
        assertTrue("No exported schemas found in ${schemaDir().absolutePath}", versions.isNotEmpty())
        val expected = (versions.first()..versions.last()).toList()
        assertEquals("Silent hole in the exported schemas: $versions", expected, versions)
    }

    @Test
    fun migrationRegistryCoversEveryGap() {
        val versions = exportedVersions()
        val reachedByMigration = AppDatabase.MIGRATIONS.map { it.endVersion }.toSet()
        // Every version after the baseline must be the end of a Migration.
        val missing = versions.drop(1).filter { it !in reachedByMigration }
        assertTrue("Missing Migration(s) ending at: $missing (exported: $versions)", missing.isEmpty())

        // The migration chain itself must be contiguous (no holes between migrations).
        val ends = AppDatabase.MIGRATIONS.map { it.endVersion }.sorted()
        assertTrue("Migration chain has holes: $ends", ends == (ends.first()..ends.last()).toList())
    }

    @Test
    fun migrationsSpanTheBaselineToTheLatestSchema() {
        val versions = exportedVersions()
        assertEquals(
            "The first Migration must start at the baseline schema",
            versions.first(), AppDatabase.MIGRATIONS.minOf { it.startVersion }
        )
        assertEquals(
            "The last Migration must land on the newest exported schema",
            versions.last(), AppDatabase.MIGRATIONS.maxOf { it.endVersion }
        )
    }
}

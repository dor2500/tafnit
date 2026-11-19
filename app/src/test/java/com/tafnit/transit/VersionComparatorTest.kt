package com.tafnit.transit

import com.tafnit.transit.data.remote.VersionComparator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun newerVersionsDetectedCorrectly() {
        assertTrue(VersionComparator.isNewer("v1.0.1", "1.0.0"))
        assertTrue(VersionComparator.isNewer("v1.1.0", "1.0.9"))
        assertTrue(VersionComparator.isNewer("2.0.0", "1.9.9"))
        assertFalse(VersionComparator.isNewer("v1.0.0", "1.0.0"))
        assertFalse(VersionComparator.isNewer("v0.9.5", "1.0.0"))
    }
}

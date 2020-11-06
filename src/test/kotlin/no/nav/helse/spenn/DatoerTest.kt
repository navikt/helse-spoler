package no.nav.helse.spenn

import no.nav.helse.spenn.januar
import no.nav.helse.spenn.mandag
import no.nav.helse.spenn.mars
import no.nav.helse.spenn.søndag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DatoerTest {

    @Test
    internal fun accuracy() {
        assertEquals(LocalDate.of(2018, 1, 1), 1.mandag)
        assertEquals(1.januar, 1.mandag)
        assertEquals(4.mars, 9.søndag)
    }
}

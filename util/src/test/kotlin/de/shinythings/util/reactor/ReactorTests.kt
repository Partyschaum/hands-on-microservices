package de.shinythings.util.reactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toFlux

class ReactorTests {

    @Test
    fun testFlux() {
        val list = mutableListOf<Int>()

        listOf(1, 2, 3, 4).toFlux()
                .filter { n: Int -> n % 2 == 0 }
                .map { n: Int -> n * 2 }
                .log()
                .subscribe { n: Int -> list.add(n) }

        assertThat(list).containsExactly(4, 8)
    }

    @Test
    fun testFluxBlocking() {
        val list = listOf(1, 2, 3, 4)
                .toFlux()
                .filter { n: Int -> n % 2 == 0 }
                .map { n: Int -> n * 2 }
                .log()
                .collectList().block()!!.toList()

        assertThat(list).containsExactly(4, 8)
    }
}

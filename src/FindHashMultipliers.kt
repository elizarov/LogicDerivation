import kotlin.random.Random

fun main() {
    val rnd = Random(1)
    repeat(3) { i ->
        var x: Int
        do {
            x = rnd.nextInt(1, Int.MAX_VALUE)
        } while (!x.toBigInteger().isProbablePrime(100))
        println("private const val hashPrime$i = $x")
    }
}
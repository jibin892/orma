package org.orma.project_90.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun OrmaQrCode(
    value: String,
    modifier: Modifier = Modifier,
    moduleColor: Color = OrmaColors.IconPrimary,
    backgroundColor: Color = OrmaColors.ScreenBackground,
) {
    val modules = remember(value) { OrmaQrMatrix.create(value) }
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = OrmaShapes.SmallCard,
        color = backgroundColor,
        contentColor = moduleColor,
        border = BorderStroke(0.6.dp, OrmaColors.Hairline),
        tonalElevation = 0.dp,
        shadowElevation = OrmaElevation.None,
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val count = modules.size
                val moduleSize = min(size.width, size.height) / count
                val left = (size.width - moduleSize * count) / 2f
                val top = (size.height - moduleSize * count) / 2f
                modules.forEachIndexed { y, row ->
                    row.forEachIndexed { x, dark ->
                        if (dark) {
                            drawRect(
                                color = moduleColor,
                                topLeft = Offset(left + x * moduleSize, top + y * moduleSize),
                                size = Size(moduleSize + 0.15f, moduleSize + 0.15f),
                            )
                        }
                    }
                }
            }
        }
    }
}

fun ormaQrCodeSvg(
    value: String,
    moduleColor: String = "#143D3D",
    backgroundColor: String = "#FFFFFF",
): String {
    val modules = runCatching { OrmaQrMatrix.create(value) }.getOrNull() ?: return ""
    val quietZone = 4
    val size = modules.size + quietZone * 2
    val rects = buildString {
        modules.forEachIndexed { y, row ->
            row.forEachIndexed { x, dark ->
                if (dark) {
                    append("""<rect x="${x + quietZone}" y="${y + quietZone}" width="1" height="1"/>""")
                }
            }
        }
    }
    return """
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $size $size" width="116" height="116" shape-rendering="crispEdges" aria-label="QR code">
          <rect width="$size" height="$size" fill="$backgroundColor"/>
          <g fill="$moduleColor">$rects</g>
        </svg>
    """.trimIndent()
}

private object OrmaQrMatrix {
    private const val Version = 5
    private const val Size = Version * 4 + 17
    private const val DataCodewords = 108
    private const val ErrorCorrectionCodewords = 26

    fun create(value: String): List<List<Boolean>> {
        val data = encodeData(value)
        val errorCorrection = reedSolomonRemainder(data, reedSolomonDivisor(ErrorCorrectionCodewords))
        val codewords = data + errorCorrection
        val modules = Array(Size) { BooleanArray(Size) }
        val function = Array(Size) { BooleanArray(Size) }
        drawFunctionPatterns(modules, function)
        drawCodewords(modules, function, codewords)
        drawFormatBits(modules, function)
        return modules.map { row -> row.toList() }
    }

    private fun encodeData(value: String): IntArray {
        val bytes = value.encodeToByteArray()
        require(bytes.size <= 106) { "QR value is too long for ORMA shop link." }
        val bits = mutableListOf<Int>()
        appendBits(bits, value = 0x4, bitCount = 4)
        appendBits(bits, value = bytes.size, bitCount = 8)
        bytes.forEach { appendBits(bits, value = it.toInt() and 0xff, bitCount = 8) }
        repeat(minOf(4, DataCodewords * 8 - bits.size)) { bits.add(0) }
        while (bits.size % 8 != 0) bits.add(0)
        val result = bits.chunked(8).map { chunk ->
            chunk.fold(0) { acc, bit -> (acc shl 1) or bit }
        }.toMutableList()
        var pad = 0
        while (result.size < DataCodewords) {
            result.add(if (pad % 2 == 0) 0xEC else 0x11)
            pad += 1
        }
        return result.toIntArray()
    }

    private fun appendBits(bits: MutableList<Int>, value: Int, bitCount: Int) {
        for (i in bitCount - 1 downTo 0) {
            bits.add((value ushr i) and 1)
        }
    }

    private fun drawFunctionPatterns(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
    ) {
        drawFinder(modules, function, 0, 0)
        drawFinder(modules, function, Size - 7, 0)
        drawFinder(modules, function, 0, Size - 7)
        drawAlignment(modules, function, 30, 30)
        for (i in 8 until Size - 8) {
            setFunction(modules, function, 6, i, i % 2 == 0)
            setFunction(modules, function, i, 6, i % 2 == 0)
        }
        reserveFormat(function)
        setFunction(modules, function, 8, Version * 4 + 9, true)
    }

    private fun drawFinder(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
        left: Int,
        top: Int,
    ) {
        for (dy in -1..7) {
            for (dx in -1..7) {
                val x = left + dx
                val y = top + dy
                if (x !in 0 until Size || y !in 0 until Size) continue
                val dark = dx in 0..6 && dy in 0..6 &&
                    (dx == 0 || dx == 6 || dy == 0 || dy == 6 || (dx in 2..4 && dy in 2..4))
                setFunction(modules, function, x, y, dark)
            }
        }
    }

    private fun drawAlignment(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
        centerX: Int,
        centerY: Int,
    ) {
        for (dy in -2..2) {
            for (dx in -2..2) {
                val dark = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) != 1
                setFunction(modules, function, centerX + dx, centerY + dy, dark)
            }
        }
    }

    private fun reserveFormat(function: Array<BooleanArray>) {
        for (i in 0..8) {
            if (i != 6) {
                function[8][i] = true
                function[i][8] = true
            }
        }
        for (i in 0..7) function[8][Size - 1 - i] = true
        for (i in 0..6) function[Size - 1 - i][8] = true
    }

    private fun setFunction(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
        x: Int,
        y: Int,
        dark: Boolean,
    ) {
        modules[y][x] = dark
        function[y][x] = true
    }

    private fun drawCodewords(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
        codewords: IntArray,
    ) {
        val bits = codewords.flatMap { codeword ->
            (7 downTo 0).map { bit -> ((codeword ushr bit) and 1) == 1 }
        }
        var bitIndex = 0
        var upward = true
        var right = Size - 1
        while (right >= 1) {
            if (right == 6) right -= 1
            for (vertical in 0 until Size) {
                val y = if (upward) Size - 1 - vertical else vertical
                for (j in 0..1) {
                    val x = right - j
                    if (!function[y][x]) {
                        val bit = bitIndex < bits.size && bits[bitIndex]
                        modules[y][x] = bit xor mask(x, y)
                        bitIndex += 1
                    }
                }
            }
            upward = !upward
            right -= 2
        }
    }

    private fun mask(x: Int, y: Int): Boolean = (x + y) % 2 == 0

    private fun drawFormatBits(
        modules: Array<BooleanArray>,
        function: Array<BooleanArray>,
    ) {
        val bits = formatBits(mask = 0)
        for (i in 0..5) setFunction(modules, function, 8, i, bit(bits, i))
        setFunction(modules, function, 8, 7, bit(bits, 6))
        setFunction(modules, function, 8, 8, bit(bits, 7))
        setFunction(modules, function, 7, 8, bit(bits, 8))
        for (i in 9..14) setFunction(modules, function, 14 - i, 8, bit(bits, i))
        for (i in 0..7) setFunction(modules, function, Size - 1 - i, 8, bit(bits, i))
        for (i in 8..14) setFunction(modules, function, 8, Size - 15 + i, bit(bits, i))
        setFunction(modules, function, 8, Size - 8, true)
    }

    private fun formatBits(mask: Int): Int {
        val data = (1 shl 3) or mask
        var remainder = data
        repeat(10) {
            remainder = (remainder shl 1) xor if ((remainder ushr 9) != 0) 0x537 else 0
        }
        return ((data shl 10) or remainder) xor 0x5412
    }

    private fun bit(value: Int, index: Int): Boolean = ((value ushr index) and 1) != 0

    private fun reedSolomonDivisor(degree: Int): IntArray {
        val result = IntArray(degree)
        result[degree - 1] = 1
        var root = 1
        for (i in 0 until degree) {
            for (j in result.indices) {
                result[j] = gfMultiply(result[j], root)
                if (j + 1 < result.size) result[j] = result[j] xor result[j + 1]
            }
            root = gfMultiply(root, 0x02)
        }
        return result
    }

    private fun reedSolomonRemainder(data: IntArray, divisor: IntArray): IntArray {
        val result = IntArray(divisor.size)
        data.forEach { codeword ->
            val factor = codeword xor result[0]
            for (i in 0 until result.lastIndex) result[i] = result[i + 1]
            result[result.lastIndex] = 0
            divisor.forEachIndexed { index, value ->
                result[index] = result[index] xor gfMultiply(value, factor)
            }
        }
        return result
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        var z = 0
        for (i in 7 downTo 0) {
            z = (z shl 1) xor if ((z and 0x80) != 0) 0x11D else 0
            if (((y ushr i) and 1) != 0) z = z xor x
        }
        return z and 0xFF
    }
}

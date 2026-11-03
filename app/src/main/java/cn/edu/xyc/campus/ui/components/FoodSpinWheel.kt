package cn.edu.xyc.campus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.edu.xyc.campus.data.local.FoodCategory
import cn.edu.xyc.campus.ui.theme.FoodColors
import kotlin.math.abs
import kotlin.random.Random

/** 盘沿宽度 13dp、中心盘 112dp、整盘 304dp —— 见设计方案 §4.3 */
private val WHEEL_SIZE = 304.dp
private val BEZEL = 13.dp
private val HUB_SIZE = 112.dp

/**
 * 计算每格的扫过角度。
 *
 * 权重只在候选够多时生效（历史里出现过的 0.3，其余 1.0）：候选少于 10 个时扇区本来就宽，
 * 再压缩没有视觉意义，直接等分。夹在 [7°, 26°] 之间再做一次水填平，保证总和正好 360°。
 */
fun sectorSweeps(weights: List<Float>): List<Float> {
    val n = weights.size
    if (n == 0) return emptyList()
    if (n < 10) return List(n) { 360f / n }

    val total = weights.sum().coerceAtLeast(0.0001f)
    val out = MutableList(n) { weights[it] / total * 360f }
    val minDeg = 7f
    val maxDeg = 26f
    repeat(8) {
        for (i in 0 until n) out[i] = out[i].coerceIn(minDeg, maxDeg)
        val diff = 360f - out.sum()
        if (abs(diff) < 0.01f) return out
        val flexible = (0 until n).filter { out[it] > minDeg + 0.01f && out[it] < maxDeg - 0.01f }
        if (flexible.isEmpty()) return out
        val add = diff / flexible.size
        flexible.forEach { out[it] += add }
    }
    return out
}

/** 按权重抽一个下标 */
fun pickWeighted(weights: List<Float>): Int {
    val total = weights.sum().coerceAtLeast(0.0001f)
    var r = Random.nextFloat() * total
    for (i in weights.indices) {
        r -= weights[i]
        if (r <= 0f) return i
    }
    return weights.lastIndex
}

/**
 * 蓝调刻度盘。
 *
 * 三层：盘沿（28 段五组色，段间留细缝）→ 盘面（同组两档明度交替 + 刻度线）→ 中心盘 / 指针。
 * 旋转只作用在 Canvas 的 graphicsLayer 上，不触发重绘。
 */
@Composable
fun FoodSpinWheel(
    pool: List<FoodCategory>,
    sweeps: List<Float>,
    rotation: Float,
    winnerIndex: Int?,
    dark: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val skin = FoodColors.skin(dark)
    Box(modifier.size(WHEEL_SIZE)) {
        Canvas(
            Modifier
                .size(WHEEL_SIZE)
                .graphicsLayer { rotationZ = rotation },
        ) {
            val r = size.minDimension / 2f
            val bezel = BEZEL.toPx()
            val rPlate = (r - bezel).coerceAtLeast(1f)
            val c = center
            val full = Size(r * 2f, r * 2f)
            val fullTopLeft = Offset(c.x - r, c.y - r)

            // 底色先铺满，扇区之间的细缝就能透出它
            drawCircle(color = skin.gap, radius = r, center = c)

            // ① 盘沿
            var start = -90f
            pool.forEachIndexed { i, cat ->
                val sw = sweeps.getOrElse(i) { 360f / pool.size }
                drawArc(
                    color = FoodColors.groupColor(cat.group, dark),
                    startAngle = start,
                    sweepAngle = (sw - 0.7f).coerceAtLeast(0.25f),
                    useCenter = true,
                    topLeft = fullTopLeft,
                    size = full,
                )
                start += sw
            }

            // ② 盘面：同组两档交替
            val plateSize = Size(rPlate * 2f, rPlate * 2f)
            val plateTopLeft = Offset(c.x - rPlate, c.y - rPlate)
            var s2 = -90f
            pool.forEachIndexed { i, _ ->
                val sw = sweeps.getOrElse(i) { 360f / pool.size }
                drawArc(
                    color = if (i % 2 == 0) skin.plateA else skin.plateB,
                    startAngle = s2,
                    sweepAngle = sw,
                    useCenter = true,
                    topLeft = plateTopLeft,
                    size = plateSize,
                )
                s2 += sw
            }

            // ③ 靠圆心淡出，避免 28 条辐条扎成一朵菊花
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to skin.plateA,
                        0.52f to skin.plateA,
                        0.78f to skin.plateA.copy(alpha = 0.55f),
                        1.00f to Color.Transparent,
                    ),
                    center = c,
                    radius = rPlate,
                ),
                radius = rPlate,
                center = c,
            )

            // ④ 外侧刻度
            var s3 = -90f
            pool.forEachIndexed { i, _ ->
                val sw = sweeps.getOrElse(i) { 360f / pool.size }
                rotate(s3, c) {
                    drawLine(
                        color = skin.plateLine,
                        start = Offset(c.x, c.y - rPlate),
                        end = Offset(c.x, c.y - rPlate * 0.86f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                s3 += sw
            }

            // ⑤ 揭晓后盘面压暗，盘沿保持饱和
            if (dimmed) {
                drawCircle(color = skin.plateA.copy(alpha = if (dark) 0.42f else 0.55f), radius = rPlate, center = c)
            }

            // ⑥ 中奖标注：盘面是奶油色，白线会隐形，所以用该组的颜色在盘沿内侧压一道短弧
            if (winnerIndex != null && winnerIndex in pool.indices) {
                val boundary = -90f + sweeps.take(winnerIndex).sum()
                val width = sweeps[winnerIndex]
                val rr = (rPlate - 4.dp.toPx()).coerceAtLeast(1f)
                drawArc(
                    color = FoodColors.groupColor(pool[winnerIndex].group, dark),
                    startAngle = boundary + 0.5f,
                    sweepAngle = (width - 1f).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = Offset(c.x - rr, c.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = 4.dp.toPx()),
                )
            }
        }

        // 中心盘（不跟着转）
        Box(
            Modifier
                .align(Alignment.Center)
                .size(HUB_SIZE)
                .clip(CircleShape)
                .background(skin.hubBg)
                .border(1.dp, skin.hubRing, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = pool.size.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = skin.hubNum,
                )
                Text(
                    text = "个候选",
                    fontSize = 9.5.sp,
                    color = skin.hubLabel,
                    modifier = Modifier.offset(y = (-2).dp),
                )
            }
        }

        // 指针
        Canvas(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .size(width = 18.dp, height = 30.dp),
        ) {
            val w = size.width
            val h = size.height
            val body = Path().apply {
                moveTo(w / 2f, h)
                lineTo(w * 0.18f, h * 0.34f)
                quadraticBezierTo(w * 0.18f, h * 0.08f, w / 2f, h * 0.08f)
                quadraticBezierTo(w * 0.82f, h * 0.08f, w * 0.82f, h * 0.34f)
                close()
            }
            drawPath(body, color = skin.needle)
            drawPath(body, color = Color.White, style = Stroke(width = 2.dp.toPx()))
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = w * 0.09f,
                center = Offset(w / 2f, h * 0.30f),
            )
        }
    }
}
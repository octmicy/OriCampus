package cn.edu.xyc.campus.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.edu.xyc.campus.data.local.FoodCategories
import cn.edu.xyc.campus.data.local.FoodCategory
import cn.edu.xyc.campus.data.local.FoodGroup
import cn.edu.xyc.campus.data.local.FoodPrefs
import cn.edu.xyc.campus.data.local.ThemeStore
import cn.edu.xyc.campus.ui.components.FoodSpinWheel
import cn.edu.xyc.campus.ui.components.pickWeighted
import cn.edu.xyc.campus.ui.components.sectorSweeps
import cn.edu.xyc.campus.ui.theme.FoodColors
import cn.edu.xyc.campus.ui.theme.isAppDarkTheme
import kotlinx.coroutines.launch
import kotlin.random.Random

/** 页面标题：22sp SemiBold（约束文档 §2.1） */
private val TitleStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)

/** 结果品类名：24sp Bold（v2 §4.1 指定的例外档，只在本页使用） */
private val ResultStyle = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)

/**
 * 「今天吃什么」——蓝调刻度盘。
 * 浅色暖砂 / 深色深瓷，跟随 isAppDarkTheme() 自动切换；数据全部本地，无网络请求。
 */
@Composable
fun FoodPickerScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dark = isAppDarkTheme()
    val skin = FoodColors.skin(dark)
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var selectedIds by remember { mutableStateOf(FoodPrefs.selectedIds(context)) }
    val pool = remember(selectedIds) { FoodCategories.all.filter { it.id in selectedIds } }
    val recentIds = remember { FoodPrefs.recentIds(context) }
    val weights = remember(pool, recentIds) { pool.map { if (it.id in recentIds) 0.3f else 1f } }
    val sweeps = remember(pool, weights) { sectorSweeps(weights) }

    val rotation = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<FoodCategory?>(null) }
    var winnerIndex by remember { mutableStateOf<Int?>(null) }
    var showFilter by remember { mutableStateOf(false) }

    // 主题包激活时页面底走主题，暖砂/深瓷只留在转盘与结果卡上
    val themeActive = ThemeStore.active.value != null
    val pageBg = when {
        themeActive -> MaterialTheme.colorScheme.background
        dark -> FoodColors.inkPage
        else -> FoodColors.warmPage
    }

    fun resetResult() {
        result = null
        winnerIndex = null
    }

    fun applySelection(next: Set<String>) {
        FoodPrefs.setSelected(context, next)
        selectedIds = next
        resetResult()
    }

    fun toggleGroup(group: FoodGroup) {
        val ids = FoodCategories.idsOf(group)
        val next = if (selectedIds.containsAll(ids)) selectedIds - ids else selectedIds + ids
        if (next.size < 3) {
            Toast.makeText(context, "至少保留 3 个品类", Toast.LENGTH_SHORT).show()
            return
        }
        applySelection(next)
    }

    fun spin() {
        if (spinning || pool.size < 3) return
        spinning = true
        resetResult()
        scope.launch {
            val idx = pickWeighted(weights)
            val before = sweeps.take(idx).sum()
            val centerAngle = before + sweeps[idx] / 2f
            val jitter = (Random.nextFloat() - 0.5f) * sweeps[idx] * 0.4f
            val target = -centerAngle + jitter
            val current = rotation.value
            val delta = ((target - current) % 360f + 360f) % 360f
            val turns = 5 + Random.nextInt(3)
            val duration = (2200 + turns * 140).coerceAtMost(3200)
            rotation.animateTo(
                targetValue = current + delta + turns * 360f,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f),
                ),
            )
            winnerIndex = idx
            result = pool[idx]
            spinning = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(pageBg)
            .systemBarsPadding(),
    ) {
        // ── 顶栏 ──
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回")
            }
            Text("今天吃什么", style = TitleStyle, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(onClick = { showFilter = true }) {
                    Icon(Icons.Rounded.Tune, contentDescription = "筛选品类")
                }
                Text(
                    text = pool.size.toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }

        // ── 范围 chip ──
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.heightIn(max = 40.dp),
        ) {
            item {
                ScopeChip(
                    text = "全部 ${FoodCategories.all.size}",
                    selected = selectedIds.size == FoodCategories.all.size,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = { applySelection(FoodCategories.allIds) },
                    onAccent = MaterialTheme.colorScheme.onPrimary,
                )
            }
            items(FoodGroup.entries.toList()) { group ->
                val ids = FoodCategories.idsOf(group)
                // 「全部」状态下不点亮组 chip，否则六颗全亮像六个筛选同时开着
                val active = selectedIds.containsAll(ids) && selectedIds.size < FoodCategories.allIds.size
                ScopeChip(
                    text = group.label,
                    selected = active,
                    accent = FoodColors.groupColor(group, dark),
                    onClick = { toggleGroup(group) },
                )
            }
        }

        // ── 转盘 ──
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box {
                if (result != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .size(104.dp)
                            .background(
                                Brush.radialGradient(listOf(skin.glow, Color.Transparent)),
                                CircleShape,
                            ),
                    )
                }
                FoodSpinWheel(
                    pool = pool,
                    sweeps = sweeps,
                    rotation = rotation.value,
                    winnerIndex = winnerIndex,
                    dark = dark,
                    dimmed = result != null,
                )
            }
        }

        // ── 结果坞（两态等高，揭晓不跳版）──
        Box(
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val picked = result
            if (picked == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (spinning) "命运正在旋转…" else "还没决定？让转盘来",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = if (spinning) "指针对准的扇区就是答案" else "${pool.size} 个品类 · 最近吃过的会变窄",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                }
            } else {
                ResultCard(picked, dark)
            }
        }

        // ── 操作 ──
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp),
        ) {
            val picked = result
            if (picked == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (spinning) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.primary,
                        )
                        .clickable(enabled = !spinning) { spin() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (spinning) "转动中…" else "转！",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                Box(
                    Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            FoodPrefs.addPick(context, picked.id)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("就吃这个", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .width(118.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, skin.cardBorder, RoundedCornerShape(8.dp))
                        .clickable { spin() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("再转一次", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = skin.needle)
                }
            }
        }

        // ── 最近吃过 ──
        RecentRow(dark)

        Spacer(Modifier.height(10.dp))
    }

    if (showFilter) {
        FoodFilterSheet(
            selected = selectedIds,
            dark = dark,
            onDismiss = { showFilter = false },
            onConfirm = {
                applySelection(it)
                showFilter = false
            },
        )
    }
}

@Composable
private fun ScopeChip(text: String, selected: Boolean, accent: Color, onClick: () -> Unit, onAccent: Color = Color.White) {
    val bg = if (selected) accent else MaterialTheme.colorScheme.surface
    val fg = if (selected) onAccent else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                1.dp,
                if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!selected) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 11.5.sp, color = fg, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun ResultCard(picked: FoodCategory, dark: Boolean) {
    val skin = FoodColors.skin(dark)
    val tone = FoodColors.groupColor(picked.group, dark)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(skin.cardBg)
            .border(1.dp, skin.cardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tone.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(picked.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "今 天 就 吃",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = picked.name,
                style = ResultStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(tone),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = picked.group.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecentRow(dark: Boolean) {
    val context = LocalContext.current
    val history = remember { FoodPrefs.history(context) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "最近吃过",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
        Spacer(Modifier.height(8.dp))
        if (history.isEmpty()) {
            Text(
                "还没有记录，转一次试试",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                history.take(4).forEach { pick ->
                    val cat = FoodCategories.byId(pick.categoryId) ?: return@forEach
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dark) 0.5f else 0.6f))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(cat.emoji, fontSize = 11.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            FoodCategories.shortName(cat),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

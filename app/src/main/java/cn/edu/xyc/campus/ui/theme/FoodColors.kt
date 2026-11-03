package cn.edu.xyc.campus.ui.theme

import androidx.compose.ui.graphics.Color
import cn.edu.xyc.campus.data.local.FoodGroup

/**
 * 转盘皮肤。盘沿五组色是功能语义色（不随皮肤、不随主题包变），
 * 皮肤只决定盘面、指针、中心盘和结果卡的质感。
 *
 * 浅色 = 暖砂，深色 = 深瓷，跟随 isAppDarkTheme() 自动切换，不单独给用户开关。
 */
data class WheelSkin(
    val plateA: Color,
    val plateB: Color,
    val plateLine: Color,
    val gap: Color,
    val needle: Color,
    val glow: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val hubBg: Color,
    val hubRing: Color,
    val hubNum: Color,
    val hubLabel: Color,
)

object FoodColors {

    private val NOODLE = Color(0xFFE8711A)
    private val RICE = Color(0xFF2E7D32)
    private val SNACK = Color(0xFF7B1FA2)
    private val FAST = Color(0xFFC62828)
    private val OTHER = Color(0xFF1565C0)

    private val NOODLE_DARK = Color(0xFFF08A3C)
    private val RICE_DARK = Color(0xFF4CAF50)
    private val SNACK_DARK = Color(0xFFA052C8)
    private val FAST_DARK = Color(0xFFE14B4B)
    private val OTHER_DARK = Color(0xFF4C8FE0)

    fun groupColor(group: FoodGroup, dark: Boolean): Color = if (dark) {
        when (group) {
            FoodGroup.NOODLE -> NOODLE_DARK
            FoodGroup.RICE -> RICE_DARK
            FoodGroup.SNACK -> SNACK_DARK
            FoodGroup.FAST -> FAST_DARK
            FoodGroup.OTHER -> OTHER_DARK
        }
    } else {
        when (group) {
            FoodGroup.NOODLE -> NOODLE
            FoodGroup.RICE -> RICE
            FoodGroup.SNACK -> SNACK
            FoodGroup.FAST -> FAST
            FoodGroup.OTHER -> OTHER
        }
    }

    /** 暖砂：奶油盘面 + 暖橙指针 */
    val warm = WheelSkin(
        plateA = Color(0xFFFFFDF8),
        plateB = Color(0xFFFAF1E3),
        plateLine = Color(0xFFF0E1CB),
        gap = Color(0x80FFFDF8),
        needle = NOODLE,
        glow = Color(0x4DE8711A),
        cardBg = Color(0xFFFFFDF9),
        cardBorder = Color(0xFFF2E6D6),
        hubBg = Color(0xFFFFFCF6),
        hubRing = Color(0xFFF1E0C8),
        hubNum = Color(0xFFB4561A),
        hubLabel = Color(0xFFA28C74),
    )

    /** 深瓷：深蓝瓷盘 + 金线 + 金针 */
    val ink = WheelSkin(
        plateA = Color(0xFF141E2C),
        plateB = Color(0xFF1A2636),
        plateLine = Color(0x33F2B705),
        gap = Color(0x990E1622),
        needle = Color(0xFFF2B705),
        glow = Color(0x38F2B705),
        cardBg = Color(0xFF141C28),
        cardBorder = Color(0xFF23303F),
        hubBg = Color(0xFF13223B),
        hubRing = Color(0x57F2B705),
        hubNum = Color(0xFFF6EBD0),
        hubLabel = Color(0xFF8FA4C4),
    )

    fun skin(dark: Boolean): WheelSkin = if (dark) ink else warm

    // 页面底色（主题包激活时由调用方改用 colorScheme.background）
    val warmPage = Color(0xFFFFFBF4)
    val inkPage = Color(0xFF0B1119)
}
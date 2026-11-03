package cn.edu.xyc.campus.data.local

/** 菜品大类：转盘盘沿的五段颜色就是这五个组 */
enum class FoodGroup(val label: String) {
    NOODLE("粉面"),
    RICE("饭类"),
    SNACK("小吃点心"),
    FAST("快餐"),
    OTHER("其他"),
}

/** 一个品类 = 转盘上的一格 */
data class FoodCategory(
    val id: String,
    val name: String,
    val group: FoodGroup,
    val emoji: String,
)

/**
 * 「今天吃什么」的 28 个品类，全部编译进 APK，无网络依赖。
 * 顺序即转盘顺序：按组连续排列，盘沿色段长度 = 该组品类数。
 */
object FoodCategories {

    val all: List<FoodCategory> = listOf(
        // 粉面（11）
        FoodCategory("noodle_soup", "汤粉面", FoodGroup.NOODLE, "🍜"),
        FoodCategory("noodle_mix", "拌面", FoodGroup.NOODLE, "🍝"),
        FoodCategory("rice_noodle_mix", "拌粉", FoodGroup.NOODLE, "🥢"),
        FoodCategory("noodle_stir", "炒面", FoodGroup.NOODLE, "🍳"),
        FoodCategory("rice_noodle_stir", "炒粉", FoodGroup.NOODLE, "🍳"),
        FoodCategory("knife_noodle", "刀削面", FoodGroup.NOODLE, "🔪"),
        FoodCategory("pulled_noodle", "拉面", FoodGroup.NOODLE, "🍜"),
        FoodCategory("rice_noodle", "米线", FoodGroup.NOODLE, "🍜"),
        FoodCategory("hot_sour_noodle", "酸辣粉/螺蛳粉", FoodGroup.NOODLE, "🌶️"),
        FoodCategory("cold_noodle", "凉皮/凉面", FoodGroup.NOODLE, "🥗"),
        FoodCategory("instant_noodle", "炒方便面", FoodGroup.NOODLE, "🍜"),

        // 饭类（5）
        FoodCategory("rice_topping", "盖浇饭", FoodGroup.RICE, "🍚"),
        FoodCategory("fried_rice", "炒饭", FoodGroup.RICE, "🍳"),
        FoodCategory("iron_plate_rice", "铁板饭/煲仔饭", FoodGroup.RICE, "🥘"),
        FoodCategory("braised_rice", "卤肉饭", FoodGroup.RICE, "🍖"),
        FoodCategory("set_meal", "套餐饭", FoodGroup.RICE, "🍱"),

        // 小吃点心（5）
        FoodCategory("dumpling", "水饺", FoodGroup.SNACK, "🥟"),
        FoodCategory("wonton", "馄饨/抄手", FoodGroup.SNACK, "🥟"),
        FoodCategory("baozi", "包子", FoodGroup.SNACK, "🥐"),
        FoodCategory("bing", "饼类", FoodGroup.SNACK, "🫓"),
        FoodCategory("congee", "粥", FoodGroup.SNACK, "🍲"),

        // 快餐（3）
        FoodCategory("burger", "汉堡", FoodGroup.FAST, "🍔"),
        FoodCategory("fried_chicken", "炸鸡", FoodGroup.FAST, "🍗"),
        FoodCategory("chicken_leg", "鸡排/鸡腿", FoodGroup.FAST, "🍗"),

        // 其他（4）
        FoodCategory("stir_fry", "小炒", FoodGroup.OTHER, "🍳"),
        FoodCategory("braised", "卤味", FoodGroup.OTHER, "🥩"),
        FoodCategory("dry_pot", "干锅/煲类", FoodGroup.OTHER, "🥘"),
        FoodCategory("mala_tang", "麻辣烫", FoodGroup.OTHER, "🌶️"),
    )

    val allIds: Set<String> = all.map { it.id }.toSet()

    fun byId(id: String): FoodCategory? = all.firstOrNull { it.id == id }

    fun of(group: FoodGroup): List<FoodCategory> = all.filter { it.group == group }

    fun idsOf(group: FoodGroup): Set<String> = of(group).map { it.id }.toSet()

    /** 转盘上一格的短标签（首页卡等窄处使用） */
    fun shortName(c: FoodCategory): String = c.name.substringBefore('/')
}
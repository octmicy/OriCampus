package cn.edu.xyc.campus.data.local

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** 一次「就吃这个」的记录 */
data class FoodPick(val categoryId: String, val at: Long)

/**
 * 「今天吃什么」本地偏好：筛选范围 + 最近 10 次结果。
 * 工程没有 DataStore 依赖（约束文档禁止新增依赖），统一走 SharedPreferences，与 AppGridPrefs / HomePrefs 一致。
 */
object FoodPrefs {

    private const val PREFS = "food_picker"
    private const val KEY_SELECTED = "selected_ids"
    private const val KEY_HISTORY = "history"
    private const val HISTORY_MAX = 10

    /** 当前参与转盘的品类 id；没存过或存坏了都回退成全部 28 个 */
    fun selectedIds(context: Context): Set<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED, null) ?: return FoodCategories.allIds
        val ids = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val valid = ids intersect FoodCategories.allIds
        return if (valid.size >= 3) valid else FoodCategories.allIds
    }

    fun setSelected(context: Context, ids: Set<String>) {
        val valid = ids intersect FoodCategories.allIds
        if (valid.size < 3) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SELECTED, valid.joinToString(",")).apply()
    }

    /** 最近吃过（新 → 旧），最多 10 条 */
    fun history(context: Context): List<FoodPick> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id")
                    if (id.isNotEmpty() && FoodCategories.byId(id) != null) {
                        add(FoodPick(id, o.optLong("ts")))
                    }
                }
            }
        }.getOrDefault(emptyList()).take(HISTORY_MAX)
    }

    fun recentIds(context: Context): Set<String> = history(context).map { it.categoryId }.toSet()

    fun addPick(context: Context, categoryId: String) {
        val list = ArrayList(history(context))
        list.removeAll { it.categoryId == categoryId }
        list.add(0, FoodPick(categoryId, System.currentTimeMillis()))
        val arr = JSONArray()
        list.take(HISTORY_MAX).forEach {
            arr.put(JSONObject().put("id", it.categoryId).put("ts", it.at))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_HISTORY, arr.toString()).apply()
    }
}
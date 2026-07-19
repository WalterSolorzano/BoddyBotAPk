package com.aistudio.unibuddy.qywvsp.data

object FuzzyMatch {
    fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
    
    fun isSimilar(s1: String, s2: String): Boolean {
        val n1 = s1.lowercase().trim()
        val n2 = s2.lowercase().trim()
        val dist = levenshteinDistance(n1, n2)
        val maxLen = maxOf(n1.length, n2.length)
        if (maxLen == 0) return true
        val threshold = maxLen * 0.3 // 30% error allowed
        return dist <= threshold
    }
}

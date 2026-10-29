package domain.service

object BotAlgorithmGame {

    fun chooseMove(board: List<List<Int>>, bot: Int, human: Int): Pair<Int, Int>? {

        findWinningMove(board, bot)?.let { return it }

        findWinningMove(board, human)?.let { return it }

        if (board[1][1] == 0) return 1 to 1

        val corners = listOf(0 to 0, 0 to 2, 2 to 0, 2 to 2)
        for ((x, y) in corners) {
            if (board[x][y] == 0) return x to y
        }

        for (i in 0 until 3)
            for (j in 0 until 3)
                if (board[i][j] == 0)
                    return i to j

        return null
    }

    private fun findWinningMove(board: List<List<Int>>, player: Int): Pair<Int, Int>? {

        for (i in 0 until 3) {
            val row = board[i]
            if (row.count { it == player } == 2 && row.contains(0))
                return i to row.indexOf(0)
        }

        for (j in 0 until 3) {
            val col = listOf(board[0][j], board[1][j], board[2][j])
            if (col.count { it == player } == 2 && col.contains(0))
                return col.indexOf(0) to j
        }

        val d1 = listOf(board[0][0], board[1][1], board[2][2])
        if (d1.count { it == player } == 2 && d1.contains(0))
            return d1.indexOf(0) to d1.indexOf(0)

        val d2 = listOf(board[0][2], board[1][1], board[2][0])
        if (d2.count { it == player } == 2 && d2.contains(0)) {
            val idx = d2.indexOf(0)
            return idx to (2 - idx)
        }

        return null
    }
}

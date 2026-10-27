package io.github.retbrown.archeryutils.classifications

import io.github.retbrown.archeryutils.handicaps.Handicaps
import io.github.retbrown.archeryutils.rounds.Round
import kotlin.math.floor

/**
 * Expected (rounded) score for [round] at [handicap] under handicap [scheme] ("AGB" or "AGBold").
 *
 * Single touchpoint between classifications and the handicaps package.
 */
internal fun hcScoreForRound(handicap: Double, round: Round, scheme: String): Double =
    Handicaps.scoreForRound(handicap, round, scheme, roundedScore = true)

/**
 * Compute the integer classification scores for [classHc] thresholds on [round], then handle
 * possible gaps in the tables or max scores by checking one handicap point above each threshold
 * (floored to handle 0.5): a classification must be exceeded by being lower than the handicap
 * threshold, not awarded if the same score is achievable at a higher handicap.
 *
 * [invalidate] is applied to the raw scores before the gap check (e.g. to remove classifications
 * for rounds that are not eligible). The result has no repeated scores.
 */
internal fun classScoresFromHandicaps(
    classHc: DoubleArray,
    round: Round,
    scheme: String,
    invalidate: (MutableList<Int>) -> Unit = {},
): MutableList<Int> {
    val scores = classHc.map { hcScoreForRound(it, round, scheme).toInt() }.toMutableList()
    invalidate(scores)
    for (i in scores.indices) {
        val nextScore = hcScoreForRound(floor(classHc[i]) + 1, round, scheme).toInt()
        if (nextScore == scores[i]) {
            // If already at max score this classification is impossible
            if (scores[i].toDouble() == round.maxScore()) {
                scores[i] = INVALID_CLASS_SCORE
            } else {
                // If gap in table increase to next score
                // (we assume here that no two classifications are only 1 point apart...)
                scores[i] += 1
            }
        }
    }
    return ClassificationUtils.fixRepeatedScores(scores, round.maxScore())
}

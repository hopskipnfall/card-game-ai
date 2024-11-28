package com.hopskipnfall.cardgameai

import com.google.common.flogger.FluentLogger

private val logger = FluentLogger.forEnclosingClass()

fun main() {
  // Use log4j as the flogger backend.
  System.setProperty(
    "flogger.backend_factory",
    "com.google.common.flogger.backend.log4j2.Log4j2BackendFactory#getInstance"
  )

  val dealer =
    Dealer(
      HumanPlayer(),
      RandomPlayer(),
      RandomPlayer(),
      RandomPlayer(),
      RandomPlayer(),
    )

  val winner = dealer.playGame()

  logger.atSevere().log("Winner: %s (%s)", winner, winner.description())
}

typealias Card = Int

class Dealer(private vararg val players: Player) {
  init {
    for ((index, player) in players.withIndex()) {
      player.playerNumber = index + 1
    }
  }

  private val pointCards: MutableList<Card> =
    (-5..10).toMutableList().also {
      it.remove(0)
      it.shuffle()
    }

  fun playGame(): Player {
    while (pointCards.isNotEmpty()) {
      doRound()
    }

    return players.maxBy { it.pointCards.sum() }
  }

  private fun doRound(): Player? {
    val nextCard = pointCards.removeFirst()
    logger.atSevere().log("Round card: %d", nextCard)

    val playersToCards =
      players.associateWith { PlayerCards(pointCards = it.pointCards, cards = it.cards) }

    val playedCards = mutableMapOf<Card, MutableSet<Player>>()
    for (player in playersToCards.keys) {
      val played: Card =
        player.playCard(cardToWin = nextCard, playersToCards.filterKeys { it != player }.values)
      logger.atSevere().log("%s played card: %d", player, played)

      playedCards.getOrPut(played) { mutableSetOf() }.add(player)
    }

    var winner: Player? = null
    for ((card, players) in
      playedCards.entries.sortedByDescending { if (nextCard > 0) it.key else -it.key }) {
      if (players.size > 1) {
        continue
      }
      winner = players.single()
      logger.atSevere().log("%s wins the round\n", winner)
      winner.pointCards.add(nextCard)
      break
    }
    return winner!!
  }
}

data class PlayerCards(val pointCards: List<Card>, val cards: List<Card>)

abstract class Player {
  val cards: MutableList<Card> = (1..15).toMutableList().also { it.shuffle() }
  val pointCards = mutableListOf<Card>()

  var playerNumber: Int = -1

  abstract fun playCard(cardToWin: Card, otherPlayersCards: Collection<PlayerCards>): Card

  override fun toString(): String = "player#$playerNumber" // + this.javaClass.simpleName

  fun description() =
    "cards = ${cards.sorted()}, earned = ${pointCards.sorted()}, score = ${pointCards.sum()}"
}

class RandomPlayer : Player() {
  override fun playCard(cardToWin: Card, otherPlayersCards: Collection<PlayerCards>): Card =
    cards.removeFirst()
}

class HumanPlayer : Player() {
  override fun playCard(cardToWin: Card, otherPlayersCards: Collection<PlayerCards>): Card {

    logger.atSevere().log("Choose a card from your list: %s", this)
    val card = readln().trim().toInt()
    check(cards.remove(card)) { "User picked a card that isn't in their deck" }
    return card
  }
}

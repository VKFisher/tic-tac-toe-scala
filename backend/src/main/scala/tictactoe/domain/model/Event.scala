package tictactoe.domain.model

import tictactoe.domain.model.{Move, MoveRejectionReason}

enum Event {
  case MoveAcceptedEvent(
      gameId: GameId,
      move: Move
  )

  case MoveRejectedEvent(
      gameId: GameId,
      move: Move,
      rejectionReason: MoveRejectionReason
  )
}

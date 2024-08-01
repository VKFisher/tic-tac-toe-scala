package tictactoe.domain.model

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

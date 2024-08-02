module Api.Data exposing
    ( CellState
    , GameField
    , GameId(..)
    , GameResult(..)
    , GameSide(..)
    , GameState
    , GameStatus(..)
    , Move
    , gameStateDecoder
    , moveEncoder
    , toNextMoveSide
    )

import Dict
import Iso8601
import Json.Decode as Decode exposing (Decoder)
import Json.Encode as Encode
import Time


type GameSide
    = X
    | O


emptyJsonObjectDecoder : Decoder ()
emptyJsonObjectDecoder =
    Decode.dict Decode.int
        |> Decode.andThen
            (\entries ->
                case Dict.size entries of
                    0 ->
                        Decode.succeed ()

                    _ ->
                        Decode.fail "Expected empty JSON object"
            )


gameSideDecoder : Decoder GameSide
gameSideDecoder =
    Decode.oneOf
        [ Decode.field "X" emptyJsonObjectDecoder |> Decode.map (always X)
        , Decode.field "O" emptyJsonObjectDecoder |> Decode.map (always O)
        ]


gameSideEncoder : GameSide -> Encode.Value
gameSideEncoder gameSide =
    case gameSide of
        X ->
            Encode.object [ ( "X", Encode.object [] ) ]

        O ->
            Encode.object [ ( "O", Encode.object [] ) ]


type GameResult
    = GameResultWin GameSide
    | GameResultDraw


gameResultDecoder : Decoder GameResult
gameResultDecoder =
    let
        decodeWin : Decoder GameResult
        decodeWin =
            Decode.map (\side -> GameResultWin side) gameSideDecoder
                |> Decode.field "winningSide"
                |> Decode.field "Win"

        decodeDraw : Decoder GameResult
        decodeDraw =
            Decode.field "Draw" emptyJsonObjectDecoder
                |> Decode.map (always GameResultDraw)
    in
    Decode.oneOf
        [ decodeWin
        , decodeDraw
        ]


type GameStatus
    = GameStatusOngoing GameSide -- next move
    | GameStatusEnded GameResult


toNextMoveSide : GameStatus -> Maybe GameSide
toNextMoveSide gameStatus =
    case gameStatus of
        GameStatusOngoing x ->
            Just x

        GameStatusEnded _ ->
            Nothing


gameStatusDecoder : Decoder GameStatus
gameStatusDecoder =
    let
        decodeOngoing : Decoder GameStatus
        decodeOngoing =
            Decode.map (\side -> GameStatusOngoing side) gameSideDecoder
                |> Decode.field "nextMoveSide"
                |> Decode.field "GameOngoing"

        decodeEnded : Decoder GameStatus
        decodeEnded =
            Decode.map (\result -> GameStatusEnded result) gameResultDecoder
                |> Decode.field "result"
                |> Decode.field "GameEnded"
    in
    Decode.oneOf
        [ decodeOngoing
        , decodeEnded
        ]


type alias CellState =
    Maybe GameSide


cellStateDecoder : Decoder CellState
cellStateDecoder =
    Decode.nullable gameSideDecoder


type alias GameField =
    ( ( CellState, CellState, CellState )
    , ( CellState, CellState, CellState )
    , ( CellState, CellState, CellState )
    )


decodeThreeTuple : Decoder a -> Decoder ( a, a, a )
decodeThreeTuple itemDecoder =
    Decode.map3 (\a b c -> ( a, b, c ))
        (Decode.index 0 itemDecoder)
        (Decode.index 1 itemDecoder)
        (Decode.index 2 itemDecoder)


gameRowDecoder : Decoder ( CellState, CellState, CellState )
gameRowDecoder =
    decodeThreeTuple cellStateDecoder


gameFieldDecoder : Decoder GameField
gameFieldDecoder =
    decodeThreeTuple gameRowDecoder


type alias Move =
    { side : GameSide
    , coords : ( Int, Int )
    }


indexDecoder : Decoder Int
indexDecoder =
    Decode.int
        |> Decode.andThen
            (\x ->
                case x of
                    0 ->
                        Decode.succeed 0

                    1 ->
                        Decode.succeed 1

                    2 ->
                        Decode.succeed 2

                    _ ->
                        Decode.fail "invalid index"
            )


coordsDecoder : Decoder ( Int, Int )
coordsDecoder =
    Decode.map2 Tuple.pair
        (Decode.field "row" indexDecoder)
        (Decode.field "col" indexDecoder)


coordsEncoder : ( Int, Int ) -> Encode.Value
coordsEncoder ( row, col ) =
    Encode.object
        [ ( "row", Encode.int row )
        , ( "col", Encode.int col )
        ]


moveDecoder : Decoder Move
moveDecoder =
    Decode.map2 Move
        (Decode.field "side" gameSideDecoder)
        (Decode.field "coords" coordsDecoder)


moveEncoder : Move -> Encode.Value
moveEncoder move =
    Encode.object
        [ ( "side", gameSideEncoder move.side )
        , ( "coords", coordsEncoder move.coords )
        ]


type GameId
    = GameId String


gameIdDecoder : Decoder GameId
gameIdDecoder =
    Decode.map GameId (Decode.field "value" Decode.string)


type alias GameState =
    { id : GameId
    , startedAt : Time.Posix
    , field : GameField
    , status : GameStatus
    , moves : List Move
    }


gameStateDecoder : Decoder GameState
gameStateDecoder =
    Decode.map5 GameState
        (Decode.field "id" gameIdDecoder)
        (Decode.field "startedAt" Iso8601.decoder)
        (Decode.field "field" gameFieldDecoder)
        (Decode.field "status" gameStatusDecoder)
        (Decode.field "moves" (Decode.list moveDecoder))

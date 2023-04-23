module Main exposing (main)

import Api.Data exposing (..)
import Browser
import Element exposing (..)
import Element.Font as Font
import Element.Region as Region
import Http
import RemoteData exposing (RemoteData(..), WebData)


main : Program () Model Msg
main =
    Browser.element
        { init = always init
        , update = update
        , view = Element.layout [ Font.family [ Font.typeface "Open Sans", Font.sansSerif ] ] << view
        , subscriptions = always Sub.none
        }


backendUrl : String
backendUrl =
    "http://localhost:8080"


type alias Model =
    { gameSide : WebData GameSide
    , gameField : WebData GameField
    , gameState : WebData GameState
    }


init : ( Model, Cmd Msg )
init =
    ( { gameSide = NotAsked
      , gameField = NotAsked
      , gameState = NotAsked
      }
    , Cmd.batch
        [ getGameSide
        , getGameField
        , getGameStateById 1
        ]
    )


type Msg
    = GameSideResponse (Result Http.Error GameSide)
    | GameFieldResponse (Result Http.Error GameField)
    | GameStateResponse (Result Http.Error GameState)


getGameStateById : Int -> Cmd Msg
getGameStateById gameId =
    Http.get
        { url = backendUrl ++ "/tic-tac-toe/" ++ String.fromInt gameId
        , expect = Http.expectJson GameStateResponse gameStateDecoder
        }


getGameSide : Cmd Msg
getGameSide =
    Http.get
        { url = backendUrl ++ "/tic-tac-toe/1/side"
        , expect = Http.expectJson GameSideResponse gameSideDecoder
        }


getGameField : Cmd Msg
getGameField =
    Http.get
        { url = backendUrl ++ "/tic-tac-toe/1/field"
        , expect = Http.expectJson GameFieldResponse gameFieldDecoder
        }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GameSideResponse x ->
            case x of
                Ok gameSide ->
                    ( { model | gameSide = Success gameSide }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )

        GameFieldResponse x ->
            case x of
                Ok gameField ->
                    ( { model | gameField = Success gameField }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )

        GameStateResponse x ->
            case x of
                Ok gameState ->
                    ( { model | gameState = Success gameState }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )


view : Model -> Element Msg
view model =
    column [ padding 40, spacing 40 ]
        [ el [ Region.heading 1, Font.size 30, Font.bold ] <| text "Elm app"
        , viewGameStateWebData model.gameState
        ]


viewGameStateWebData : WebData GameState -> Element Msg
viewGameStateWebData x =
    case x of
        Success state ->
            viewGameState state

        _ ->
            text "???"


viewGameState : GameState -> Element Msg
viewGameState x =
    column [ spacing 10 ]
        [ viewGameStatus x.status
        , viewGameField x.field
        ]


viewGreeting : WebData String -> Element Msg
viewGreeting greeting =
    text <|
        case greeting of
            Success x ->
                x

            _ ->
                "?"


showGameSide : GameSide -> String
showGameSide x =
    case x of
        X ->
            "X"

        O ->
            "O"


viewGameSide : WebData GameSide -> Element Msg
viewGameSide greeting =
    case greeting of
        Success x ->
            text <| showGameSide x

        _ ->
            text "no game side"


showCellState : CellState -> String
showCellState x =
    case x of
        Just X ->
            "[ X ]"

        Just O ->
            "[ O ]"

        Nothing ->
            "[   ]"


viewGameRow : ( CellState, CellState, CellState ) -> Element Msg
viewGameRow ( a, b, c ) =
    row [ spacing 10 ] <| List.map (showCellState >> text) [ a, b, c ]


viewGameField : GameField -> Element Msg
viewGameField gameField =
    let
        ( r1, r2, r3 ) =
            gameField
    in
    column [ spacing 10 ] <| List.map viewGameRow [ r1, r2, r3 ]


viewGameStatus : GameStatus -> Element Msg
viewGameStatus gameStatus =
    case gameStatus of
        GameStatusOngoing nextSide ->
            text <| "next move side: " ++ showGameSide nextSide

        GameStatusEnded result ->
            text <| "Game ended, result: " ++ showGameResult result


showGameResult : GameResult -> String
showGameResult res =
    case res of
        GameResultDraw ->
            "Draw"

        GameResultWin winningSide ->
            "Win by " ++ showGameSide winningSide

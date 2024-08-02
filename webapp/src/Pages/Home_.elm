module Pages.Home_ exposing (Model, Msg, page)

import Api.Types exposing (..)
import Effect exposing (Effect)
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Font as Font
import Element.Input exposing (button)
import Element.Region as Region
import Http exposing (emptyBody, jsonBody)
import Json.Decode as Decode
import Maybe.Extra
import Page exposing (Page)
import RemoteData exposing (RemoteData(..), WebData)
import Route exposing (Route)
import Shared
import Time
import View exposing (View)



-- Ports
-- Page-specific types and related functions
-- Page-specific constants


backendUrl : String
backendUrl =
    "http://localhost:8001"



-- Flags, main, page


page : Shared.Model -> Route () -> Page Model Msg
page _ _ =
    Page.new
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- Model, init


type alias Model =
    { gameList : WebData (List GameState)
    }


init : () -> ( Model, Effect Msg )
init _ =
    ( { gameList = NotAsked }, Effect.sendCmd getGames )



-- Subscriptions


subscriptions : Model -> Sub Msg
subscriptions _ =
    Time.every 200 (always GetGames)



-- Library configs
-- Network requests


getGames : Cmd Msg
getGames =
    Http.get
        { url = backendUrl ++ "/tic-tac-toe/list"
        , expect = Http.expectJson GameListResponse (Decode.list gameStateDecoder)
        }


startGame : Cmd Msg
startGame =
    Http.post
        { url = backendUrl ++ "/tic-tac-toe/start"
        , expect = Http.expectJson GameStartResponse gameStateDecoder
        , body = emptyBody
        }


makeMove : GameId -> Move -> Cmd Msg
makeMove (GameId uuidString) move =
    Http.post
        { url = backendUrl ++ "/tic-tac-toe/" ++ uuidString ++ "/move"
        , expect = Http.expectJson MoveResponse gameStateDecoder
        , body = jsonBody <| moveEncoder move
        }



-- Msg, update


type Msg
    = StartButtonPressed
    | GetGames
    | MadeMove GameId Move
    | MoveResponse (Result Http.Error GameState)
    | GameStartResponse (Result Http.Error GameState)
    | GameListResponse (Result Http.Error (List GameState))



-- | GameStateResponse (Result Http.Error GameState)


update : Msg -> Model -> ( Model, Effect Msg )
update msg model =
    case msg of
        StartButtonPressed ->
            ( model, Effect.sendCmd startGame )

        GetGames ->
            ( model, Effect.sendCmd getGames )

        MadeMove gameId move ->
            ( model, Effect.sendCmd <| makeMove gameId move )

        GameListResponse x ->
            case x of
                Ok games ->
                    ( { model
                        | gameList =
                            Success
                                (List.sortBy (\g -> negate <| Time.posixToMillis g.startedAt) games)
                      }
                    , Effect.none
                    )

                Err e ->
                    ( { model | gameList = Failure e }, Effect.none )

        GameStartResponse x ->
            case x of
                Ok _ ->
                    ( model, Effect.sendCmd getGames )

                Err _ ->
                    ( model, Effect.none )

        MoveResponse x ->
            case x of
                Ok _ ->
                    ( model, Effect.sendCmd getGames )

                Err _ ->
                    ( model, Effect.none )



-- View


view : Model -> View Msg
view model =
    { title = "Homepage"
    , attributes = [ Font.family [ Font.typeface "Open Sans", Font.sansSerif ] ]
    , element =
        column [ padding 40, spacing 40, centerX ]
            [ el [ Region.heading 1, Font.size 30, Font.bold ] <| text "Tic-Tac-Toe"
            , viewStartGameButton
            , viewGameList model.gameList
            ]
    }


viewStartGameButton : Element Msg
viewStartGameButton =
    button [ padding 10, Background.color (rgb 0.75 0.75 0.75), Border.color (rgb 0 0 0), Border.width 1 ]
        { onPress = Just StartButtonPressed
        , label = text "Start game"
        }


viewGameList : WebData (List GameState) -> Element Msg
viewGameList dataListGame =
    case dataListGame of
        Success games ->
            column [ spacing 40 ] (List.map viewGameState games)

        _ ->
            text "???"


showGameId : GameId -> String
showGameId (GameId x) =
    x


viewGameState : GameState -> Element Msg
viewGameState game =
    row [ spacing 20 ]
        [ viewGameField game.id (toNextMoveSide game.status) game.field
        , column [ spacing 10 ]
            [ viewGameStatus game.status
            , el [ Font.color (rgb 0.8 0.8 0.8) ] <| text <| showGameId game.id
            ]
        ]


showGameSide : GameSide -> String
showGameSide x =
    case x of
        X ->
            "X"

        O ->
            "O"


viewGameSide : GameSide -> Element msg
viewGameSide x =
    case x of
        X ->
            el [ Font.color (rgb 1 0 0) ] <| text <| showGameSide x

        O ->
            el [ Font.color (rgb 0 0 1) ] <| text <| showGameSide x


showCellState : GameId -> Maybe Move -> CellState -> Element Msg
showCellState gameId maybeMove x =
    let
        cellAttr : List (Attribute msg)
        cellAttr =
            [ height (px 50), width (px 50) ]

        labelAttr : List (Attribute msg)
        labelAttr =
            [ centerX, centerY ]
    in
    case x of
        Just gameSide ->
            el (cellAttr ++ [ Background.color (rgb 0.75 0.75 0.75) ]) <| el labelAttr <| viewGameSide gameSide

        Nothing ->
            Maybe.Extra.unwrap (el (cellAttr ++ [ Background.color (rgb 0.85 0.85 0.85) ]) <| el labelAttr none)
                (\move ->
                    button
                        (cellAttr ++ [ Background.color (rgb 1 1 1), Element.mouseOver [ Background.color (rgb 0.7 0.7 1) ] ])
                        { onPress = Just (MadeMove gameId move)
                        , label = el labelAttr none
                        }
                )
                maybeMove


viewGameRow : GameId -> Maybe GameSide -> Int -> ( CellState, CellState, CellState ) -> Element Msg
viewGameRow gameId nextMoveGameSide rowIndex ( a, b, c ) =
    let
        toMove : Int -> Maybe Move
        toMove colIndex =
            Maybe.map (\side -> { side = side, coords = ( rowIndex, colIndex ) }) nextMoveGameSide
    in
    [ a, b, c ]
        |> List.indexedMap (toMove >> showCellState gameId)
        |> row [ spacing 1 ]


viewGameField : GameId -> Maybe GameSide -> GameField -> Element Msg
viewGameField gameId nextMoveGameSide gameField =
    let
        ( r1, r2, r3 ) =
            gameField
    in
    column
        [ spacing 1, Border.color (rgb 0 0 0), Border.width 1, Background.color (rgb 0 0 0) ]
    <|
        List.indexedMap (viewGameRow gameId nextMoveGameSide) [ r1, r2, r3 ]


viewGameStatus : GameStatus -> Element Msg
viewGameStatus gameStatus =
    case gameStatus of
        GameStatusOngoing nextSide ->
            text <| "Next move: " ++ showGameSide nextSide

        GameStatusEnded result ->
            text <| showGameResult result


showGameResult : GameResult -> String
showGameResult res =
    case res of
        GameResultDraw ->
            "Draw"

        GameResultWin winningSide ->
            showGameSide winningSide ++ " wins!"

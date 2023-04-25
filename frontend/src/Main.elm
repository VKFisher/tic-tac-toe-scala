module Main exposing (main)

import Browser
import Dict
import Element exposing (..)
import Element.Font as Font
import Element.Input as Input
import Element.Region as Region
import Http
import Json.Decode as Decode exposing (Decoder)
import RemoteData exposing (RemoteData(..), WebData)


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


moveDecoder : Decoder Move
moveDecoder =
    Decode.map2 Move
        (Decode.field "side" gameSideDecoder)
        (Decode.field "coords" coordsDecoder)


type alias GameState =
    { field : GameField
    , status : GameStatus
    , moves : List Move
    }


gameStateDecoder : Decoder GameState
gameStateDecoder =
    Decode.map3 GameState
        (Decode.field "field" gameFieldDecoder)
        (Decode.field "status" gameStatusDecoder)
        (Decode.field "moves" (Decode.list moveDecoder))


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
    { nameInput : String
    , greeting : WebData String
    , gameSide : WebData GameSide
    , gameField : WebData GameField
    , gameState : WebData GameState
    }


init : ( Model, Cmd Msg )
init =
    let
        defaultName : String
        defaultName =
            "John"
    in
    ( { nameInput = defaultName
      , greeting = NotAsked
      , gameSide = NotAsked
      , gameField = NotAsked
      , gameState = NotAsked
      }
    , Cmd.batch
        [ getGreeting defaultName
        , getGameSide
        , getGameField
        , getGameStateById 1
        ]
    )


type Msg
    = SetName String
    | GreetingResponse (Result Http.Error String)
    | GameSideResponse (Result Http.Error GameSide)
    | GameFieldResponse (Result Http.Error GameField)
    | GameStateResponse (Result Http.Error GameState)


getGreeting : String -> Cmd Msg
getGreeting name =
    Http.get
        { url = backendUrl ++ "/greet?name=" ++ name
        , expect = Http.expectString GreetingResponse
        }


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
        SetName x ->
            ( { model | nameInput = x }, getGreeting x )

        GreetingResponse x ->
            case x of
                Ok greeting ->
                    ( { model | greeting = Success greeting }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )

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
        , Input.text [ width <| maximum 300 fill ]
            { onChange = SetName
            , text = model.nameInput
            , placeholder = Just <| Input.placeholder [] <| text "Jack"
            , label = Input.labelAbove [] <| text "Name"
            }
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

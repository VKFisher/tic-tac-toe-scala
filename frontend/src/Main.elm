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


emptyJsonDecoder : Decoder ()
emptyJsonDecoder =
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
        [ Decode.field "X" emptyJsonDecoder |> Decode.map (always X)
        , Decode.field "O" emptyJsonDecoder |> Decode.map (always O)
        ]



-- type alias CellState =
--     Maybe GameSide
-- type GameOutcome
--     = GameOutcomeWin GameSide
--     | GameOutcomeDraw
-- type GameStatus
--     = GameStatusOngoing GameSide -- next move
--     | GameStatusEnded GameOutcome
-- type GameState
--     = GameState
--         { field : GameField
--         , status : GameStatus
--         }
-- type alias GameField =
--     ( ( CellState, CellState, CellState )
--     , ( CellState, CellState, CellState )
--     , ( CellState, CellState, CellState )
--     )


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
      }
    , Cmd.batch [ getGreeting defaultName, getGameSide ]
    )


type Msg
    = SetName String
    | GreetingResponse (Result Http.Error String)
    | GameSideResponse (Result Http.Error GameSide)


getGreeting : String -> Cmd Msg
getGreeting name =
    Http.get
        { url = backendUrl ++ "/greet?name=" ++ name
        , expect = Http.expectString GreetingResponse
        }


getGameSide : Cmd Msg
getGameSide =
    Http.get
        { url = backendUrl ++ "/tic-tac-toe/1/game-side"
        , expect = Http.expectJson GameSideResponse gameSideDecoder
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
        , viewGreeting model.greeting
        , viewGameSide model.gameSide
        ]



-- viewControls : Element Msg
-- viewControls =
--     row []
--         [ Input.button []
--             { onPress = Nothing
--             , label = text "New game"
--             }
--         ]
-- viewGameField : GameField -> Element Msg
-- viewGameField _ =
--     none


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
                none

module Main exposing (main)

import Browser
import Element exposing (..)
import Element.Font as Font
import Element.Input as Input
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
    { nameInput : String
    , greeting : WebData String
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
      }
    , getGreeting defaultName
    )


type Msg
    = SetName String
    | GreetingResponse (Result Http.Error String)


getGreeting : String -> Cmd Msg
getGreeting name =
    Http.get
        { url = backendUrl ++ "/greet?name=" ++ name
        , expect = Http.expectString GreetingResponse
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
        ]


viewGreeting : WebData String -> Element Msg
viewGreeting greeting =
    text <|
        case greeting of
            Success x ->
                x

            _ ->
                "?"

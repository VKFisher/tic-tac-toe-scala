module Main exposing (main)

import Browser
import Element exposing (..)
import Element.Font as Font
import Element.Input as Input
import Element.Region as Region


main : Program () Model Msg
main =
    Browser.sandbox
        { init = init
        , update = update
        , view = Element.layout [ Font.family [ Font.typeface "Open Sans", Font.sansSerif ] ] << view
        }


type alias Model =
    { nameInput : String
    }


init : Model
init =
    { nameInput = "John"
    }


type Msg
    = SetName String


update : Msg -> Model -> Model
update msg model =
    case msg of
        SetName x ->
            { model | nameInput = x }


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
        , text <| "Hello, " ++ model.nameInput
        ]
